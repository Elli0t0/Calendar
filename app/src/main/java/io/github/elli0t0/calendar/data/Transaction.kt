package io.github.elli0t0.calendar.data

import org.json.JSONObject

enum class TxType { INCOME, EXPENSE }

enum class Repeat { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }

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
    val note: String = "",
    /** Index into EventPalette, or -1 to use the type color. */
    val colorIndex: Int = -1,
    val repeat: Repeat = Repeat.NONE,
    /** Last epoch day the series repeats on, or null when it never ends. */
    val repeatUntilEpochDay: Long? = null,
    val remind: Remind = Remind.NONE,
)

internal fun Transaction.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("type", type.name)
    put("title", title)
    put("amount", amount)
    put("epochDay", epochDay)
    put("note", note)
    put("colorIndex", colorIndex)
    put("repeat", repeat.name)
    if (repeatUntilEpochDay != null) put("repeatUntilEpochDay", repeatUntilEpochDay)
    put("remind", remind.name)
}

internal fun JSONObject.toTransaction(): Transaction = Transaction(
    id = getLong("id"),
    type = TxType.valueOf(getString("type")),
    title = getString("title"),
    amount = getLong("amount"),
    epochDay = getLong("epochDay"),
    note = optString("note", ""),
    colorIndex = optInt("colorIndex", -1),
    repeat = runCatching { Repeat.valueOf(optString("repeat", Repeat.NONE.name)) }
        .getOrDefault(Repeat.NONE),
    repeatUntilEpochDay = if (has("repeatUntilEpochDay") && !isNull("repeatUntilEpochDay")) {
        getLong("repeatUntilEpochDay")
    } else {
        null
    },
    remind = runCatching { Remind.valueOf(optString("remind", Remind.NONE.name)) }
        .getOrDefault(Remind.NONE)
)
