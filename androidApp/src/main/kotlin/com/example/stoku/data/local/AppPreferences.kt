package com.example.stoku.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.stoku.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "stoku_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val LOGGED_IN_USER_ROLE = stringPreferencesKey("logged_in_user_role")
        val GLOBAL_LOW_STOCK_THRESHOLD = intPreferencesKey("global_low_stock_threshold")
    }

    val loggedInUserId: Flow<Long?> = context.dataStore.data.map { it[Keys.LOGGED_IN_USER_ID] }

    val loggedInUserRole: Flow<UserRole?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN_USER_ROLE]?.let(UserRole::fromValue)
    }

    val globalLowStockThreshold: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.GLOBAL_LOW_STOCK_THRESHOLD] ?: DEFAULT_LOW_STOCK_THRESHOLD
    }

    suspend fun setLoggedInUser(userId: Long, role: UserRole) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOGGED_IN_USER_ID] = userId
            prefs[Keys.LOGGED_IN_USER_ROLE] = role.value
        }
    }

    suspend fun clearLoggedInUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.LOGGED_IN_USER_ID)
            prefs.remove(Keys.LOGGED_IN_USER_ROLE)
        }
    }

    suspend fun setGlobalLowStockThreshold(threshold: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.GLOBAL_LOW_STOCK_THRESHOLD] = threshold }
    }

    companion object {
        const val DEFAULT_LOW_STOCK_THRESHOLD = 5
    }
}
