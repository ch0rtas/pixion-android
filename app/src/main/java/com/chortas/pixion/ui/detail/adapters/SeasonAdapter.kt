package com.chortas.pixion.ui.detail.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.model.Season
import com.chortas.pixion.databinding.ItemSeasonBinding

class SeasonAdapter(
    private var seasons: List<Season>,
    private val onSeasonClick: (Season) -> Unit
) : RecyclerView.Adapter<SeasonAdapter.SeasonViewHolder>() {

    fun updateSeasons(newSeasons: List<Season>) {
        seasons = newSeasons
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonViewHolder {
        val binding = ItemSeasonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SeasonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeasonViewHolder, position: Int) {
        holder.bind(seasons[position])
    }

    override fun getItemCount() = seasons.size

    inner class SeasonViewHolder(
        private val binding: ItemSeasonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSeasonClick(seasons[position])
                }
            }
        }

        fun bind(season: Season) {
            binding.tvName.text = season.name
            binding.tvEpisodeCount.text = "${season.episodeCount} episodios"
            binding.tvAirDate.text = season.getFormattedAirDate()

            season.posterPath?.let { posterPath ->
                Glide.with(binding.root.context)
                    .load("https://image.tmdb.org/t/p/w500$posterPath")
                    .error(R.drawable.ic_movie_placeholder)
                    .into(binding.ivPoster)
            } ?: run {
                binding.ivPoster.setImageResource(R.drawable.ic_movie_placeholder)
            }
        }
    }
} 