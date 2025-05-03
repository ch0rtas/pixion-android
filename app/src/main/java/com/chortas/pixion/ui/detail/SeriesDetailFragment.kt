package com.chortas.pixion.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.CastMember
import com.chortas.pixion.data.model.SeriesDetail
import com.chortas.pixion.data.model.Video
import com.chortas.pixion.data.repository.FavoritesRepository
import com.chortas.pixion.databinding.FragmentSeriesDetailBinding
import com.chortas.pixion.ui.detail.adapters.CastAdapter
import com.chortas.pixion.ui.detail.adapters.SeasonAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SeriesDetailFragment : Fragment() {
    private var _binding: FragmentSeriesDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var castAdapter: CastAdapter
    private lateinit var seasonAdapter: SeasonAdapter
    private var seriesId: Int = 0
    private var isFavorite: Boolean = false
    private var trailerKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        seriesId = arguments?.getInt("series_id") ?: 0
        auth = FirebaseAuth.getInstance()
        favoritesRepository = FavoritesRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeriesDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        loadSeriesDetails()
        loadSeriesVideos()
        checkFavoriteStatus()
        setupClickListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            try {
                isFavorite = favoritesRepository.isFavorite(seriesId)
                binding.btnFavorite.setImageResource(
                    if (isFavorite) android.R.drawable.star_big_on
                    else android.R.drawable.star_big_off
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.error_verifying_favorites, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerViews() {
        castAdapter = CastAdapter(emptyList()) { castMember ->
            val fragment = ActorDetailFragment.newInstance(castMember.id)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        seasonAdapter = SeasonAdapter(emptyList()) { season ->
            val bundle = Bundle().apply {
                putInt("series_id", seriesId)
                putInt("season_number", season.seasonNumber)
            }
            findNavController().navigate(R.id.action_seriesDetailFragment_to_seasonDetailFragment, bundle)
        }

        binding.rvCast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }

        binding.rvSeasons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = seasonAdapter
        }
    }

    private fun loadSeriesDetails() {
        binding.progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TMDbApi::class.java)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getSeriesDetails(seriesId)
                }
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    response.body()?.let { series ->
                        displaySeriesDetails(series)
                    } ?: run {
                        Toast.makeText(requireContext(), getString(R.string.error_loading_details), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.error_loading_details_code, response.code().toString()), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), getString(R.string.connection_error_with_message, e.message), 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSeriesVideos() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TMDbApi::class.java)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getSeriesVideos(seriesId)
                }
                
                if (response.isSuccessful) {
                    response.body()?.let { videoResponse ->
                        trailerKey = videoResponse.results.find { it.type == "Trailer" }?.key
                        binding.btnTrailer.visibility = if (trailerKey != null) View.VISIBLE else View.GONE
                    }
                }
            } catch (e: Exception) {
                binding.btnTrailer.visibility = View.GONE
            }
        }
    }

    private fun displaySeriesDetails(series: SeriesDetail) {
        binding.tvTitle.text = series.name
        binding.tvOverview.text = series.overview
        binding.tvFirstAirDate.text = series.getFormattedFirstAirDate()
        binding.tvRating.text = "${series.getFormattedRating()}/10"

        series.posterPath?.let { posterPath ->
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500$posterPath")
                .error(R.drawable.ic_movie_placeholder)
                .into(binding.ivPoster)
        } ?: run {
            binding.ivPoster.setImageResource(R.drawable.ic_movie_placeholder)
        }

        series.backdropPath?.let { backdropPath ->
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/original$backdropPath")
                .error(R.drawable.ic_movie_placeholder)
                .into(binding.ivBackdrop)
        } ?: run {
            binding.ivBackdrop.setImageResource(R.drawable.ic_movie_placeholder)
        }

        series.credits.cast.let { cast: List<CastMember> ->
            castAdapter.updateCast(cast)
        }

        series.seasons.let { seasons ->
            seasonAdapter.updateSeasons(seasons)
        }
    }

    private fun setupClickListeners() {
        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        binding.btnTrailer.setOnClickListener {
            trailerKey?.let { key ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))
                startActivity(intent)
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.no_trailer_available), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            try {
                if (isFavorite) {
                    favoritesRepository.removeFromFavorites(seriesId)
                    binding.btnFavorite.setImageResource(android.R.drawable.star_big_off)
                    Toast.makeText(requireContext(), R.string.series_removed_from_favorites, Toast.LENGTH_SHORT).show()
                } else {
                    favoritesRepository.addToFavorites(seriesId, "series")
                    binding.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                    Toast.makeText(requireContext(), R.string.series_added_to_favorites, Toast.LENGTH_SHORT).show()
                }
                isFavorite = !isFavorite
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.error_generic, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
} 