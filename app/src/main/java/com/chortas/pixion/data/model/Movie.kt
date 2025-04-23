package com.chortas.pixion.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int
) {
    fun getFormattedReleaseDate(): String {
        return releaseDate?.let {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date)
            } catch (e: Exception) {
                it
            }
        } ?: "Fecha no disponible"
    }

    fun getFormattedRating(): String {
        return String.format(Locale.getDefault(), "%.1f", voteAverage)
    }
}

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    val runtime: Int?,
    val genres: List<Genre>,
    val credits: Credits
) {
    fun getFormattedReleaseDate(): String {
        return releaseDate?.let {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date)
            } catch (e: Exception) {
                it
            }
        } ?: "Fecha no disponible"
    }

    fun getFormattedRating(): String {
        return String.format(Locale.getDefault(), "%.1f", voteAverage)
    }

    fun getFormattedRuntime(): String {
        return runtime?.let {
            val hours = it / 60
            val minutes = it % 60
            when {
                hours > 0 -> "$hours h $minutes min"
                else -> "$minutes min"
            }
        } ?: "Duración no disponible"
    }
}

data class Genre(
    val id: Int,
    val name: String
)

data class Credits(
    val cast: List<CastMember>
)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path")
    val profilePath: String?
) 