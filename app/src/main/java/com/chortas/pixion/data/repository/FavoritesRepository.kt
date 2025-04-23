package com.chortas.pixion.data.repository

import com.chortas.pixion.data.model.Favorite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.tasks.await

class FavoritesRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val favoritesRef = database.reference.child("favorites")

    suspend fun addToFavorites(movieId: Int) {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        val favorite = Favorite(movieId = movieId, userId = userId)
        
        favoritesRef
            .child(userId)
            .child(movieId.toString())
            .setValue(favorite)
            .await()
    }

    suspend fun removeFromFavorites(movieId: Int) {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        
        favoritesRef
            .child(userId)
            .child(movieId.toString())
            .removeValue()
            .await()
    }

    suspend fun getFavorites(): List<Int> {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        
        val snapshot = favoritesRef
            .child(userId)
            .get()
            .await()

        return if (snapshot.exists()) {
            snapshot.children.mapNotNull { it.getValue<Favorite>()?.movieId }
        } else {
            emptyList()
        }
    }

    suspend fun isFavorite(movieId: Int): Boolean {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        
        val snapshot = favoritesRef
            .child(userId)
            .child(movieId.toString())
            .get()
            .await()

        return snapshot.exists()
    }
} 