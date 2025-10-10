package org.friesoft.porturl

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val SYSTEM_DEFAULT = "system"

data class Language(
    val code: String,
    @StringRes val displayLanguageResId: Int
)

val appLanguages = listOf(
    Language(SYSTEM_DEFAULT, R.string.language_system_default),
    Language("en", R.string.language_en),
    Language("de", R.string.language_de)
)

@Singleton
class AppLocaleManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun changeLanguage(languageCode: String) {
        val localeList = if (languageCode == SYSTEM_DEFAULT) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageCode)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun getLanguageCode(): String {
        return AppCompatDelegate.getApplicationLocales().takeIf { !it.isEmpty }?.get(0)?.language
            ?: SYSTEM_DEFAULT
    }
}
