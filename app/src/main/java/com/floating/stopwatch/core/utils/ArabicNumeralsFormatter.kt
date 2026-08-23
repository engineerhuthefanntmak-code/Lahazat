package com.floating.stopwatch.core.utils

/**
 * Utility to convert Western Arabic digits (0-9) to Eastern Arabic digits (٠-٩)
 * to ensure 100% Arabic numeral compliance across all user-facing UI elements.
 */
fun String.toArabicNumerals(): String {
    val westernDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val easternDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

    var result = this
    for (i in 0..9) {
        result = result.replace(westernDigits[i], easternDigits[i])
    }
    return result
}

fun Int.toArabicNumerals(): String {
    return this.toString().toArabicNumerals()
}

fun Long.toArabicNumerals(): String {
    return this.toString().toArabicNumerals()
}
