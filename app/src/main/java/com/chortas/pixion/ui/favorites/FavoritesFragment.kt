package com.chortas.pixion.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.Actor
import com.chortas.pixion.data.model.Favorite
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.data.model.Series
import com.chortas.pixion.data.repository.FavoritesRepository
import com.chortas.pixion.databinding.FragmentFavoritesBinding
import com.chortas.pixion.ui.detail.ActorDetailActivity
import com.chortas.pixion.ui.detail.MovieDetailFragment
import com.chortas.pixion.ui.detail.SeriesDetailActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavoritesFragment : Fragment() {
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var favoritesAdapter: FavoritesCardAdapter
    private lateinit var auth: FirebaseAuth
    private val allItems = mutableListOf<Any>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        favoritesRepository = FavoritesRepository()
        setupRecyclerView()
        setupClickListeners()
        checkAuthAndLoadFavorites()
    }

    private fun setupRecyclerView() {
        favoritesAdapter = FavoritesCardAdapter(
            items = allItems,
            onItemClick = { item ->
                when (item) {
                    is Movie -> {
                        findNavController().navigate(
                            R.id.action_favoritesFragment_to_movieDetailFragment,
                            Bundle().apply {
                                putInt("movie_id", item.id)
                            }
                        )
                    }
                    is Series -> {
                        val intent = Intent(requireContext(), SeriesDetailActivity::class.java)
                        intent.putExtra("series_id", item.id)
                        startActivity(intent)
                    }
                    is Actor -> {
                        val intent = Intent(requireContext(), ActorDetailActivity::class.java)
                        intent.putExtra("actor_id", item.id)
                        startActivity(intent)
                    }
                }
            },
            onRemoveClick = { item ->
                val contentId = when (item) {
                    is Movie -> item.id
                    is Series -> item.id
                    is Actor -> item.id
                    else -> return@FavoritesCardAdapter
                }
                
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        favoritesRepository.removeFromFavorites(contentId)
                        allItems.remove(item)
                        favoritesAdapter.notifyDataSetChanged()
                        if (allItems.isEmpty()) {
                            showEmptyState()
                        }
                        Toast.makeText(requireContext(), R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), getString(R.string.error_removing_favorite, e.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        binding.rvContent.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = favoritesAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun checkAuthAndLoadFavorites() {
        if (auth.currentUser == null) {
            Toast.makeText(requireContext(), getString(R.string.login_required), 
                Toast.LENGTH_LONG).show()
            return
        }
        loadFavorites()
    }

    private fun loadFavorites() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val favorites = favoritesRepository.getFavorites()
                if (favorites.isEmpty()) {
                    showEmptyState()
                    return@launch
                }

                val retrofit = Retrofit.Builder()
                    .baseUrl("https://api.themoviedb.org/3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val api = retrofit.create(TMDbApi::class.java)
                
                allItems.clear()

                for (favorite in favorites) {
                    try {
                        when (favorite.type) {
                            "movie" -> {
                                val response = withContext(Dispatchers.IO) {
                                    api.getMovieDetails(favorite.contentId)
                                }
                                
                                if (response.isSuccessful) {
                                    response.body()?.let { movieDetail ->
                                        allItems.add(Movie(
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
                                        allItems.add(Series(
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
                                        allItems.add(Actor(
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
                        Log.e("FavoritesFragment", "Error al cargar contenido ${favorite.contentId}", e)
                    }
                }

                // Obtener las fechas de adición para cada elemento
                val addedAtMap = mutableMapOf<Int, Long>()
                for (item in allItems) {
                    val id = when (item) {
                        is Movie -> item.id
                        is Series -> item.id
                        is Actor -> item.id
                        else -> null
                    }
                    if (id != null) {
                        try {
                            val addedAt = withContext(Dispatchers.IO) {
                                favoritesRepository.getFavoriteAddedAt(id)
                            }
                            addedAtMap[id] = addedAt
                        } catch (e: Exception) {
                            addedAtMap[id] = 0L
                        }
                    }
                }

                // Ordenar por fecha de adición (más recientes primero)
                allItems.sortByDescending { item ->
                    val id = when (item) {
                        is Movie -> item.id
                        is Series -> item.id
                        is Actor -> item.id
                        else -> null
                    }
                    addedAtMap[id] ?: 0L
                }
                
                favoritesAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
                
                if (allItems.isEmpty()) {
                    showEmptyState()
                }
            } catch (e: Exception) {
                Log.e("FavoritesFragment", "Error al cargar favoritos", e)
                Toast.makeText(requireContext(), getString(R.string.error_loading_favorites, e.message), Toast.LENGTH_LONG).show()
                showEmptyState()
            }
        }
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 