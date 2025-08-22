
package com.example.folivix.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "folivix_settings")

@Singleton
class AppPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val SERVER_IP_KEY = stringPreferencesKey("server_ip")
        private val LAST_USER_ID_KEY = stringPreferencesKey("last_user_id")
        private const val DEFAULT_IP = "192.168.1.71"
    }

    val serverIp: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_IP_KEY] ?: DEFAULT_IP
    }

    suspend fun saveServerIp(ip: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_IP_KEY] = ip
        }
    }

    val lastUserId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_USER_ID_KEY]
    }

    suspend fun saveLastUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_USER_ID_KEY] = userId
        }
    }

    suspend fun clearLastUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(LAST_USER_ID_KEY)
        }
    }
}