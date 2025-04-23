package com.chortas.pixion.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chortas.pixion.R
import com.chortas.pixion.data.model.User
import com.chortas.pixion.databinding.ActivityRegisterBinding
import com.chortas.pixion.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    checkUsernameAvailability(username) { isAvailable ->
                        if (isAvailable) {
                            registerUser(email, password, username)
                        } else {
                            Toast.makeText(this, getString(R.string.username_taken), 
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, getString(R.string.passwords_dont_match), 
                        Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, getString(R.string.fill_all_fields), 
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        database.reference.child("usernames")
            .child(username)
            .get()
            .addOnSuccessListener { snapshot ->
                callback(!snapshot.exists())
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.error_checking_username), 
                    Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val newUser = User(
                            uid = it.uid,
                            email = email,
                            username = username
                        )
                        
                        // Guardar usuario en Realtime Database
                        database.reference.child("users").child(it.uid).setValue(newUser)
                        
                        // Guardar referencia de nombre de usuario
                        database.reference.child("usernames").child(username).setValue(it.uid)
                        
                        // Navegar a la pantalla principal
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.registration_error, task.exception?.message), 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
} 
