package com.chortas.pixion.data.model

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    val id: Int,
    val results: List<Video>
)

data class Video(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val size: Int,
    val type: String,
    @SerializedName("official")
    val isOfficial: Boolean
) 