package com.example.util

object NumberFormatter {

    /**
     * Converts digits in the given string to either French/Western (123) or Eastern Arabic (١٢٣)
     */
    fun format(text: String, useArabicDigits: Boolean): String {
        if (text.isEmpty()) return text
        return if (useArabicDigits) {
            toEasternArabicDigits(text)
        } else {
            toFrenchWesternDigits(text)
        }
    }

    fun format(number: Number, useArabicDigits: Boolean): String {
        return format(number.toString(), useArabicDigits)
    }

    private fun toEasternArabicDigits(input: String): String {
        val sb = java.lang.StringBuilder(input.length)
        for (i in 0 until input.length) {
            val ch = input[i]
            when (ch) {
                '0' -> sb.append('٠')
                '1' -> sb.append('١')
                '2' -> sb.append('٢')
                '3' -> sb.append('٣')
                '4' -> sb.append('٤')
                '5' -> sb.append('٥')
                '6' -> sb.append('٦')
                '7' -> sb.append('٧')
                '8' -> sb.append('٨')
                '9' -> sb.append('٩')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    private fun toFrenchWesternDigits(input: String): String {
        val sb = java.lang.StringBuilder(input.length)
        for (i in 0 until input.length) {
            val ch = input[i]
            when (ch) {
                '٠' -> sb.append('0')
                '١' -> sb.append('1')
                '٢' -> sb.append('2')
                '٣' -> sb.append('3')
                '٤' -> sb.append('4')
                '٥' -> sb.append('5')
                '٦' -> sb.append('6')
                '٧' -> sb.append('7')
                '٨' -> sb.append('8')
                '٩' -> sb.append('9')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }
}
