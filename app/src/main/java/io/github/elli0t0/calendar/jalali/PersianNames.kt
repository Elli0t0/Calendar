package io.github.elli0t0.calendar.jalali

import java.time.DayOfWeek
import java.time.LocalDate

/** Persian names and digits for the calendar UI. */
object PersianNames {

    val jalaliMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    val gregorianMonths = listOf(
        "ژانویه", "فوریه", "مارس", "آوریل", "مه", "ژوئن",
        "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    )

    /** Weekday letters, week starting Saturday. Friday (ج) is last. */
    val weekdayLetters = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    /** Full weekday names, week starting Saturday. */
    val weekdayNames = listOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه"
    )

    fun weekdayName(date: LocalDate): String = weekdayNames[weekdayIndex(date.dayOfWeek)]

    fun jalaliMonthName(month: Int): String = jalaliMonths[month - 1]

    fun gregorianMonthName(month: Int): String = gregorianMonths[month - 1]

    /** Letter for a Gregorian date, Saturday = ش (index 0). */
    fun weekdayLetter(date: LocalDate): String = weekdayLetters[weekdayIndex(date.dayOfWeek)]

    /** Saturday = 0 … Friday = 6. */
    fun weekdayIndex(dayOfWeek: DayOfWeek): Int = (dayOfWeek.value + 1) % 7

    fun isFriday(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.FRIDAY
}

private val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** 2026 -> «۲۰۲۶». */
fun Int.toPersianDigits(): String =
    toString().map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")

fun Long.toPersianDigits(): String =
    toString().map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")
