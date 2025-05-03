package com.chortas.pixion.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.Cast
import com.chortas.pixion.data.model.MovieDetail
import com.chortas.pixion.data.repository.FavoritesRepository
import com.chortas.pixion.databinding.FragmentMovieDetailBinding
import com.chortas.pixion.ui.detail.adapters.CastAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MovieDetailFragment : Fragment() {
    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var favoritesRepository: FavoritesRepository
    private var movieId: Int = 0
    private var isFavorite: Boolean = false
    private var trailerKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        movieId = arguments?.getInt("movie_id") ?: 0
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        favoritesRepository = FavoritesRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        Toast.makeText(context, R.string.movie_removed_from_favorites, Toast.LENGTH_SHORT).show()
                    } else {
                        favoritesRepository.addToFavorites(movieId, "movie")
                        binding.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                        Toast.makeText(context, R.string.movie_added_to_favorites, Toast.LENGTH_SHORT).show()
                    }
                    isFavorite = !isFavorite
                } catch (e: Exception) {
                    Toast.makeText(context, getString(R.string.error_generic, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnTrailer.setOnClickListener {
            trailerKey?.let { key ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$key"))
                startActivity(intent)
            } ?: run {
                Toast.makeText(context, getString(R.string.no_trailer_available), Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, getString(R.string.error_verifying_favorites, e.message), Toast.LENGTH_SHORT).show()
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
                        Log.d("MovieDetailFragment", "Movie details: $movie")
                        displayMovieDetails(movie)
                    } ?: run {
                        Toast.makeText(context, getString(R.string.error_loading_details), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, getString(R.string.error_loading_details_code, response.code().toString()), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.connection_error_with_message, e.message), 
                    Toast.LENGTH_SHORT).show()
                Log.e("MovieDetailFragment", "Error loading movie details", e)
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
            Log.w("MovieDetailFragment", "Poster URL no disponible")
            null
        }

        val backdropUrl = movie.backdropPath?.let {
            "https://image.tmdb.org/t/p/original$it"
        } ?: run {
            Log.w("MovieDetailFragment", "Backdrop URL no disponible")
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
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rvCast.adapter = CastAdapter(cast) { castMember ->
                findNavController().navigate(
                    R.id.action_movieDetailFragment_to_actorDetailFragment,
                    Bundle().apply {
                        putInt("actor_id", castMember.id)
                    }
                )
            }
        } ?: run {
            Log.w("MovieDetailFragment", "El reparto no está disponible")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(movieId: Int): MovieDetailFragment {
            val fragment = MovieDetailFragment()
            val args = Bundle()
            args.putInt("movie_id", movieId)
            fragment.arguments = args
            return fragment
        }
    }
} 