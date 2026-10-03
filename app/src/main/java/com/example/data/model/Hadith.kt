package com.example.data.model

data class Hadith(
    val id: Int,
    val category: String,
    val text: String,
    val narrator: String,
    val source: String,
    val hadithNumber: String,
    val grading: String // "صحيح", "حسن", "متفق عليه"
)

data class HourlyHadithExplanation(
    val id: Int,
    val hourOfDay: Int,
    val title: String,
    val textArabic: String,
    val narrator: String,
    val source: String,
    val grading: String,
    val explanation: String,
    val keyLessons: List<String>,
    val hourLabel: String = ""
)

data class Dhikr(
    val id: Int,
    val category: String,
    val text: String,
    val count: Int,
    val fadl: String = "",
    val reference: String = ""
)
