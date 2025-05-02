package com.chortas.pixion.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.data.model.MovieResponse
import com.chortas.pixion.data.model.Series
import com.chortas.pixion.data.model.SeriesResponse
import com.chortas.pixion.databinding.FragmentMainBinding
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

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var seriesAdapter: SeriesAdapter
    private val movies = mutableListOf<Movie>()
    private val series = mutableListOf<Series>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        setupRecyclerViews()
        setupTabLayout()
        setupToolbarButtons()
        checkAuthAndLoadContent()
    }

    private fun setupRecyclerViews() {
        movieAdapter = MovieAdapter(movies) { movie ->
            val intent = Intent(requireContext(), MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        seriesAdapter = SeriesAdapter(series) { series ->
            val intent = Intent(requireContext(), SeriesDetailActivity::class.java)
            intent.putExtra("series_id", series.id)
            startActivity(intent)
        }

        binding.recyclerViewMovies.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = movieAdapter
        }

        binding.recyclerViewSeries.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = seriesAdapter
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.recyclerViewMovies.visibility = View.VISIBLE
                        binding.recyclerViewSeries.visibility = View.GONE
                    }
                    1 -> {
                        binding.recyclerViewMovies.visibility = View.GONE
                        binding.recyclerViewSeries.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupToolbarButtons() {
        binding.btnFavorites.setOnClickListener {
            val intent = Intent(requireContext(), FavoritesActivity::class.java)
            startActivity(intent)
        }

        binding.btnMenu.setOnClickListener {
            showMenuDialog()
        }
    }

    private fun showMenuDialog() {
        val menuItems = arrayOf("Enviar reporte de bug", "Cerrar sesión")
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setItems(menuItems) { _, which ->
                when (which) {
                    0 -> {
                        val emailIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@pixion.com"))
                            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Bug - Pixion")
                            putExtra(Intent.EXTRA_TEXT, "Por favor, describe el problema que encontraste:")
                        }
                        startActivity(Intent.createChooser(emailIntent, "Enviar email..."))
                    }
                    1 -> {
                        auth.signOut()
                        val intent = Intent(requireContext(), AuthActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
            .show()
    }

    private fun checkAuthAndLoadContent() {
        if (auth.currentUser == null) {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        } else {
            loadMovies()
            loadSeries()
        }
    }

    private fun loadMovies() {
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.themoviedb.org/3/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val api = retrofit.create(TMDbApi::class.java)
                    api.getPopularMovies()
                }

                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    movies.clear()
                    movies.addAll(movieResponse?.results ?: emptyList())
                    movieAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar películas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSeries() {
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val retrofit = Retrofit.Builder()
                        .baseUrl("https://api.themoviedb.org/3/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val api = retrofit.create(TMDbApi::class.java)
                    api.getPopularSeries()
                }

                if (response.isSuccessful) {
                    val seriesResponse = response.body()
                    series.clear()
                    series.addAll(seriesResponse?.results ?: emptyList())
                    seriesAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error al cargar series", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 