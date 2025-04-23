package com.chortas.pixion.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Favorite(
    val movieId: Int = 0,
    val userId: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this(0, "", System.currentTimeMillis())
} 