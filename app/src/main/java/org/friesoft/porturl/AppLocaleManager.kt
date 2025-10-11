package org.friesoft.porturl

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import android.os.LocaleList
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val SYSTEM_DEFAULT = "system"

data class Language(
    val code: String,
    @StringRes val displayLanguage: Int
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(languageCode)
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
        }
    }

    fun getLanguageCode(): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                ?.applicationLocales
                ?.takeUnless { it.isEmpty }
                ?.get(0)
        } else {
            AppCompatDelegate.getApplicationLocales()
                .takeIf { !it.isEmpty }
                ?.get(0)
        }
        return locale?.language ?: SYSTEM_DEFAULT
    }

}
