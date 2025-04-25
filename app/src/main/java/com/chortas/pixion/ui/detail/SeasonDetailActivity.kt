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
import com.chortas.pixion.data.model.SeasonDetail
import com.chortas.pixion.databinding.ActivitySeasonDetailBinding
import com.chortas.pixion.ui.detail.adapters.EpisodeAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SeasonDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeasonDetailBinding
    private lateinit var episodeAdapter: EpisodeAdapter
    private var seriesId: Int = 0
    private var seasonNumber: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeasonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        seriesId = intent.getIntExtra("series_id", 0)
        seasonNumber = intent.getIntExtra("season_number", 0)
        setupRecyclerView()
        loadSeasonDetails()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        episodeAdapter = EpisodeAdapter(emptyList())

        binding.rvEpisodes.apply {
            layoutManager = LinearLayoutManager(this@SeasonDetailActivity)
            adapter = episodeAdapter
        }
    }

    private fun loadSeasonDetails() {
        binding.progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(TMDbApi::class.java)
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    api.getSeasonDetails(seriesId, seasonNumber)
                }
                
                binding.progressBar.visibility = View.GONE
                
                if (response.isSuccessful) {
                    response.body()?.let { season ->
                        displaySeasonDetails(season)
                    } ?: run {
                        Toast.makeText(this@SeasonDetailActivity, getString(R.string.error_loading_details), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SeasonDetailActivity, getString(R.string.error_loading_details_code, response.code().toString()), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SeasonDetailActivity, getString(R.string.connection_error_with_message, e.message), 
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySeasonDetails(season: SeasonDetail) {
        binding.tvTitle.text = season.name
        binding.tvOverview.text = season.overview ?: getString(R.string.description_not_available)

        season.posterPath?.let { posterPath ->
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500$posterPath")
                .error(R.drawable.ic_movie_placeholder)
                .into(binding.ivPoster)
        } ?: run {
            binding.ivPoster.setImageResource(R.drawable.ic_movie_placeholder)
        }

        episodeAdapter.updateEpisodes(season.episodes)
    }
} 