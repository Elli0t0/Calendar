package io.github.elli0t0.calendar.ui.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.jalali.isPersianLocale
import java.time.LocalDate

private val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** Converts English digits in a string to Persian digits when the app
 * language is fa; leaves them Latin otherwise. */
fun toPersianDigits(s: String): String {
    if (!isPersianLocale()) return s
    return s.map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")
}

/** 1500000 -> «۱٬۵۰۰٬۰۰۰» (fa) or «1,500,000» (en). A negative value keeps
 * its leading minus. The currency word is not part of the number: callers
 * wrap the result in R.string.amount_with_currency. */
fun formatAmount(value: Long): String {
    val separator = if (isPersianLocale()) "٬" else ","
    val grouped = value.toString().reversed().chunked(3).joinToString(separator).reversed()
    return toPersianDigits(grouped)
}

/** Keeps only digits (Persian or English), capped so the value fits in a Long. */
fun normalizeDigits(input: String): String {
    val out = StringBuilder()
    for (c in input) {
        when (c) {
            in '0'..'9' -> out.append(c)
            in '۰'..'۹' -> out.append('0' + (c - '۰'))
            in '٠'..'٩' -> out.append('0' + (c - '٠'))
        }
    }
    return out.toString().take(15)
}

/** Groups English digits with the locale's thousands separator and converts
 * them to the locale's digits. */
fun formatGroupedDigits(digits: String): String {
    if (digits.isEmpty()) return ""
    val separator = if (isPersianLocale()) "٬" else ","
    val grouped = digits.reversed().chunked(3).joinToString(separator).reversed()
    return toPersianDigits(grouped)
}

/** Full Shamsi date with weekday, e.g. «دوشنبه ۱۸ آبان ۱۴۰۵» (fa) or
 * «Monday, 18 Aban 1405» (en). Word order comes from R.string.date_full. */
@Composable
fun fullJalaliDate(date: LocalDate): String {
    val j = Jalali.toJalali(date)
    return stringResource(
        R.string.date_full,
        PersianNames.weekdayName(date),
        toPersianDigits(j.day.toString()),
        PersianNames.jalaliMonthName(j.month),
        toPersianDigits(j.year.toString())
    )
}

/** Weekday + day + month, no year: «دوشنبه ۱۸ آبان» (fa) or
 * «Monday, 18 Aban» (en). Used for the Day view title. */
@Composable
fun shortJalaliDate(date: LocalDate): String {
    val j = Jalali.toJalali(date)
    return stringResource(
        R.string.date_day_short,
        PersianNames.weekdayName(date),
        toPersianDigits(j.day.toString()),
        PersianNames.jalaliMonthName(j.month)
    )
}

/** Same text as [fullJalaliDate], but plain (no @Composable) for use outside
 * composition, e.g. notification text. Reads the name arrays from [context]. */
fun formatJalaliDate(context: Context, date: LocalDate): String {
    val j = Jalali.toJalali(date)
    val weekday = context.resources.getStringArray(R.array.weekday_names)[PersianNames.weekdayIndex(date.dayOfWeek)]
    val month = context.resources.getStringArray(R.array.shamsi_month_names)[j.month - 1]
    return context.getString(
        R.string.date_full,
        weekday,
        toPersianDigits(j.day.toString()),
        month,
        toPersianDigits(j.year.toString())
    )
}
