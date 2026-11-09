package io.github.elli0t0.calendar.data

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * The context wrapped in the app language's locale, with the matching
 * layout direction (RTL for fa, LTR for en). Works on minSdk 26 without
 * AppCompat. Also sets the JVM default locale, so isPersianLocale() and
 * the locale-aware digit/separator helpers follow the app language.
 */
fun Context.withAppLocale(lang: AppLanguage): Context {
    val locale = Locale(lang.localeTag())
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}
