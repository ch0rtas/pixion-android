package com.chortas.pixion.data.model

import com.google.gson.annotations.SerializedName

data class ActorDetail(
    val id: Int,
    val name: String,
    val biography: String?,
    @SerializedName("profile_path")
    val profilePath: String?,
    @SerializedName("birthday")
    val birthday: String?,
    @SerializedName("place_of_birth")
    val placeOfBirth: String?,
    @SerializedName("movie_credits")
    val movieCredits: MovieCredits
)

data class MovieCredits(
    val cast: List<Movie>
) 