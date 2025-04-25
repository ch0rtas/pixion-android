package com.chortas.pixion.data.model

import com.google.gson.annotations.SerializedName

data class Series(
    val id: Int,
    val name: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int
) {
    fun getFormattedFirstAirDate(): String {
        return firstAirDate?.let {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date)
            } catch (e: Exception) {
                it
            }
        } ?: "Fecha no disponible"
    }

    fun getFormattedRating(): String {
        return String.format(java.util.Locale.getDefault(), "%.1f", voteAverage)
    }
}

data class SeriesDetail(
    val id: Int,
    val name: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("first_air_date")
    val firstAirDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    val seasons: List<Season>,
    val credits: Credits
) {
    fun getFormattedFirstAirDate(): String {
        return firstAirDate?.let {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date)
            } catch (e: Exception) {
                it
            }
        } ?: "Fecha no disponible"
    }

    fun getFormattedRating(): String {
        return String.format(java.util.Locale.getDefault(), "%.1f", voteAverage)
    }
}

data class Season(
    val id: Int,
    val name: String,
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("season_number")
    val seasonNumber: Int,
    @SerializedName("episode_count")
    val episodeCount: Int,
    @SerializedName("air_date")
    val airDate: String?
) {
    fun getFormattedAirDate(): String {
        return airDate?.let {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date)
            } catch (e: Exception) {
                it
            }
        } ?: "Fecha no disponible"
    }
}

data class SeasonDetail(
    val id: Int,
    val name: String,
    val overview: String?,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("season_number")
    val seasonNumber: Int,
    val episodes: List<Episode>
)

data class Episode(
    val id: Int,
    val name: String,
    val overview: String?,
    @SerializedName("still_path")
    val stillPath: String?,
    @SerializedName("episode_number")
    val episodeNumber: Int,
    @SerializedName("air_date")
    val airDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double
) {
    fun getFormattedAirDate(): String {
        return airDate?.let {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date)
            } catch (e: Exception) {
                it
            }
        } ?: "Fecha no disponible"
    }

    fun getFormattedRating(): String {
        return String.format(java.util.Locale.getDefault(), "%.1f", voteAverage)
    }
} 