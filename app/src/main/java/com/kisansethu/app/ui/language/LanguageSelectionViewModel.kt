package com.kisansethu.app.ui.language

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.Language
import com.kisansethu.app.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class LanguageSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val _selectedLanguage = MutableStateFlow<Language?>(null)
    val selectedLanguage: StateFlow<Language?> = _selectedLanguage.asStateFlow()

    fun selectLanguage(language: Language) {
        _selectedLanguage.value = language
    }

    fun confirmLanguageSelection(onSuccess: () -> Unit) {
        val language = _selectedLanguage.value ?: return
        viewModelScope.launch {
            userPreferencesRepository.saveSelectedLanguage(language.code)
            applyLocale(language.code)
            onSuccess()
        }
    }

    private fun applyLocale(languageCode: String) {
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
        Locale.setDefault(Locale.forLanguageTag(languageCode))
    }
}
