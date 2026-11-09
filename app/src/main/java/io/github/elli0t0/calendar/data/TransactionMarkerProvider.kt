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
        // Evaluated only for the visible days, so a repeating event shows on
        // every matching day without precomputing its whole series.
        return TxRepository.items.value
            .filter { occursOn(it, date) }
            .map { it.toDayMarker() }
    }
}
