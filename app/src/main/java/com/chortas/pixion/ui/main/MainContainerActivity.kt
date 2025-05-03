package com.chortas.pixion.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.chortas.pixion.R
import com.google.firebase.auth.FirebaseAuth

class MainContainerActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var doubleBackToExitPressedOnce = false
    private val handler = Handler(Looper.getMainLooper())

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

        // Si estamos en el MainFragment y hay un usuario autenticado
        if (navController.currentDestination?.id == R.id.mainFragment && auth.currentUser != null) {
            if (doubleBackToExitPressedOnce) {
                finish()
                return
            }

            doubleBackToExitPressedOnce = true
            Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show()

            handler.postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000) // 2 segundos para presionar de nuevo
            return
        }

        // Para otros casos, usar la navegación normal
        if (!navController.popBackStack()) {
            super.onBackPressed()
        }
    }
} 