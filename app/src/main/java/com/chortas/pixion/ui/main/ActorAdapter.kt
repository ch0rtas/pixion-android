package com.chortas.pixion.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chortas.pixion.R
import com.chortas.pixion.data.model.Actor
import com.chortas.pixion.databinding.ItemActorBinding

class ActorAdapter(
    private var actors: List<Actor>,
    private val onActorClick: (Actor) -> Unit
) : RecyclerView.Adapter<ActorAdapter.ActorViewHolder>() {

    inner class ActorViewHolder(private val binding: ItemActorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(actor: Actor) {
            binding.tvName.text = actor.name
            
            actor.profilePath?.let { profilePath ->
                Glide.with(binding.root.context)
                    .load("https://image.tmdb.org/t/p/w500$profilePath")
                    .error(R.drawable.ic_movie_placeholder)
                    .into(binding.ivProfile)
            } ?: run {
                binding.ivProfile.setImageResource(R.drawable.ic_movie_placeholder)
            }

            binding.root.setOnClickListener {
                onActorClick(actor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorViewHolder {
        val binding = ItemActorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ActorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActorViewHolder, position: Int) {
        holder.bind(actors[position])
    }

    override fun getItemCount(): Int = actors.size

    fun updateActors(newActors: List<Actor>) {
        actors = newActors
        notifyDataSetChanged()
    }
} 