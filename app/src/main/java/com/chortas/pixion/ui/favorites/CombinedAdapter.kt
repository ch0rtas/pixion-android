package com.chortas.pixion.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.model.Actor
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.data.model.Series
import com.chortas.pixion.databinding.ItemActorBinding
import com.chortas.pixion.databinding.ItemMovieBinding
import com.chortas.pixion.databinding.ItemSeriesBinding

class CombinedAdapter(
    private var items: List<Any>,
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MOVIE = 0
        private const val TYPE_SERIES = 1
        private const val TYPE_ACTOR = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is Movie -> TYPE_MOVIE
            is Series -> TYPE_SERIES
            is Actor -> TYPE_ACTOR
            else -> throw IllegalArgumentException("Tipo de elemento no soportado")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MOVIE -> {
                val binding = ItemMovieBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MovieViewHolder(binding)
            }
            TYPE_SERIES -> {
                val binding = ItemSeriesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SeriesViewHolder(binding)
            }
            TYPE_ACTOR -> {
                val binding = ItemActorBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ActorViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Tipo de vista no soportado")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MovieViewHolder -> holder.bind(items[position] as Movie)
            is SeriesViewHolder -> holder.bind(items[position] as Series)
            is ActorViewHolder -> holder.bind(items[position] as Actor)
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<Any>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }

        fun bind(movie: Movie) {
            binding.tvTitle.text = movie.title
            binding.tvRating.text = String.format("%.1f", movie.voteAverage)
            
            movie.posterPath?.let { posterPath ->
                Glide.with(binding.root)
                    .load("https://image.tmdb.org/t/p/w500$posterPath")
                    .error(R.drawable.ic_movie_placeholder)
                    .into(binding.ivPoster)
            } ?: run {
                binding.ivPoster.setImageResource(R.drawable.ic_movie_placeholder)
            }
        }
    }

    inner class SeriesViewHolder(
        private val binding: ItemSeriesBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }

        fun bind(series: Series) {
            binding.tvTitle.text = series.name
            binding.tvRating.text = String.format("%.1f", series.voteAverage)
            
            series.posterPath?.let { posterPath ->
                Glide.with(binding.root)
                    .load("https://image.tmdb.org/t/p/w500$posterPath")
                    .error(R.drawable.ic_movie_placeholder)
                    .into(binding.ivPoster)
            } ?: run {
                binding.ivPoster.setImageResource(R.drawable.ic_movie_placeholder)
            }
        }
    }

    inner class ActorViewHolder(
        private val binding: ItemActorBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }

        fun bind(actor: Actor) {
            binding.tvName.text = actor.name
            
            actor.profilePath?.let { profilePath ->
                Glide.with(binding.root)
                    .load("https://image.tmdb.org/t/p/w500$profilePath")
                    .error(R.drawable.ic_movie_placeholder)
                    .into(binding.ivProfile)
            } ?: run {
                binding.ivProfile.setImageResource(R.drawable.ic_movie_placeholder)
            }
        }
    }
} 