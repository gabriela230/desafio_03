package com.udb.aprendeapp.controller

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.udb.aprendeapp.R
import com.udb.aprendeapp.databinding.ActivityRegisterBinding
import com.udb.aprendeapp.model.UserRole
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegisterSubmit.setOnClickListener {
            val username = binding.etRegUsername.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()
            val role = if (binding.rbTeacher.isChecked) UserRole.DOCENTE else UserRole.ESTUDIANTE

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!validatePassword(password)) {
                Toast.makeText(this, getString(R.string.password_rules), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("UserAuthStore", MODE_PRIVATE)
            if (prefs.contains("USER_$username")) {
                Toast.makeText(this, "El usuario ya está registrado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.edit().apply {
                putString("USER_$username", password)
                putString("ROLE_$username", role.name)
                apply()
            }

            Toast.makeText(this, "Registro exitoso. Ahora inicie sesión.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.tvLoginLink.setOnClickListener {
            finish()
        }
    }

    // Cumple con la validacion estricta requerida por la guia
    private fun validatePassword(password: String): Boolean {
        if (password.length < 12) return false
        val hasUpper = Pattern.compile("[A-Z]").matcher(password).find()
        val hasLower = Pattern.compile("[a-z]").matcher(password).find()
        val hasDigit = Pattern.compile("[0-9]").matcher(password).find()
        val hasSpecial = Pattern.compile("[!@#$%^&*]").matcher(password).find()
        return hasUpper && hasLower && hasDigit && hasSpecial
    }
}
