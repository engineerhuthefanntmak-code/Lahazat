package com.floating.stopwatch.core.utils

import com.floating.stopwatch.core.utils.toArabicNumerals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.regex.Pattern

class UiLanguageVerificationTest {

    @Test
    fun testArabicNumeralsConversion() {
        assertEquals("٠١٢٣٤٥٦٧٨٩", "0123456789".toArabicNumerals())
        assertEquals("الآية: ١٥", "الآية: 15".toArabicNumerals())
    }

    @Test
    fun testNoLatinLettersInCoreStrings() {
        val appName = "الريّاش"
        val appSubtitle = "في رواية شعبة بن عياش"

        val latinPattern = Pattern.compile("[a-zA-Z]")
        assertFalse("App name must not contain Latin letters", latinPattern.matcher(appName).find())
        assertFalse("App subtitle must not contain Latin letters", latinPattern.matcher(appSubtitle).find())
    }
}
