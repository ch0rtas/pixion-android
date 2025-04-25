package com.chortas.pixion.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Favorite(
    val contentId: Int = 0,
    val userId: String = "",
    val type: String = "movie", // movie, series, actor
    val addedAt: Long = System.currentTimeMillis()
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this(0, "", "movie", System.currentTimeMillis())
} 