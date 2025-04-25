package com.chortas.pixion.ui.detail.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.model.CastMember
import com.chortas.pixion.databinding.ItemCastBinding

class CastAdapter(
    private var cast: List<CastMember>,
    private val onCastClick: (CastMember) -> Unit = {}
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    fun updateCast(newCast: List<CastMember>) {
        cast = newCast
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(cast[position])
    }

    override fun getItemCount() = cast.size

    inner class CastViewHolder(private val binding: ItemCastBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCastClick(cast[position])
                }
            }
        }

        fun bind(castMember: CastMember) {
            binding.tvName.text = castMember.name
            binding.tvCharacter.text = castMember.character

            castMember.profilePath?.let { profilePath ->
                Glide.with(binding.root.context)
                    .load("https://image.tmdb.org/t/p/w500$profilePath")
                    .error(R.drawable.ic_movie_placeholder)
                    .into(binding.ivProfile)
            } ?: run {
                binding.ivProfile.setImageResource(R.drawable.ic_movie_placeholder)
            }
        }
    }
} 