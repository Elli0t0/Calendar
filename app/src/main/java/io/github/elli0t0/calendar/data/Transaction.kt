package io.github.elli0t0.calendar.data

import org.json.JSONObject

enum class TxType { INCOME, EXPENSE }

/**
 * A transaction. The date is stored as an epoch day (LocalDate.toEpochDay())
 * and converted to Jalali only for display. Amounts are Long, in Toman.
 */
data class Transaction(
    val id: Long,
    val type: TxType,
    val title: String,
    val amount: Long,
    val epochDay: Long,
)

internal fun Transaction.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("type", type.name)
    put("title", title)
    put("amount", amount)
    put("epochDay", epochDay)
}

internal fun JSONObject.toTransaction(): Transaction = Transaction(
    id = getLong("id"),
    type = TxType.valueOf(getString("type")),
    title = getString("title"),
    amount = getLong("amount"),
    epochDay = getLong("epochDay")
)
