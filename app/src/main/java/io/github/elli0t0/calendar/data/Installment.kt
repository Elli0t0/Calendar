package io.github.elli0t0.calendar.data

import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

data class Installment(
    val id: Long,
    val title: String,
    val amountPerMonth: Long,
    val dayOfMonth: Int, // Jalali day, 1..31
    val count: Int, // total number of installments
    val startYear: Int, // Jalali year of the first installment
    val startMonth: Int, // Jalali month of the first installment
    val paid: Set<Int>, // indexes 0 until count that are paid
)

/**
 * Due date of installment [k] (0-based). The month carries into later years;
 * the day is clamped to that month's length.
 */
fun dueDate(i: Installment, k: Int): LocalDate {
    val total = (i.startYear * 12 + (i.startMonth - 1)) + k
    val year = Math.floorDiv(total, 12)
    val month = Math.floorMod(total, 12) + 1
    val day = minOf(i.dayOfMonth, Jalali.monthLength(year, month))
    return Jalali.toGregorian(JalaliDate(year, month, day))
}

internal fun Installment.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("amountPerMonth", amountPerMonth)
    put("dayOfMonth", dayOfMonth)
    put("count", count)
    put("startYear", startYear)
    put("startMonth", startMonth)
    val paidArray = JSONArray()
    paid.forEach { paidArray.put(it) }
    put("paid", paidArray)
}

internal fun JSONObject.toInstallment(): Installment {
    val paidArray = optJSONArray("paid")
    val paid = mutableSetOf<Int>()
    if (paidArray != null) {
        for (i in 0 until paidArray.length()) paid.add(paidArray.getInt(i))
    }
    return Installment(
        id = getLong("id"),
        title = getString("title"),
        amountPerMonth = getLong("amountPerMonth"),
        dayOfMonth = getInt("dayOfMonth"),
        count = getInt("count"),
        startYear = getInt("startYear"),
        startMonth = getInt("startMonth"),
        paid = paid
    )
}
