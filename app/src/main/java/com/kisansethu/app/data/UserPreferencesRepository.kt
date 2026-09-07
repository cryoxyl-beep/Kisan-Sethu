package com.kisansethu.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val LOGGED_IN_FARMER_ID = stringPreferencesKey("logged_in_farmer_id")
        val LOGGED_IN_FARMER_NAME = stringPreferencesKey("logged_in_farmer_name")
    }

    val selectedLanguage: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE]
        }

    val isDarkTheme: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME]
        }

    val loggedInFarmerId: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LOGGED_IN_FARMER_ID] }

    val loggedInFarmerName: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LOGGED_IN_FARMER_NAME] }

    suspend fun saveSelectedLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = languageCode
        }
    }

    suspend fun saveDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_THEME] = isDark
        }
    }

    suspend fun saveLoggedInFarmer(farmerId: String, farmerName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOGGED_IN_FARMER_ID] = farmerId
            preferences[PreferencesKeys.LOGGED_IN_FARMER_NAME] = farmerName
        }
    }

    suspend fun clearLoggedInFarmer() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LOGGED_IN_FARMER_ID)
            preferences.remove(PreferencesKeys.LOGGED_IN_FARMER_NAME)
        }
    }
}
