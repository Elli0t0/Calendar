package io.github.elli0t0.calendar.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsTest {

    @Test
    fun `wrap event titles defaults to off and can be turned on`() {
        assertFalse(Settings().wrapEventTitles)
        assertTrue(Settings().copy(wrapEventTitles = true).wrapEventTitles)
    }
}
