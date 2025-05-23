package com.chortas.pixion.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.ActorDetail
import com.chortas.pixion.data.repository.FavoritesRepository
import com.chortas.pixion.databinding.FragmentActorDetailBinding
import com.chortas.pixion.ui.main.MovieAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.navigation.fragment.findNavController

class ActorDetailFragment : Fragment() {
    private var _binding: FragmentActorDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var favoritesRepository: FavoritesRepository
    private var actorId: Int = 0
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actorId = arguments?.getInt("actor_id") ?: 0
        favoritesRepository = FavoritesRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        loadActorDetails()
        checkFavoriteStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            try {
                isFavorite = favoritesRepository.isFavorite(actorId)
                binding.btnFavorite.setImageResource(
                    if (isFavorite) android.R.drawable.star_big_on
                    else android.R.drawable.star_big_off
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.error_verifying_favorites, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(emptyList()) { movie ->
            findNavController().navigate(
                R.id.action_actorDetailFragment_to_movieDetailFragment,
                Bundle().apply {
                    putInt("movie_id", movie.id)
                }
            )
        }

        binding.rvKnownFor.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movieAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            try {
                if (isFavorite) {
                    favoritesRepository.removeFromFavorites(actorId)
                    binding.btnFavorite.setImageResource(android.R.drawable.star_big_off)
                    Toast.makeText(requireContext(), R.string.actor_removed_from_favorites, Toast.LENGTH_SHORT).show()
                } else {
                    favoritesRepository.addToFavorites(actorId, "actor")
                    binding.btnFavorite.setImageResource(android.R.drawable.star_big_on)
                    Toast.makeText(requireContext(), R.string.actor_added_to_favorites, Toast.LENGTH_SHORT).show()
                }
                isFavorite = !isFavorite
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.error_generic, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadActorDetails() {
        binding.progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TMDbApi::class.java)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getActorDetails(actorId)
                }
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    response.body()?.let { actor ->
                        displayActorDetails(actor)
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

    private fun displayActorDetails(actor: ActorDetail) {
        binding.tvName.text = actor.name
        binding.tvBiography.text = actor.biography ?: getString(R.string.biography_not_available)
        
        val formattedBirthday = actor.birthday?.let {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(it)
                outputFormat.format(date)
            } catch (e: Exception) {
                getString(R.string.birthday_not_available)
            }
        } ?: getString(R.string.birthday_not_available)
        
        binding.tvBirthday.text = formattedBirthday
        binding.tvPlaceOfBirth.text = actor.placeOfBirth ?: getString(R.string.place_of_birth_not_available)

        actor.profilePath?.let { profilePath ->
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500$profilePath")
                .error(R.drawable.ic_movie_placeholder)
                .into(binding.ivProfile)
        } ?: run {
            binding.ivProfile.setImageResource(R.drawable.ic_movie_placeholder)
        }

        actor.movieCredits.cast.let { movies ->
            movieAdapter.updateMovies(movies)
        }
    }

    companion object {
        fun newInstance(actorId: Int): ActorDetailFragment {
            val fragment = ActorDetailFragment()
            val args = Bundle()
            args.putInt("actor_id", actorId)
            fragment.arguments = args
            return fragment
        }
    }
} 