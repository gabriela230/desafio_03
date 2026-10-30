package com.example.aprendeapp.model

data class Resource(
    val id: Int = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val enlace: String = "",
    val imagen: String = "",
    val ratingTotal: Int = 0,
    val ratingCount: Int = 0
) {
    fun average(): Float = if (ratingCount == 0) 0f else ratingTotal.toFloat() / ratingCount
}
