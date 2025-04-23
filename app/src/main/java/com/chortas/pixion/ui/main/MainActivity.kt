package com.chortas.pixion.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.data.model.MovieResponse
import com.chortas.pixion.databinding.ActivityMainBinding
import com.chortas.pixion.ui.detail.MovieDetailActivity
import com.chortas.pixion.ui.favorites.FavoritesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var movieAdapter: MovieAdapter
    private val movies = mutableListOf<Movie>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupRecyclerView()
        loadMovies()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(movies) { movie ->
            val intent = Intent(this, MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        binding.rvMovies.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = movieAdapter
        }
    }

    private fun loadMovies() {
        binding.progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TMDbApi::class.java)
        
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getPopularMovies()
                }
                
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.results?.let { newMovies ->
                        movies.clear()
                        movies.addAll(newMovies)
                        movieAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Error al cargar películas", 
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Error de conexión", 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            finish()
        }
    }
} 