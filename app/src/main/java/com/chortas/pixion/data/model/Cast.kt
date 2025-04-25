package com.chortas.pixion.data.model

import com.google.gson.annotations.SerializedName

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path")
    val profilePath: String?
) 