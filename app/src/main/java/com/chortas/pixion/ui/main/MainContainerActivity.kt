package com.chortas.pixion.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.chortas.pixion.R
import com.google.firebase.auth.FirebaseAuth

class MainContainerActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        auth = FirebaseAuth.getInstance()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        // Verificar si hay una sesión activa
        if (auth.currentUser != null) {
            navController.navigate(R.id.mainFragment)
        }
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        if (!navController.popBackStack()) {
            if (auth.currentUser != null) {
                // Si estamos en el fragmento principal y el usuario está autenticado, no hacer nada
                return
            }
            super.onBackPressed()
        }
    }
} 