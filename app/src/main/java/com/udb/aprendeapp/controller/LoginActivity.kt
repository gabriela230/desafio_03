package com.udb.aprendeapp.controller

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.udb.aprendeapp.databinding.ActivityLoginBinding
import com.udb.aprendeapp.model.SessionManager
import com.udb.aprendeapp.model.UserRole

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (sessionManager.getUser() != null) {
            navigateToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Simulacion de autenticacion local con credenciales
            val prefs = getSharedPreferences("UserAuthStore", MODE_PRIVATE)
            val storedPass = prefs.getString("USER_$username", null)
            val storedRole = prefs.getString("ROLE_$username", UserRole.ESTUDIANTE.name)

            if (storedPass != null && storedPass == password) {
                val role = UserRole.valueOf(storedRole!!)
                sessionManager.saveUserSession(username, role)
                Toast.makeText(this, "¡Bienvenido a AprendeApp!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            } else {
                Toast.makeText(this, "Usuario o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
