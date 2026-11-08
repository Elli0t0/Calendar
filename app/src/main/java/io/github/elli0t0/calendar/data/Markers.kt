package io.github.elli0t0.calendar.data

import java.time.LocalDate

enum class MarkerType { INCOME, EXPENSE, INSTALLMENT }

data class DayMarker(val type: MarkerType, val title: String, val amount: Long? = null)

interface MarkerProvider {
    fun markersFor(date: LocalDate): List<DayMarker>
}

/** Release default: no markers. */
class EmptyMarkerProvider : MarkerProvider {
    override fun markersFor(date: LocalDate): List<DayMarker> = emptyList()
}

/**
 * Debug-only look: a few sample markers on fixed days of the current month.
 * Only ever used when BuildConfig.DEBUG is true (see MainActivity).
 */
class FakeMarkerProvider : MarkerProvider {
    override fun markersFor(date: LocalDate): List<DayMarker> {
        val markers = mutableListOf<DayMarker>()
        when (date.dayOfMonth % 9) {
            0 -> markers.add(DayMarker(MarkerType.INCOME, "حقوق", 25_000_000L))
            3 -> markers.add(DayMarker(MarkerType.EXPENSE, "خرید", 850_000L))
            5 -> markers.add(DayMarker(MarkerType.INSTALLMENT, "قسط وام", 3_200_000L))
        }
        if (date.dayOfMonth % 7 == 2) {
            markers.add(DayMarker(MarkerType.EXPENSE, "خوراک", 420_000L))
        }
        return markers.take(3)
    }
}
