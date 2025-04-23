package com.chortas.pixion.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chortas.pixion.databinding.ActivityLoginBinding
import com.chortas.pixion.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnLogin.setOnClickListener {
            val identifier = binding.etIdentifier.text.toString()
            val password = binding.etPassword.text.toString()

            if (identifier.isNotEmpty() && password.isNotEmpty()) {
                loginUser(identifier, password)
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos", 
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(identifier: String, password: String) {
        // Primero intentamos iniciar sesión con el identificador como email
        auth.signInWithEmailAndPassword(identifier, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Si falla, intentamos con el nombre de usuario
                    checkUsernameAndLogin(identifier, password)
                }
            }
    }

    private fun checkUsernameAndLogin(username: String, password: String) {
        database.reference.child("usernames")
            .child(username)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userId = snapshot.getValue(String::class.java)
                    userId?.let { uid ->
                        database.reference.child("users")
                            .child(uid)
                            .child("email")
                            .get()
                            .addOnSuccessListener { emailSnapshot ->
                                val email = emailSnapshot.getValue(String::class.java)
                                email?.let { userEmail ->
                                    auth.signInWithEmailAndPassword(userEmail, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finish()
                                            } else {
                                                Toast.makeText(this, 
                                                    "Error al iniciar sesión: ${task.exception?.message}", 
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "Credenciales inválidas", 
                        Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar el nombre de usuario", 
                    Toast.LENGTH_SHORT).show()
            }
    }
} 