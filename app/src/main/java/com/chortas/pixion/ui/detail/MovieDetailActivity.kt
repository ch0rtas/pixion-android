package com.chortas.pixion.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.MovieDetail
import com.chortas.pixion.data.model.Video
import com.chortas.pixion.data.repository.FavoritesRepository
import com.chortas.pixion.databinding.ActivityMovieDetailBinding
import com.chortas.pixion.ui.detail.adapters.CastAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var favoritesRepository: FavoritesRepository
    private var movieId: Int = 0
    private var isFavorite: Boolean = false
    private var trailerKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        favoritesRepository = FavoritesRepository()
        movieId = intent.getIntExtra("movie_id", 0)

        setupClickListeners()
        checkFavoriteStatus()
        loadMovieDetails()
        loadMovieVideos()
    }

    private fun setupClickListeners() {
        binding.btnFavorite.setOnClickListener {
            lifecycleScope.launch {
                try {
                    if (isFavorite) {
                        favoritesRepository.removeFromFavorites(movieId)
                        binding.btnFavorite.setImageResource(android.R.drawable.star_big_off)
                        Toast.makeText(this@MovieDetailActivity, R.string.movie_removed_from_favorites, Toast.LENGTH_SHORT).show()
                    } else {
                        favoritesRepository.addToFavorites(movieId, "movie")
                        binding.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                        Toast.makeText(this@MovieDetailActivity, R.string.movie_added_to_favorites, Toast.LENGTH_SHORT).show()
                    }
                    isFavorite = !isFavorite
                } catch (e: Exception) {
                    Toast.makeText(this@MovieDetailActivity, getString(R.string.error_generic, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnTrailer.setOnClickListener {
            trailerKey?.let { key ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, getString(R.string.no_trailer_available), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            try {
                isFavorite = favoritesRepository.isFavorite(movieId)
                binding.btnFavorite.setImageResource(
                    if (isFavorite) android.R.drawable.star_big_on
                    else android.R.drawable.star_big_off
                )
            } catch (e: Exception) {
                Toast.makeText(this@MovieDetailActivity, getString(R.string.error_verifying_favorites, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMovieDetails() {
        binding.progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TMDbApi::class.java)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getMovieDetails(movieId)
                }
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    response.body()?.let { movie ->
                        Log.d("MovieDetailActivity", "Movie details: $movie")
                        displayMovieDetails(movie)
                    } ?: run {
                        Toast.makeText(this@MovieDetailActivity, getString(R.string.error_loading_details), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MovieDetailActivity, getString(R.string.error_loading_details_code, response.code().toString()), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MovieDetailActivity, getString(R.string.connection_error_with_message, e.message), 
                    Toast.LENGTH_SHORT).show()
                Log.e("MovieDetailActivity", "Error loading movie details", e)
            }
        }
    }

    private fun loadMovieVideos() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TMDbApi::class.java)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getMovieVideos(movieId)
                }
                
                if (response.isSuccessful) {
                    response.body()?.results?.let { videos ->
                        // Buscar el primer trailer oficial
                        trailerKey = videos.find { video ->
                            video.type == "Trailer" && video.site == "YouTube" && video.isOfficial
                        }?.key
                    }
                }
            } catch (e: Exception) {
                // No mostramos error al usuario ya que el trailer es opcional
            }
        }
    }

    private fun displayMovieDetails(movie: MovieDetail) {
        // Título y descripción
        binding.tvTitle.text = movie.title ?: getString(R.string.title_not_available)
        binding.tvOverview.text = movie.overview ?: getString(R.string.description_not_available)
        
        // Fecha de estreno formateada
        binding.tvReleaseDate.text = movie.getFormattedReleaseDate()
        
        // Puntuación formateada
        binding.tvRating.text = "${movie.getFormattedRating()}/10"
        
        // Duración formateada
        binding.tvRuntime.text = movie.getFormattedRuntime()

        // URLs de imágenes con validación de null
        val posterUrl = movie.posterPath?.let {
            "https://image.tmdb.org/t/p/w500$it"
        } ?: run {
            Log.w("MovieDetailActivity", "Poster URL no disponible")
            null
        }

        val backdropUrl = movie.backdropPath?.let {
            "https://image.tmdb.org/t/p/original$it"
        } ?: run {
            Log.w("MovieDetailActivity", "Backdrop URL no disponible")
            null
        }

        // Cargar imágenes con Glide
        posterUrl?.let {
            Glide.with(this)
                .load(it)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_movie_placeholder)
                .into(binding.ivPoster)
        } ?: binding.ivPoster.setImageResource(R.drawable.ic_movie_placeholder)

        backdropUrl?.let {
            Glide.with(this)
                .load(it)
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_movie_placeholder)
                .into(binding.ivBackdrop)
        } ?: binding.ivBackdrop.setImageResource(R.drawable.ic_movie_placeholder)

        // Configurar RecyclerView para el reparto
        movie.credits?.cast?.let { cast ->
            binding.rvCast.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rvCast.adapter = CastAdapter(cast) { castMember ->
                val intent = Intent(this, ActorDetailActivity::class.java)
                intent.putExtra("actor_id", castMember.id)
                startActivity(intent)
            }
        } ?: run {
            Log.w("MovieDetailActivity", "El reparto no está disponible")
        }
    }
} 