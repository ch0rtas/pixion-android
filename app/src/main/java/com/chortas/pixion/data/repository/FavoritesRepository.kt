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

    suspend fun addToFavorites(contentId: Int, type: String) {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        val favorite = Favorite(contentId = contentId, userId = userId, type = type)
        
        favoritesRef
            .child(userId)
            .child(contentId.toString())
            .setValue(favorite)
            .await()
    }

    suspend fun removeFromFavorites(contentId: Int) {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        
        favoritesRef
            .child(userId)
            .child(contentId.toString())
            .removeValue()
            .await()
    }

    suspend fun getFavorites(type: String? = null): List<Favorite> {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        
        val snapshot = favoritesRef
            .child(userId)
            .get()
            .await()

        return if (snapshot.exists()) {
            snapshot.children.mapNotNull { it.getValue<Favorite>() }
                .filter { type == null || it.type == type }
        } else {
            emptyList()
        }
    }

    suspend fun isFavorite(contentId: Int): Boolean {
        val userId = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
        
        val snapshot = favoritesRef
            .child(userId)
            .child(contentId.toString())
            .get()
            .await()

        return snapshot.exists()
    }
} 