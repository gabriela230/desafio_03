package com.udb.aprendeapp.model

enum class UserRole {
    ESTUDIANTE,
    DOCENTE
}

data class User(
    val username: String,
    val role: UserRole
)
