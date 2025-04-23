package com.chortas.pixion.data.model

data class User(
    val uid: String,
    val email: String,
    val username: String,
    val createdAt: Long = System.currentTimeMillis()
) 