package com.omidshojaee.repeatalarm.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PersianCalendarHelperTest {

    @Test
    fun testGregorianToPersianConversion() {
        // 2024-03-20 -> 1 Farvardin 1403
        val date1 = LocalDate.of(2024, 3, 20)
        val pDate1 = PersianCalendarHelper.gregorianToPersian(date1)
        assertEquals(1403, pDate1.year)
        assertEquals(1, pDate1.month)
        assertEquals(1, pDate1.day)
        assertEquals("فروردین", pDate1.monthName)

        // 2024-03-19 -> 29 Esfand 1402 (non-leap year)
        val date2 = LocalDate.of(2024, 3, 19)
        val pDate2 = PersianCalendarHelper.gregorianToPersian(date2)
        assertEquals(1402, pDate2.year)
        assertEquals(12, pDate2.month)
        assertEquals(29, pDate2.day)

        // 2025-03-19 -> 29 Esfand 1403 (non-leap year)
        val date3 = LocalDate.of(2025, 3, 19)
        val pDate3 = PersianCalendarHelper.gregorianToPersian(date3)
        assertEquals(1403, pDate3.year)
        assertEquals(12, pDate3.month)
        assertEquals(29, pDate3.day)

        // 2026-03-20 -> 30 Esfand 1404 (leap year)
        val date4 = LocalDate.of(2026, 3, 20)
        val pDate4 = PersianCalendarHelper.gregorianToPersian(date4)
        assertEquals(1404, pDate4.year)
        assertEquals(12, pDate4.month)
        assertEquals(30, pDate4.day)

        // 1979-02-11 -> 22 Bahman 1357
        val date5 = LocalDate.of(1979, 2, 11)
        val pDate5 = PersianCalendarHelper.gregorianToPersian(date5)
        assertEquals(1357, pDate5.year)
        assertEquals(11, pDate5.month)
        assertEquals(22, pDate5.day)
        assertEquals("بهمن", pDate5.monthName)

        // 2000-01-01 -> 11 Dey 1378
        val date6 = LocalDate.of(2000, 1, 1)
        val pDate6 = PersianCalendarHelper.gregorianToPersian(date6)
        assertEquals(1378, pDate6.year)
        assertEquals(10, pDate6.month)
        assertEquals(11, pDate6.day)
        assertEquals("دی", pDate6.monthName)
    }

    @Test
    fun testPersianToGregorianConversion() {
        // 1 Farvardin 1403 -> 2024-03-20
        val pDate1 = PersianDate(1403, 1, 1)
        val gDate1 = PersianCalendarHelper.persianToGregorian(pDate1)
        assertEquals(LocalDate.of(2024, 3, 20), gDate1)

        // 29 Esfand 1402 -> 2024-03-19
        val pDate2 = PersianDate(1402, 12, 29)
        val gDate2 = PersianCalendarHelper.persianToGregorian(pDate2)
        assertEquals(LocalDate.of(2024, 3, 19), gDate2)

        // 29 Esfand 1403 -> 2025-03-19
        val pDate3 = PersianDate(1403, 12, 29)
        val gDate3 = PersianCalendarHelper.persianToGregorian(pDate3)
        assertEquals(LocalDate.of(2025, 3, 19), gDate3)

        // 30 Esfand 1404 -> 2026-03-20
        val pDate4 = PersianDate(1404, 12, 30)
        val gDate4 = PersianCalendarHelper.persianToGregorian(pDate4)
        assertEquals(LocalDate.of(2026, 3, 20), gDate4)

        // 22 Bahman 1357 -> 1979-02-11
        val pDate5 = PersianDate(1357, 11, 22)
        val gDate5 = PersianCalendarHelper.persianToGregorian(pDate5)
        assertEquals(LocalDate.of(1979, 2, 11), gDate5)
    }

    @Test
    fun testBiDirectionalRoundTrip() {
        // Test round trip for a sequence of 1000 days starting from 2020-01-01
        var current = LocalDate.of(2020, 1, 1)
        repeat(1000) {
            val persian = PersianCalendarHelper.gregorianToPersian(current)
            val backToGregorian = PersianCalendarHelper.persianToGregorian(persian)
            assertEquals("Failed for date $current", current, backToGregorian)
            current = current.plusDays(1)
        }
    }

    @Test
    fun testLeapYears() {
        assertTrue(PersianCalendarHelper.isLeapYear(1404))
        assertFalse(PersianCalendarHelper.isLeapYear(1403))
        assertFalse(PersianCalendarHelper.isLeapYear(1402))
        assertTrue(PersianCalendarHelper.isLeapYear(1399))
        assertFalse(PersianCalendarHelper.isLeapYear(1400))
    }

    @Test
    fun testDaysInMonth() {
        // Farvardin to Shahrivar (months 1-6) have 31 days
        for (m in 1..6) {
            assertEquals(31, PersianCalendarHelper.getDaysInMonth(1403, m))
        }
        // Mehr to Bahman (months 7-11) have 30 days
        for (m in 7..11) {
            assertEquals(30, PersianCalendarHelper.getDaysInMonth(1403, m))
        }
        // Esfand in non-leap year (1403) has 29 days
        assertEquals(29, PersianCalendarHelper.getDaysInMonth(1403, 12))
        // Esfand in leap year (1404) has 30 days
        assertEquals(30, PersianCalendarHelper.getDaysInMonth(1404, 12))
    }

    @Test
    fun testMonthNames() {
        val expectedNames = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
        )
        assertEquals(expectedNames, PersianCalendarHelper.PERSIAN_MONTH_NAMES)

        expectedNames.forEachIndexed { index, name ->
            val pDate = PersianDate(1403, index + 1, 1)
            assertEquals(name, pDate.monthName)
        }
    }

    @Test
    fun testPersianDateFormatting() {
        val pDate = PersianDate(1403, 12, 5)
        assertEquals("۵ اسفند ۱۴۰۳", pDate.formatLong())
        assertEquals("۱۴۰۳/۱۲/۰۵", pDate.formatShort())
    }

    @Test
    fun testEpochMillisConversion() {
        val zoneId = ZoneId.of("Asia/Tehran")
        val pDate = PersianDate(1403, 6, 15)
        val millis = PersianCalendarHelper.persianDateToEpochMillis(pDate, zoneId)
        val backToPersian = PersianCalendarHelper.epochMillisToPersianDate(millis, zoneId)

        assertEquals(pDate.year, backToPersian.year)
        assertEquals(pDate.month, backToPersian.month)
        assertEquals(pDate.day, backToPersian.day)
    }
}
