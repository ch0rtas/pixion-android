package com.chortas.pixion.data.model

data class Actor(
    val id: Int,
    val name: String,
    val profilePath: String?,
    val biography: String?,
    val birthday: String?,
    val placeOfBirth: String?
) 