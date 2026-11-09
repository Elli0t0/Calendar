package io.github.elli0t0.calendar.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `pref FA maps to FA and locale fa`() {
        assertEquals(AppLanguage.FA, languageFromPref("FA"))
        assertEquals("fa", AppLanguage.FA.localeTag())
    }

    @Test
    fun `pref EN maps to EN and locale en`() {
        assertEquals(AppLanguage.EN, languageFromPref("EN"))
        assertEquals("en", AppLanguage.EN.localeTag())
    }

    @Test
    fun `missing pref defaults to FA`() {
        assertEquals(AppLanguage.FA, languageFromPref(null))
    }

    @Test
    fun `unknown pref value defaults to FA`() {
        assertEquals(AppLanguage.FA, languageFromPref("xx"))
        assertEquals(AppLanguage.FA, languageFromPref(""))
    }

    @Test
    fun `settings default language is FA`() {
        assertEquals(AppLanguage.FA, Settings().language)
    }
}
