package com.chortas.pixion.data.model

data class MovieResponse(
    val results: List<Movie>,
    val page: Int,
    val totalPages: Int,
    val totalResults: Int
) 