package com.udb.aprendeapp.model

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AprendeAppSession", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_FAVORITES = "favorites"
    }

    fun saveUserSession(username: String, role: UserRole) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role.name)
            apply()
        }
    }

    fun getUser(): User? {
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val roleStr = prefs.getString(KEY_ROLE, UserRole.ESTUDIANTE.name) ?: UserRole.ESTUDIANTE.name
        return User(username, UserRole.valueOf(roleStr))
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(resourceId: String): Boolean {
        val currentFavs = getFavorites().toMutableSet()
        val isFav: Boolean
        if (currentFavs.contains(resourceId)) {
            currentFavs.remove(resourceId)
            isFav = false
        } else {
            currentFavs.add(resourceId)
            isFav = true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, currentFavs).apply()
        return isFav
    }

    fun isFavorite(resourceId: String): Boolean {
        return getFavorites().contains(resourceId)
    }
}
