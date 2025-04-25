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
import com.chortas.pixion.databinding.ActivitySeriesDetailBinding
import com.chortas.pixion.ui.detail.adapters.CastAdapter
import com.chortas.pixion.ui.detail.adapters.SeasonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SeriesDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeriesDetailBinding
    private lateinit var castAdapter: CastAdapter
    private lateinit var seasonAdapter: SeasonAdapter
    private var seriesId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeriesDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        seriesId = intent.getIntExtra("series_id", 0)
        setupRecyclerViews()
        loadSeriesDetails()
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
} 