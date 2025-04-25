package com.chortas.pixion.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.Favorite
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.data.model.Series
import com.chortas.pixion.data.model.Actor
import com.chortas.pixion.data.repository.FavoritesRepository
import com.chortas.pixion.databinding.ActivityFavoritesBinding
import com.chortas.pixion.ui.detail.MovieDetailActivity
import com.chortas.pixion.ui.detail.SeriesDetailActivity
import com.chortas.pixion.ui.detail.ActorDetailActivity
import com.chortas.pixion.ui.main.MovieAdapter
import com.chortas.pixion.ui.main.SeriesAdapter
import com.chortas.pixion.ui.main.ActorAdapter
import com.google.android.material.chip.Chip
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
    private lateinit var seriesAdapter: SeriesAdapter
    private lateinit var actorAdapter: ActorAdapter
    private lateinit var auth: FirebaseAuth
    private val movies = mutableListOf<Movie>()
    private val series = mutableListOf<Series>()
    private val actors = mutableListOf<Actor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        favoritesRepository = FavoritesRepository()
        setupRecyclerView()
        setupChipGroup()
        checkAuthAndLoadFavorites()
    }

    private fun setupChipGroup() {
        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAll -> loadFavorites()
                R.id.chipMovies -> loadFavorites("movie")
                R.id.chipSeries -> loadFavorites("series")
                R.id.chipActors -> loadFavorites("actor")
            }
        }
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(movies) { movie ->
            val intent = Intent(this, MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        seriesAdapter = SeriesAdapter(series) { series ->
            val intent = Intent(this, SeriesDetailActivity::class.java)
            intent.putExtra("series_id", series.id)
            startActivity(intent)
        }

        actorAdapter = ActorAdapter(actors) { actor ->
            val intent = Intent(this, ActorDetailActivity::class.java)
            intent.putExtra("actor_id", actor.id)
            startActivity(intent)
        }

        binding.rvContent.apply {
            layoutManager = GridLayoutManager(this@FavoritesActivity, 2)
            adapter = movieAdapter
        }
    }

    private fun checkAuthAndLoadFavorites() {
        if (auth.currentUser == null) {
            Toast.makeText(this, getString(R.string.login_required), 
                Toast.LENGTH_LONG).show()
            finish()
            return
        }
        loadFavorites()
    }

    private fun loadFavorites(type: String? = null) {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        lifecycleScope.launch {
            try {
                val favorites = favoritesRepository.getFavorites(type)
                if (favorites.isEmpty()) {
                    showEmptyState()
                    return@launch
                }

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.themoviedb.org/3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(TMDbApi::class.java)
                
                movies.clear()
                series.clear()
                actors.clear()

                for (favorite in favorites) {
                    try {
                        when (favorite.type) {
                            "movie" -> {
                                val response = withContext(Dispatchers.IO) {
                                    api.getMovieDetails(favorite.contentId)
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
                                }
                            }
                            "series" -> {
                                val response = withContext(Dispatchers.IO) {
                                    api.getSeriesDetails(favorite.contentId)
                                }
                                
                                if (response.isSuccessful) {
                                    response.body()?.let { seriesDetail ->
                                        series.add(Series(
                                            id = seriesDetail.id,
                                            name = seriesDetail.name,
                                            overview = seriesDetail.overview,
                                            posterPath = seriesDetail.posterPath,
                                            backdropPath = seriesDetail.backdropPath,
                                            firstAirDate = seriesDetail.firstAirDate,
                                            voteAverage = seriesDetail.voteAverage,
                                            voteCount = seriesDetail.voteCount
                                        ))
                                    }
                                }
                            }
                            "actor" -> {
                                val response = withContext(Dispatchers.IO) {
                                    api.getActorDetails(favorite.contentId)
                                }
                                
                                if (response.isSuccessful) {
                                    response.body()?.let { actorDetail ->
                                        actors.add(Actor(
                                            id = actorDetail.id,
                                            name = actorDetail.name,
                                            profilePath = actorDetail.profilePath,
                                            biography = actorDetail.biography,
                                            birthday = actorDetail.birthday,
                                            placeOfBirth = actorDetail.placeOfBirth
                                        ))
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FavoritesActivity", "Error al cargar contenido ${favorite.contentId}", e)
                    }
                }

                updateRecyclerView()
            } catch (e: Exception) {
                Log.e("FavoritesActivity", "Error al cargar favoritos", e)
                Toast.makeText(this@FavoritesActivity, getString(R.string.error_loading_favorites, e.message), Toast.LENGTH_LONG).show()
                showEmptyState()
            }
        }
    }

    private fun updateRecyclerView() {
        when {
            movies.isNotEmpty() -> {
                binding.rvContent.adapter = movieAdapter
                movieAdapter.notifyDataSetChanged()
            }
            series.isNotEmpty() -> {
                binding.rvContent.adapter = seriesAdapter
                seriesAdapter.notifyDataSetChanged()
            }
            actors.isNotEmpty() -> {
                binding.rvContent.adapter = actorAdapter
                actorAdapter.notifyDataSetChanged()
            }
            else -> showEmptyState()
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
    }
} 