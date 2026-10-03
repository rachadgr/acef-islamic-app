package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.data.model.GpsLocationData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.TimeZone

class GpsLocationHelper(private val context: Context) {

    sealed class GpsState {
        object Idle : GpsState()
        object Locating : GpsState()
        data class Success(val locationData: GpsLocationData) : GpsState()
        object PermissionRequired : GpsState()
        object GpsDisabled : GpsState()
        data class Error(val message: String) : GpsState()
    }

    private val _gpsState = MutableStateFlow<GpsState>(GpsState.Idle)
    val gpsState: StateFlow<GpsState> = _gpsState.asStateFlow()

    private val _lastGpsLocation = MutableStateFlow<GpsLocationData?>(null)
    val lastGpsLocation: StateFlow<GpsLocationData?> = _lastGpsLocation.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    fun isLocationServiceEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return try {
            val isGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            isGps || isNetwork
        } catch (_: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun requestGpsLocation(onComplete: ((GpsLocationData?) -> Unit)? = null) {
        if (!hasLocationPermission()) {
            _gpsState.value = GpsState.PermissionRequired
            onComplete?.invoke(null)
            return
        }
        if (!isLocationServiceEnabled()) {
            _gpsState.value = GpsState.GpsDisabled
            onComplete?.invoke(null)
            return
        }

        _gpsState.value = GpsState.Locating

        scope.launch {
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cts = CancellationTokenSource()
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            handleNewLocation(location, onComplete)
                        } else {
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                                if (lastLoc != null) {
                                    handleNewLocation(lastLoc, onComplete)
                                } else {
                                    requestViaLocationManager(onComplete)
                                }
                            }.addOnFailureListener {
                                requestViaLocationManager(onComplete)
                            }
                        }
                    }
                    .addOnFailureListener {
                        requestViaLocationManager(onComplete)
                    }
            } catch (e: Exception) {
                requestViaLocationManager(onComplete)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestViaLocationManager(onComplete: ((GpsLocationData?) -> Unit)?) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            _gpsState.value = GpsState.Error("خدمة تحديد المواقع غير متوفرة")
            onComplete?.invoke(null)
            return
        }
        try {
            val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLoc = when {
                gpsLoc != null && netLoc != null -> if (gpsLoc.time >= netLoc.time) gpsLoc else netLoc
                gpsLoc != null -> gpsLoc
                else -> netLoc
            }
            if (bestLoc != null) {
                handleNewLocation(bestLoc, onComplete)
                return
            }

            val singleListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    handleNewLocation(location, onComplete)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationManager.GPS_PROVIDER
            } else {
                LocationManager.NETWORK_PROVIDER
            }
            locationManager.requestSingleUpdate(provider, singleListener, Looper.getMainLooper())

            scope.launch {
                delay(12000)
                if (_gpsState.value is GpsState.Locating) {
                    locationManager.removeUpdates(singleListener)
                    val fallback = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    if (fallback != null) {
                        handleNewLocation(fallback, onComplete)
                    } else {
                        _gpsState.value = GpsState.Error("تعذر التقاط إشارة GPS، يرجى المحاولة في مكان مفتوح")
                        onComplete?.invoke(null)
                    }
                }
            }
        } catch (e: Exception) {
            _gpsState.value = GpsState.Error("خطأ في GPS: ${e.localizedMessage ?: "غير معروف"}")
            onComplete?.invoke(null)
        }
    }

    private fun handleNewLocation(location: Location, onComplete: ((GpsLocationData?) -> Unit)?) {
        scope.launch(Dispatchers.IO) {
            val lat = location.latitude
            val lng = location.longitude
            val altitude = location.altitude
            val accuracy = location.accuracy

            val tz = TimeZone.getDefault()
            val offsetHours = (tz.rawOffset + tz.dstSavings).toDouble() / (1000.0 * 60.0 * 60.0)

            var cityAr = ""
            var countryAr = ""
            try {
                val geocoder = Geocoder(context, Locale("ar"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            cityAr = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                            countryAr = addr.countryName ?: ""
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        cityAr = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                        countryAr = addr.countryName ?: ""
                    }
                }
            } catch (_: Exception) {}

            if (cityAr.isBlank()) {
                cityAr = String.format(Locale.getDefault(), "%.4f شمالاً, %.4f شرقاً", lat, lng)
            }

            val data = GpsLocationData(
                latitude = lat,
                longitude = lng,
                altitude = altitude,
                accuracyMeters = accuracy,
                cityNameAr = cityAr,
                countryNameAr = countryAr,
                timezone = offsetHours,
                timestamp = System.currentTimeMillis()
            )

            withContext(Dispatchers.Main) {
                _lastGpsLocation.value = data
                _gpsState.value = GpsState.Success(data)
                onComplete?.invoke(data)
            }
        }
    }
}
