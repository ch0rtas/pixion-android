package com.chortas.pixion.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chortas.pixion.data.model.Series
import com.chortas.pixion.databinding.ItemMovieBinding

class SeriesAdapter(
    private var series: List<Series>,
    private val onSeriesClick: (Series) -> Unit
) : RecyclerView.Adapter<SeriesAdapter.SeriesViewHolder>() {

    fun updateSeries(newSeries: List<Series>) {
        series = newSeries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeriesViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SeriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeriesViewHolder, position: Int) {
        holder.bind(series[position])
    }

    override fun getItemCount() = series.size

    inner class SeriesViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSeriesClick(series[position])
                }
            }
        }

        fun bind(series: Series) {
            binding.tvTitle.text = series.name
            binding.tvRating.text = String.format("%.1f", series.voteAverage)

            series.posterPath?.let { posterPath ->
                val imageUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                Log.d("SeriesAdapter", "Loading image for ${series.name}: $imageUrl")
                
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .error(android.R.drawable.ic_menu_gallery)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivPoster)
            } ?: run {
                Log.e("SeriesAdapter", "No poster path for series: ${series.name}")
                binding.ivPoster.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }
} 