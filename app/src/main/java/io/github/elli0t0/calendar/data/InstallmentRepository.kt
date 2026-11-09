package io.github.elli0t0.calendar.data

import android.content.Context
import io.github.elli0t0.calendar.notify.ReminderScheduler
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for installments. Initialized once in
 * MainActivity.onCreate with the application context. Disk writes happen on
 * Dispatchers.IO; the StateFlow updates immediately on the calling thread.
 */
object InstallmentRepository {

    private val _items = MutableStateFlow<List<Installment>>(emptyList())
    val items: StateFlow<List<Installment>> = _items.asStateFlow()

    private lateinit var store: InstallmentStore
    private lateinit var appContext: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        appContext = context.applicationContext
        store = InstallmentStore(File(context.filesDir, "installments.json"))
        scope.launch {
            _items.value = store.load()
            ReminderScheduler.reschedule(appContext, TxRepository.items.value, _items.value)
        }
    }

    fun add(
        title: String,
        amountPerMonth: Long,
        dayOfMonth: Int,
        count: Int,
        startYear: Int,
        startMonth: Int,
        remind: Remind = Remind.NONE,
    ) {
        val nextId = (_items.value.maxOfOrNull { it.id } ?: 0L) + 1L
        updateList(
            _items.value + Installment(
                nextId, title, amountPerMonth, dayOfMonth, count, startYear, startMonth,
                emptySet(), remind
            )
        )
    }

    fun update(inst: Installment) {
        updateList(_items.value.map { if (it.id == inst.id) inst else it })
    }

    fun delete(id: Long) {
        updateList(_items.value.filterNot { it.id == id })
    }

    fun togglePaid(id: Long, index: Int) {
        updateList(
            _items.value.map {
                if (it.id != id) it
                else if (index in it.paid) it.copy(paid = it.paid - index)
                else it.copy(paid = it.paid + index)
            }
        )
    }

    private fun updateList(newList: List<Installment>) {
        _items.value = newList
        if (::store.isInitialized) {
            scope.launch { store.save(newList) }
        }
        if (::appContext.isInitialized) {
            ReminderScheduler.reschedule(appContext, TxRepository.items.value, newList)
        }
    }
}
