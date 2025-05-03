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
import com.chortas.pixion.ui.detail.MovieDetailFragment
import com.chortas.pixion.ui.detail.SeriesDetailFragment
import com.chortas.pixion.ui.favorites.FavoritesFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.navigation.fragment.findNavController

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var seriesAdapter: SeriesAdapter
    private val movies = mutableListOf<Movie>()
    private val series = mutableListOf<Series>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var selectedTabPosition = 0
    private var isFirstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            selectedTabPosition = savedInstanceState.getInt("selected_tab_position", 0)
        }
    }

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

        if (isFirstLoad) {
            isFirstLoad = false
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(selectedTabPosition))
            updateTabVisibility(selectedTabPosition)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(selectedTabPosition))
        updateTabVisibility(selectedTabPosition)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selected_tab_position", selectedTabPosition)
    }

    private fun updateTabVisibility(position: Int) {
        when (position) {
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

    private fun setupRecyclerViews() {
        movieAdapter = MovieAdapter(movies) { movie ->
            findNavController().navigate(
                R.id.action_mainFragment_to_movieDetailFragment,
                Bundle().apply {
                    putInt("movie_id", movie.id)
                }
            )
        }

        seriesAdapter = SeriesAdapter(series) { series ->
            findNavController().navigate(
                R.id.action_mainFragment_to_seriesDetailFragment,
                Bundle().apply {
                    putInt("series_id", series.id)
                }
            )
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
                selectedTabPosition = tab?.position ?: 0
                updateTabVisibility(selectedTabPosition)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupToolbarButtons() {
        binding.btnFavorites.setOnClickListener {
            if (auth.currentUser == null) {
                findNavController().navigate(R.id.loginFragment)
            } else {
                findNavController().navigate(R.id.action_mainFragment_to_favoritesFragment)
            }
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
                        findNavController().navigate(R.id.loginFragment)
                    }
                }
            }
            .show()
    }

    private fun checkAuthAndLoadContent() {
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
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