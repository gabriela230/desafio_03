package com.udb.aprendeapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Resource(
    @SerializedName("id")
    val id: String = "",
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("tipo")
    val tipo: String,
    @SerializedName("enlace")
    val enlace: String,
    @SerializedName("imagen")
    val imagen: String,
    @SerializedName("calificacion")
    var calificacion: Double = 5.0,
    @SerializedName("numCalificaciones")
    var numCalificaciones: Int = 1
) : Serializable
