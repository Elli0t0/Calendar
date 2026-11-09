package io.github.elli0t0.calendar.data

import java.time.LocalDate

/** Orange markers for unpaid installments due on the date. */
class InstallmentMarkerProvider : MarkerProvider {
    override fun markersFor(date: LocalDate): List<DayMarker> {
        return InstallmentRepository.items.value.flatMap { inst ->
            (0 until inst.count)
                .filter { k -> k !in inst.paid && dueDate(inst, k) == date }
                .map { DayMarker(MarkerType.INSTALLMENT, inst.title, inst.amountPerMonth, refId = inst.id) }
        }
    }
}
