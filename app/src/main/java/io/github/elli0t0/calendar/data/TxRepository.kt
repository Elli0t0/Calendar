package io.github.elli0t0.calendar.data

import android.content.Context
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for transactions. Initialized once in
 * MainActivity.onCreate with the application context. Disk writes happen on
 * Dispatchers.IO; the StateFlow updates immediately on the calling thread.
 */
object TxRepository {

    private val _items = MutableStateFlow<List<Transaction>>(emptyList())
    val items: StateFlow<List<Transaction>> = _items.asStateFlow()

    private lateinit var store: TxStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        store = TxStore(File(context.filesDir, "transactions.json"))
        scope.launch {
            _items.value = store.load()
        }
    }

    fun add(
        type: TxType,
        title: String,
        amount: Long,
        date: LocalDate,
        note: String = "",
        colorIndex: Int = -1,
        repeat: Repeat = Repeat.NONE,
        repeatUntilEpochDay: Long? = null,
    ) {
        val nextId = (_items.value.maxOfOrNull { it.id } ?: 0L) + 1L
        updateList(
            _items.value + Transaction(
                nextId, type, title, amount, date.toEpochDay(), note, colorIndex,
                repeat, repeatUntilEpochDay
            )
        )
    }

    fun update(tx: Transaction) {
        updateList(_items.value.map { if (it.id == tx.id) tx else it })
    }

    fun delete(id: Long) {
        updateList(_items.value.filterNot { it.id == id })
    }

    private fun updateList(newList: List<Transaction>) {
        _items.value = newList
        if (::store.isInitialized) {
            scope.launch { store.save(newList) }
        }
    }
}
