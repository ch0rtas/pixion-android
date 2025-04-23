package com.chortas.pixion.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.data.repository.FavoritesRepository
import com.chortas.pixion.databinding.ActivityFavoritesBinding
import com.chortas.pixion.ui.detail.MovieDetailActivity
import com.chortas.pixion.ui.main.MovieAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var auth: FirebaseAuth
    private val movies = mutableListOf<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        favoritesRepository = FavoritesRepository()
        setupRecyclerView()
        checkAuthAndLoadFavorites()
    }

    private fun checkAuthAndLoadFavorites() {
        if (auth.currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tus favoritos", 
                Toast.LENGTH_LONG).show()
            finish()
            return
        }
        loadFavorites()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(movies) { movie ->
            val intent = Intent(this, MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        binding.rvMovies.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            adapter = movieAdapter
        }
    }

    private fun loadFavorites() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val favoriteIds = favoritesRepository.getFavorites()
                if (favoriteIds.isEmpty()) {
                    showEmptyState()
                    return@launch
                }

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.themoviedb.org/3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(TMDbApi::class.java)
                
                movies.clear()
                for (movieId in favoriteIds) {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            api.getMovieDetails(movieId)
                        }
                        
                        if (response.isSuccessful) {
                            response.body()?.let { movieDetail ->
                                movies.add(Movie(
                                    id = movieDetail.id,
                                    title = movieDetail.title,
                                    overview = movieDetail.overview,
                                    posterPath = movieDetail.posterPath,
                                    backdropPath = movieDetail.backdropPath,
                                    releaseDate = movieDetail.releaseDate,
                                    voteAverage = movieDetail.voteAverage,
                                    voteCount = movieDetail.voteCount
                                ))
                            }
                        } else {
                            Log.e("FavoritesActivity", "Error al cargar detalles de la película $movieId: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("FavoritesActivity", "Error al cargar película $movieId", e)
                    }
                }

                if (movies.isEmpty()) {
                    showEmptyState()
                } else {
                    movieAdapter.notifyDataSetChanged()
                    binding.progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("FavoritesActivity", "Error al cargar favoritos", e)
                Toast.makeText(this@FavoritesActivity, 
                    "Error al cargar favoritos: ${e.message}", 
                    Toast.LENGTH_LONG).show()
                showEmptyState()
            }
        }
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
    }
} 