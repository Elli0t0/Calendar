package io.github.elli0t0.calendar.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FirstDay { SATURDAY, SUNDAY, MONDAY }
enum class MarkerStyle { VERY_MUTED, MUTED, BRIGHT, VERY_BRIGHT }
enum class CalendarViewMode { YEAR, MONTH, WEEK, DAY, SCHEDULE }

data class Settings(
    val firstDay: FirstDay = FirstDay.SATURDAY,
    val showGregorianInTiles: Boolean = true,
    val showGregorianInHeader: Boolean = true,
    val markerStyle: MarkerStyle = MarkerStyle.MUTED,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
)

/** Calendar settings, persisted in SharedPreferences and exposed as a flow. */
object AppSettings {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_FIRST_DAY = "first_day"
    private const val KEY_SHOW_GREGORIAN_IN_TILES = "show_gregorian_in_tiles"
    private const val KEY_SHOW_GREGORIAN_IN_HEADER = "show_gregorian_in_header"
    private const val KEY_MARKER_STYLE = "marker_style"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"
    private const val KEY_VIEW_MODE = "view_mode"

    private val _settings = MutableStateFlow(Settings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs != null) return
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _settings.value = read()
    }

    private fun read(): Settings {
        val p = prefs ?: return Settings()
        return Settings(
            firstDay = runCatching {
                FirstDay.valueOf(p.getString(KEY_FIRST_DAY, null) ?: "SATURDAY")
            }.getOrDefault(FirstDay.SATURDAY),
            showGregorianInTiles = p.getBoolean(KEY_SHOW_GREGORIAN_IN_TILES, true),
            showGregorianInHeader = p.getBoolean(KEY_SHOW_GREGORIAN_IN_HEADER, true),
            markerStyle = runCatching {
                MarkerStyle.valueOf(p.getString(KEY_MARKER_STYLE, null) ?: "MUTED")
            }.getOrDefault(MarkerStyle.MUTED),
            reminderHour = p.getInt(KEY_REMINDER_HOUR, 9),
            reminderMinute = p.getInt(KEY_REMINDER_MINUTE, 0),
            viewMode = runCatching {
                CalendarViewMode.valueOf(p.getString(KEY_VIEW_MODE, null) ?: "MONTH")
            }.getOrDefault(CalendarViewMode.MONTH),
        )
    }

    private fun update(transform: (Settings) -> Settings) {
        val next = transform(_settings.value)
        _settings.value = next
        prefs?.edit()?.apply {
            putString(KEY_FIRST_DAY, next.firstDay.name)
            putBoolean(KEY_SHOW_GREGORIAN_IN_TILES, next.showGregorianInTiles)
            putBoolean(KEY_SHOW_GREGORIAN_IN_HEADER, next.showGregorianInHeader)
            putString(KEY_MARKER_STYLE, next.markerStyle.name)
            putInt(KEY_REMINDER_HOUR, next.reminderHour)
            putInt(KEY_REMINDER_MINUTE, next.reminderMinute)
            putString(KEY_VIEW_MODE, next.viewMode.name)
        }?.apply()
    }

    fun setFirstDay(firstDay: FirstDay) = update { it.copy(firstDay = firstDay) }
    fun setShowGregorianInTiles(show: Boolean) =
        update { it.copy(showGregorianInTiles = show) }
    fun setShowGregorianInHeader(show: Boolean) =
        update { it.copy(showGregorianInHeader = show) }
    fun setMarkerStyle(style: MarkerStyle) = update { it.copy(markerStyle = style) }
    fun setReminderTime(hour: Int, minute: Int) =
        update { it.copy(reminderHour = hour, reminderMinute = minute) }
    fun setViewMode(mode: CalendarViewMode) = update { it.copy(viewMode = mode) }
}
