package com.chortas.pixion.ui.detail.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.model.Episode
import com.chortas.pixion.databinding.ItemEpisodeBinding

class EpisodeAdapter(
    private var episodes: List<Episode>
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    fun updateEpisodes(newEpisodes: List<Episode>) {
        episodes = newEpisodes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position])
    }

    override fun getItemCount() = episodes.size

    inner class EpisodeViewHolder(
        private val binding: ItemEpisodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(episode: Episode) {
            binding.tvEpisodeNumber.text = binding.root.context.getString(R.string.episode_number, episode.episodeNumber)
            binding.tvName.text = episode.name
            binding.tvAirDate.text = episode.getFormattedAirDate()
            binding.tvRating.text = episode.getFormattedRating()

            episode.stillPath?.let { stillPath ->
                Glide.with(binding.root.context)
                    .load("https://image.tmdb.org/t/p/w500$stillPath")
                    .error(R.drawable.ic_movie_placeholder)
                    .into(binding.ivStill)
            } ?: run {
                binding.ivStill.setImageResource(R.drawable.ic_movie_placeholder)
            }
        }
    }
} 