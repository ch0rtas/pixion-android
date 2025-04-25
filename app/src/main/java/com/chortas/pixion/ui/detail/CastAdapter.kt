package com.chortas.pixion.ui.detail

import android.content.Intent
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

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val actor = cast[position]
                    val intent = Intent(binding.root.context, ActorDetailActivity::class.java)
                    intent.putExtra("actor_id", actor.id)
                    binding.root.context.startActivity(intent)
                }
            }
        }

        fun bind(castMember: CastMember) {
            binding.tvName.text = castMember.name
            binding.tvCharacter.text = castMember.character

            Glide.with(binding.root.context)
                .load("https://image.tmdb.org/t/p/w200${castMember.profilePath}")
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivProfile)
        }
    }
} 