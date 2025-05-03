package com.chortas.pixion.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chortas.pixion.R
import com.chortas.pixion.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.btnLogin.setOnClickListener {
            val identifier = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (identifier.isNotEmpty() && password.isNotEmpty()) {
                loginUser(identifier, password)
            } else {
                Toast.makeText(requireContext(), getString(R.string.fill_all_fields), 
                    Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }

    private fun loginUser(identifier: String, password: String) {
        auth.signInWithEmailAndPassword(identifier, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
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
                                                navigateToMain()
                                            } else {
                                                Toast.makeText(requireContext(), 
                                                    "Error al iniciar sesión: ${task.exception?.message}", 
                                                    Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                }
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.invalid_credentials), 
                        Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), getString(R.string.error_checking_username), 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 