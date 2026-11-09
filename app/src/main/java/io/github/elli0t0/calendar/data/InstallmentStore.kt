package io.github.elli0t0.calendar.data

import java.io.File
import org.json.JSONArray

/**
 * Reads/writes the installment list as a JSON array in [file]
 * (filesDir/installments.json). Writes go to a temp file first and are then
 * renamed over the real file, so a crash mid-write never corrupts data.
 * A missing or broken file loads as an empty list.
 */
class InstallmentStore(private val file: File) {

    fun load(): List<Installment> {
        return try {
            if (!file.exists()) return emptyList()
            val text = file.readText()
            if (text.isBlank()) return emptyList()
            val array = JSONArray(text)
            List(array.length()) { i -> array.getJSONObject(i).toInstallment() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(list: List<Installment>) {
        try {
            val array = JSONArray()
            list.forEach { array.put(it.toJson()) }
            val parent = file.parentFile
            if (parent == null) {
                file.writeText(array.toString())
                return
            }
            val tmp = File(parent, file.name + ".tmp")
            tmp.writeText(array.toString())
            if (!tmp.renameTo(file)) {
                file.writeText(array.toString())
                tmp.delete()
            }
        } catch (_: Exception) {
            // Disk failures must never crash the app; the in-memory list stays live.
        }
    }
}
