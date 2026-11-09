package io.github.elli0t0.calendar.data

enum class Remind { NONE, ON_DAY, ONE_DAY_BEFORE, TWO_DAYS_BEFORE, ONE_WEEK_BEFORE }

/** Days before the due date the reminder fires, or null when it never fires. */
fun Remind.offsetDays(): Int? = when (this) {
    Remind.NONE -> null
    Remind.ON_DAY -> 0
    Remind.ONE_DAY_BEFORE -> 1
    Remind.TWO_DAYS_BEFORE -> 2
    Remind.ONE_WEEK_BEFORE -> 7
}
