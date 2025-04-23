package com.chortas.pixion.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.databinding.ItemMovieBinding

class MovieAdapter(
    private val movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount() = movies.size

    inner class MovieViewHolder(
        private val binding: ItemMovieBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMovieClick(movies[position])
                }
            }
        }

        fun bind(movie: Movie) {
            binding.tvTitle.text = movie.title
            binding.tvRating.text = String.format("%.1f", movie.voteAverage)

            movie.posterPath?.let { posterPath ->
                val imageUrl = "https://image.tmdb.org/t/p/w500$posterPath"
                Log.d("MovieAdapter", "Loading image for ${movie.title}: $imageUrl")
                
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .error(android.R.drawable.ic_menu_gallery)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivPoster)
            } ?: run {
                Log.e("MovieAdapter", "No poster path for movie: ${movie.title}")
                binding.ivPoster.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }
} 