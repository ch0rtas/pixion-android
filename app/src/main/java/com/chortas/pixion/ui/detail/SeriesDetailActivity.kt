package com.chortas.pixion.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.api.TMDbApi
import com.chortas.pixion.data.model.CastMember
import com.chortas.pixion.data.model.SeriesDetail
import com.chortas.pixion.data.repository.FavoritesRepository
import com.chortas.pixion.databinding.ActivitySeriesDetailBinding
import com.chortas.pixion.ui.detail.adapters.CastAdapter
import com.chortas.pixion.ui.detail.adapters.SeasonAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SeriesDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeriesDetailBinding
    private lateinit var castAdapter: CastAdapter
    private lateinit var seasonAdapter: SeasonAdapter
    private lateinit var favoritesRepository: FavoritesRepository
    private var seriesId: Int = 0
    private var isFavorite: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        seriesId = intent.getIntExtra("series_id", 0)
        favoritesRepository = FavoritesRepository()
        setupRecyclerViews()
        loadSeriesDetails()
        checkFavoriteStatus()
        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun checkFavoriteStatus() {
        lifecycleScope.launch {
            try {
                isFavorite = favoritesRepository.isFavorite(seriesId)
                updateFavoriteButton()
            } catch (e: Exception) {
                Toast.makeText(this@SeriesDetailActivity, getString(R.string.error_verifying_favorites, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerViews() {
        castAdapter = CastAdapter(emptyList()) { castMember ->
            val intent = Intent(this, ActorDetailActivity::class.java)
            intent.putExtra("actor_id", castMember.id)
            startActivity(intent)
        }
        seasonAdapter = SeasonAdapter(emptyList()) { season ->
            val intent = Intent(this, SeasonDetailActivity::class.java)
            intent.putExtra("series_id", seriesId)
            intent.putExtra("season_number", season.seasonNumber)
            startActivity(intent)
        }

        binding.rvCast.apply {
            layoutManager = LinearLayoutManager(this@SeriesDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }

        binding.rvSeasons.apply {
            layoutManager = LinearLayoutManager(this@SeriesDetailActivity)
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
                        Toast.makeText(this@SeriesDetailActivity, getString(R.string.error_loading_details), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SeriesDetailActivity, getString(R.string.error_loading_details_code, response.code().toString()), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SeriesDetailActivity, getString(R.string.connection_error_with_message, e.message), 
                    Toast.LENGTH_SHORT).show()
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
            lifecycleScope.launch {
                try {
                    if (isFavorite) {
                        favoritesRepository.removeFromFavorites(seriesId)
                        Toast.makeText(this@SeriesDetailActivity, getString(R.string.series_removed_from_favorites), Toast.LENGTH_SHORT).show()
                    } else {
                        favoritesRepository.addToFavorites(seriesId, "series")
                        Toast.makeText(this@SeriesDetailActivity, getString(R.string.series_added_to_favorites), Toast.LENGTH_SHORT).show()
                    }
                    isFavorite = !isFavorite
                    updateFavoriteButton()
                } catch (e: Exception) {
                    Toast.makeText(this@SeriesDetailActivity, getString(R.string.error_generic, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateFavoriteButton() {
        binding.btnFavorite.setImageResource(
            if (isFavorite) android.R.drawable.star_big_on
            else android.R.drawable.star_big_off
        )
    }
} 