package io.github.elli0t0.calendar.ui.common

import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import java.time.LocalDate

private val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** Converts English digits in a string to Persian digits, leaving the rest. */
fun toPersianDigits(s: String): String =
    s.map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")

/** 1500000 -> «۱٬۵۰۰٬۰۰۰ تومان». A negative value keeps its leading minus. */
fun formatAmount(value: Long): String {
    val grouped = value.toString().reversed().chunked(3).joinToString("٬").reversed()
    return "${toPersianDigits(grouped)} تومان"
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

/** Groups English digits with ٬ and converts them to Persian digits. */
fun formatGroupedDigits(digits: String): String {
    if (digits.isEmpty()) return ""
    val grouped = digits.reversed().chunked(3).joinToString("٬").reversed()
    return toPersianDigits(grouped)
}

/** Full Shamsi date with weekday, e.g. «دوشنبه ۱۸ آبان ۱۴۰۵». */
fun fullJalaliDate(date: LocalDate): String {
    val j = Jalali.toJalali(date)
    return "${PersianNames.weekdayName(date)} ${toPersianDigits(j.day.toString())} " +
        "${PersianNames.jalaliMonthName(j.month)} ${toPersianDigits(j.year.toString())}"
}
