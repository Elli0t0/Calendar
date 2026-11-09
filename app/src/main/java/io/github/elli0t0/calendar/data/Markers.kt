package io.github.elli0t0.calendar.data

import java.time.LocalDate

enum class MarkerType { INCOME, EXPENSE, INSTALLMENT }

data class DayMarker(val type: MarkerType, val title: String, val amount: Long? = null, val refId: Long? = null)

interface MarkerProvider {
    fun markersFor(date: LocalDate): List<DayMarker>
}

/** No markers. */
class EmptyMarkerProvider : MarkerProvider {
    override fun markersFor(date: LocalDate): List<DayMarker> = emptyList()
}

/** Merges several providers; markers are concatenated in provider order. */
class CompositeMarkerProvider(private val providers: List<MarkerProvider>) : MarkerProvider {
    override fun markersFor(date: LocalDate): List<DayMarker> =
        providers.flatMap { it.markersFor(date) }
}
