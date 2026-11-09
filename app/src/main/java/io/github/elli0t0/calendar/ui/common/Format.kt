package io.github.elli0t0.calendar.ui.common

private val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

/** Converts English digits in a string to Persian digits, leaving the rest. */
fun toPersianDigits(s: String): String =
    s.map { if (it in '0'..'9') persianDigits[it - '0'] else it }.joinToString("")

/** 1500000 -> «۱٬۵۰۰٬۰۰۰ تومان». A negative value keeps its leading minus. */
fun formatAmount(value: Long): String {
    val grouped = value.toString().reversed().chunked(3).joinToString("٬").reversed()
    return "${toPersianDigits(grouped)} تومان"
}
