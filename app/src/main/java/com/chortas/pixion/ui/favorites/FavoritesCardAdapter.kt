package com.chortas.pixion.ui.favorites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chortas.pixion.R
import com.chortas.pixion.data.model.Actor
import com.chortas.pixion.data.model.Movie
import com.chortas.pixion.data.model.Series

class FavoritesCardAdapter(
    private val items: List<Any>,
    private val onItemClick: (Any) -> Unit,
    private val onRemoveClick: (Any) -> Unit
) : RecyclerView.Adapter<FavoritesCardAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_card, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivPoster)
        private val titleView: TextView = itemView.findViewById(R.id.tvTitle)
        private val removeButton: ImageButton = itemView.findViewById(R.id.btnRemove)

        fun bind(item: Any) {
            when (item) {
                is Movie -> {
                    titleView.text = item.title
                    Glide.with(itemView.context)
                        .load("https://image.tmdb.org/t/p/w500${item.posterPath}")
                        .apply(RequestOptions().placeholder(R.drawable.placeholder_poster))
                        .into(imageView)
                }
                is Series -> {
                    titleView.text = item.name
                    Glide.with(itemView.context)
                        .load("https://image.tmdb.org/t/p/w500${item.posterPath}")
                        .apply(RequestOptions().placeholder(R.drawable.placeholder_poster))
                        .into(imageView)
                }
                is Actor -> {
                    titleView.text = item.name
                    Glide.with(itemView.context)
                        .load("https://image.tmdb.org/t/p/w500${item.profilePath}")
                        .apply(RequestOptions().placeholder(R.drawable.placeholder_poster))
                        .into(imageView)
                }
            }

            itemView.setOnClickListener { onItemClick(item) }
            removeButton.setOnClickListener { onRemoveClick(item) }
        }
    }
} 