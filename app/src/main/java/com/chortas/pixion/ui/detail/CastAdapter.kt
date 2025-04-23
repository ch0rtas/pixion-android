package com.chortas.pixion.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chortas.pixion.data.model.CastMember
import com.chortas.pixion.databinding.ItemCastBinding

class CastAdapter(
    private val cast: List<CastMember>
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ItemCastBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(cast[position])
    }

    override fun getItemCount() = cast.size

    inner class CastViewHolder(
        private val binding: ItemCastBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(castMember: CastMember) {
            binding.tvName.text = castMember.name
            binding.tvCharacter.text = castMember.character

            Glide.with(binding.root.context)
                .load("https://image.tmdb.org/t/p/w200${castMember.profilePath}")
                .into(binding.ivProfile)
        }
    }
} 