package io.github.elli0t0.calendar.jalali

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import io.github.elli0t0.calendar.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/** True when the app's current language is Persian (fa). Digits follow it. */
fun isPersianLocale(): Boolean = Locale.getDefault().language == "fa"

/**
 * Calendar names in the app's current language, read from string arrays
 * (values/arrays.xml, values-fa/arrays.xml). Weekday order starts Saturday.
 * The Jalali algorithm itself (Jalali.kt) stays untouched and locale-free.
 */
object PersianNames {

    @Composable
    fun weekdayName(date: LocalDate): String =
        stringArrayResource(R.array.weekday_names)[weekdayIndex(date.dayOfWeek)]

    @Composable
    fun jalaliMonthName(month: Int): String =
        stringArrayResource(R.array.shamsi_month_names)[month - 1]

    @Composable
    fun gregorianMonthName(month: Int): String =
        stringArrayResource(R.array.gregorian_month_names)[month - 1]

    /** Letter for a Gregorian date, Saturday = first letter (index 0). */
    @Composable
    fun weekdayLetter(date: LocalDate): String =
        stringArrayResource(R.array.weekday_letters)[weekdayIndex(date.dayOfWeek)]

    /** Weekday letters, week starting Saturday. Friday is last. */
    @Composable
    fun weekdayLetters(): List<String> =
        stringArrayResource(R.array.weekday_letters).toList()

    /** Saturday = 0 … Friday = 6. */
    fun weekdayIndex(dayOfWeek: DayOfWeek): Int = (dayOfWeek.value + 1) % 7

    fun isFriday(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.FRIDAY
}

private val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** 2026 -> «۲۰۲۶» in Persian, «2026» otherwise. */
fun Int.toPersianDigits(): String {
    if (!isPersianLocale()) return toString()
    return toString().map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")
}

fun Long.toPersianDigits(): String {
    if (!isPersianLocale()) return toString()
    return toString().map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")
}
