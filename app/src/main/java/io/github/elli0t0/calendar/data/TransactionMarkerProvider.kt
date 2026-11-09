package io.github.elli0t0.calendar.data

import java.time.LocalDate

fun Transaction.toDayMarker(): DayMarker = DayMarker(
    type = if (type == TxType.INCOME) MarkerType.INCOME else MarkerType.EXPENSE,
    title = title,
    amount = amount,
    refId = id,
    colorIndex = colorIndex
)

/** Real markers from stored transactions (replaces the debug fake). */
class TransactionMarkerProvider : MarkerProvider {
    override fun markersFor(date: LocalDate): List<DayMarker> {
        val day = date.toEpochDay()
        return TxRepository.items.value
            .filter { it.epochDay == day }
            .map { it.toDayMarker() }
    }
}
