package com.example.aprendeapp.controller

import android.content.Context
import com.example.aprendeapp.model.User
import java.security.MessageDigest

class AuthController(context: Context) {
    private val prefs = context.getSharedPreferences("aprende_users", Context.MODE_PRIVATE)
    private fun hash(value: String) = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray()).joinToString("") { "%02x".format(it) }

    fun validPassword(p: String): Boolean =
        p.length >= 12 && p.any(Char::isUpperCase) && p.any(Char::isLowerCase) &&
            p.any(Char::isDigit) && p.any { "!@#\$%^&*".contains(it) }

    fun register(email: String, password: String, role: String): String? {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "email"
        if (!validPassword(password)) return "password"
        if (prefs.contains(email.lowercase())) return "exists"
        prefs.edit().putString(email.lowercase(), "${hash(password)}|$role").apply()
        return null
    }

    fun login(email: String, password: String): User? {
        val data = prefs.getString(email.lowercase(), null) ?: return null
        val parts = data.split("|")
        if (parts.size != 2 || parts[0] != hash(password)) return null
        return User(email, parts[0], parts[1])
    }
}
