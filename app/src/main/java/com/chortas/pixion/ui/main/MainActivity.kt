package com.chortas.pixion.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.data.model.MovieResponse
import com.chortas.pixion.data.model.Series
import com.chortas.pixion.data.model.SeriesResponse
import com.chortas.pixion.databinding.ActivityMainBinding
import com.chortas.pixion.ui.auth.AuthActivity
import com.chortas.pixion.ui.detail.MovieDetailActivity
import com.chortas.pixion.ui.detail.SeriesDetailActivity
import com.chortas.pixion.ui.favorites.FavoritesActivity
import com.google.android.material.tabs.TabLayout
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
    private lateinit var seriesAdapter: SeriesAdapter
    private val movies = mutableListOf<Movie>()
    private val series = mutableListOf<Series>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupRecyclerViews()
        setupTabLayout()
        checkAuthAndLoadContent()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
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

        binding.rvContent.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = movieAdapter
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        binding.rvContent.adapter = movieAdapter
                        loadMovies()
                    }
                    1 -> {
                        binding.rvContent.adapter = seriesAdapter
                        loadSeries()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun checkAuthAndLoadContent() {
        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }
        loadMovies()
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
                        movieAdapter.updateMovies(movies)
                    }
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.error_loading_movies), 
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, getString(R.string.connection_error), 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSeries() {
        binding.progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TMDbApi::class.java)
        
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getPopularSeries()
                }
                
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    response.body()?.results?.let { newSeries ->
                        series.clear()
                        series.addAll(newSeries)
                        seriesAdapter.updateSeries(series)
                    }
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.error_loading_movies), 
                        Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, getString(R.string.connection_error), 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        binding.btnMenu.setOnClickListener {
            showOptionsMenu()
        }
    }

    private fun showOptionsMenu() {
        val options = arrayOf(getString(R.string.report_bug), getString(R.string.logout))
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> reportBug()
                1 -> logout()
            }
        }
        builder.show()
    }

    private fun reportBug() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("chortas@protonmail.ch"))
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de bug - Pixion")
            putExtra(Intent.EXTRA_TEXT, "Por favor, describe el problema que has encontrado:")
        }
        startActivity(Intent.createChooser(emailIntent, "Enviar email"))
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
} 