package com.omidshojaee.repeatalarm.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class PersianDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int,   // 1 to 31
) {
    val monthName: String
        get() = PersianCalendarHelper.PERSIAN_MONTH_NAMES.getOrElse(month - 1) { "" }

    /**
     * Formats date to long Persian format (e.g. "۵ اسفند ۱۴۰۳").
     */
    fun formatLong(): String {
        return "$day $monthName $year".toPersianDigits()
    }

    /**
     * Formats date to numeric short Persian format (e.g. "۱۴۰۳/۱۲/۰۵").
     */
    fun formatShort(): String {
        val mStr = if (month < 10) "0$month" else month.toString()
        val dStr = if (day < 10) "0$day" else day.toString()
        return "$year/$mStr/$dStr".toPersianDigits()
    }
}

object PersianCalendarHelper {

    val PERSIAN_MONTH_NAMES = listOf(
        "فروردین",
        "اردیبهشت",
        "خرداد",
        "تیر",
        "مرداد",
        "شهریور",
        "مهر",
        "آبان",
        "آذر",
        "دی",
        "بهمن",
        "اسفند"
    )

    /**
     * Converts Gregorian date (year, month, day) to Julian Day Number (JDN).
     */
    fun gregorianToJdn(gy: Int, gm: Int, gd: Int): Long {
        val date = LocalDate.of(gy, gm, gd)
        return date.toEpochDay() + 2440588L
    }

    /**
     * Converts Julian Day Number (JDN) to Gregorian [LocalDate].
     */
    fun jdnToGregorian(jdn: Long): LocalDate {
        return LocalDate.ofEpochDay(jdn - 2440588L)
    }

    /**
     * Converts Jalali (Persian Solar Hijri) date (year, month, day) to Julian Day Number (JDN).
     */
    fun jalaliToJdn(jy: Int, jm: Int, jd: Int): Long {
        val epbase: Long = if (jy >= 0) jy.toLong() - 474L else jy.toLong() - 473L
        val epyear: Long = 474L + (epbase % 2820L)
        val md: Long = if (jm <= 7) (jm - 1).toLong() * 31L else (jm - 7).toLong() * 30L + 186L
        return jd.toLong() + md + (epyear * 682L - 110L) / 2816L + (epyear - 1L) * 365L + (epbase / 2820L) * 1029983L + 1948320L
    }

    /**
     * Converts Julian Day Number (JDN) to [PersianDate].
     */
    fun jdnToJalali(jdn: Long): PersianDate {
        var jy = ((jdn - 1948320) / 365.2425 + 1).toInt()
        while (jalaliToJdn(jy + 1, 1, 1) <= jdn) {
            jy++
        }
        while (jalaliToJdn(jy, 1, 1) > jdn) {
            jy--
        }

        val dayNum = (jdn - jalaliToJdn(jy, 1, 1)).toInt()
        val jm: Int
        val jd: Int
        if (dayNum < 186) {
            jm = dayNum / 31 + 1
            jd = dayNum % 31 + 1
        } else {
            val rem = dayNum - 186
            jm = rem / 30 + 7
            jd = rem % 30 + 1
        }
        return PersianDate(jy, jm, jd)
    }

    /**
     * Converts Gregorian [LocalDate] to [PersianDate].
     */
    fun gregorianToPersian(date: LocalDate): PersianDate {
        val jdn = gregorianToJdn(date.year, date.monthValue, date.dayOfMonth)
        return jdnToJalali(jdn)
    }

    /**
     * Converts [PersianDate] to Gregorian [LocalDate].
     */
    fun persianToGregorian(persianDate: PersianDate): LocalDate {
        val jdn = jalaliToJdn(persianDate.year, persianDate.month, persianDate.day)
        return jdnToGregorian(jdn)
    }

    /**
     * Converts epoch milliseconds to [PersianDate] using local timezone.
     */
    fun epochMillisToPersianDate(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): PersianDate {
        val localDate = Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalDate()
        return gregorianToPersian(localDate)
    }

    /**
     * Converts [PersianDate] to epoch milliseconds at start of day (00:00:00) using local timezone.
     */
    fun persianDateToEpochMillis(persianDate: PersianDate, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        val localDate = persianToGregorian(persianDate)
        return localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    /**
     * Checks if a Jalali year is a leap year.
     */
    fun isLeapYear(jy: Int): Boolean {
        return (jalaliToJdn(jy + 1, 1, 1) - jalaliToJdn(jy, 1, 1)) == 366L
    }

    /**
     * Returns the number of days in a given Jalali month of a given Jalali year.
     */
    fun getDaysInMonth(jy: Int, jm: Int): Int {
        return when (jm) {
            in 1..6 -> 31
            in 7..11 -> 30
            12 -> if (isLeapYear(jy)) 30 else 29
            else -> throw IllegalArgumentException("Invalid Jalali month: $jm")
        }
    }

    /**
     * Formats epoch millis as long Persian date string (e.g., "۵ اسفند ۱۴۰۳").
     */
    fun formatDate(epochMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return epochMillisToPersianDate(epochMillis, zoneId).formatLong()
    }
}
