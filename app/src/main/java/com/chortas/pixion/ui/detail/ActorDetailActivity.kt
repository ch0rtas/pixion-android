package com.chortas.pixion.ui.detail

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.ActorDetail
import com.chortas.pixion.databinding.ActivityActorDetailBinding
import com.chortas.pixion.ui.main.MovieAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ActorDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActorDetailBinding
    private lateinit var movieAdapter: MovieAdapter
    private var actorId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActorDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        actorId = intent.getIntExtra("actor_id", 0)
        setupRecyclerView()
        loadActorDetails()
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(emptyList()) { movie ->
            val intent = android.content.Intent(this, MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        binding.rvMovies.apply {
            layoutManager = LinearLayoutManager(this@ActorDetailActivity)
            adapter = movieAdapter
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
                        Toast.makeText(this@ActorDetailActivity, getString(R.string.error_loading_details), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ActorDetailActivity, getString(R.string.error_loading_details_code, response.code().toString()), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@ActorDetailActivity, getString(R.string.connection_error_with_message, e.message), 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayActorDetails(actor: ActorDetail) {
        binding.tvName.text = actor.name
        binding.tvBiography.text = actor.biography ?: getString(R.string.biography_not_available)
        binding.tvBirthday.text = actor.birthday ?: getString(R.string.birthday_not_available)
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
} 