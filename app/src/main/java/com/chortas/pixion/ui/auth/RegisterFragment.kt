package com.chortas.pixion.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chortas.pixion.R
import com.chortas.pixion.data.model.User
import com.chortas.pixion.databinding.DialogTermsBinding
import com.chortas.pixion.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.regex.Pattern

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var termsAccepted = false
    private var isUsernameAvailable = false
    private var isEmailAvailable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupUsernameValidation()
        setupEmailValidation()
        setupPasswordValidation()
        setupTermsAndConditions()
        setupRegisterButton()

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun setupUsernameValidation() {
        val usernamePattern = Pattern.compile("^[a-zA-Z0-9_]{4,}$")
        
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val username = s.toString()
                if (username.isEmpty()) {
                    binding.tilUsername.helperText = "El nombre de usuario debe tener mínimo 4 caracteres y no puede contener caracteres especiales como % $ () = ¿? ¡! @ # & *"
                    binding.tilUsername.isHelperTextEnabled = true
                    binding.tvUsernameAvailability.text = ""
                    isUsernameAvailable = false
                } else if (!usernamePattern.matcher(username).matches()) {
                    binding.tilUsername.helperText = "El nombre de usuario debe tener mínimo 4 caracteres y no puede contener caracteres especiales como % $ () = ¿? ¡! @ # & *"
                    binding.tilUsername.isHelperTextEnabled = true
                    binding.tvUsernameAvailability.text = "Usuario no disponible"
                    binding.tvUsernameAvailability.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    isUsernameAvailable = false
                } else {
                    binding.tilUsername.isHelperTextEnabled = false
                    checkUsernameAvailability(username)
                }
                validateForm()
            }
        })
    }

    private fun setupEmailValidation() {
        val emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString()
                if (email.isEmpty()) {
                    binding.tilEmail.helperText = "El email debe tener un formato válido (ejemplo@dominio.com)"
                    binding.tilEmail.isHelperTextEnabled = true
                    binding.tilEmail.error = null
                    isEmailAvailable = false
                } else if (!emailPattern.matcher(email).matches()) {
                    binding.tilEmail.helperText = "El email debe tener un formato válido (ejemplo@dominio.com)"
                    binding.tilEmail.isHelperTextEnabled = true
                    binding.tilEmail.error = "Formato de email no válido"
                    isEmailAvailable = false
                } else {
                    binding.tilEmail.isHelperTextEnabled = false
                    binding.tilEmail.error = null
                    checkEmailAvailability(email)
                }
                validateForm()
            }
        })
    }

    private fun setupPasswordValidation() {
        val passwordPattern = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
        )

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isEmpty()) {
                    binding.tilPassword.helperText = "La contraseña debe contener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
                    binding.tilPassword.isHelperTextEnabled = true
                    binding.tilPassword.error = null
                } else if (!passwordPattern.matcher(password).matches()) {
                    binding.tilPassword.helperText = "La contraseña debe contener al menos 8 caracteres, una mayúscula, un número y un carácter especial"
                    binding.tilPassword.isHelperTextEnabled = true
                    binding.tilPassword.error = "Contraseña no válida"
                } else {
                    binding.tilPassword.isHelperTextEnabled = false
                    binding.tilPassword.error = null
                }
                validateForm()
            }
        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val confirmPassword = s.toString()
                val password = binding.etPassword.text.toString()
                if (password != confirmPassword) {
                    binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                } else {
                    binding.tilConfirmPassword.error = null
                }
                validateForm()
            }
        })
    }

    private fun setupTermsAndConditions() {
        binding.cbTerms.setOnClickListener {
            if (binding.cbTerms.isChecked) {
                showTermsDialog()
            }
            validateForm()
        }
    }

    private fun showTermsDialog() {
        val dialogView = DialogTermsBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView.root)
            .setCancelable(false)
            .create()

        dialogView.btnAcceptTerms.setOnClickListener {
            termsAccepted = true
            binding.cbTerms.isChecked = true
            validateForm()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun validateForm() {
        val email = binding.etEmail.text.toString()
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val usernamePattern = Pattern.compile("^[a-zA-Z0-9_]{4,}$")
        val passwordPattern = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
        )

        binding.btnRegister.isEnabled = email.isNotEmpty() &&
                username.isNotEmpty() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                password == confirmPassword &&
                usernamePattern.matcher(username).matches() &&
                passwordPattern.matcher(password).matches() &&
                termsAccepted &&
                isUsernameAvailable &&
                isEmailAvailable
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            registerUser(email, password, username)
        }
    }

    private fun registerUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val newUser = User(
                            uid = it.uid,
                            email = email,
                            username = username
                        )
                        
                        database.reference.child("users").child(it.uid).setValue(newUser)
                        database.reference.child("usernames").child(username).setValue(it.uid)
                        
                        findNavController().navigate(R.id.mainActivity)
                    }
                } else {
                    Toast.makeText(requireContext(), 
                        getString(R.string.registration_error, task.exception?.message), 
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkUsernameAvailability(username: String) {
        database.reference.child("usernames")
            .child(username)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    binding.tvUsernameAvailability.text = "Nombre de usuario disponible"
                    binding.tvUsernameAvailability.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
                    isUsernameAvailable = true
                } else {
                    binding.tvUsernameAvailability.text = "Nombre de usuario ya en uso"
                    binding.tvUsernameAvailability.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                    isUsernameAvailable = false
                }
                validateForm()
            }
            .addOnFailureListener {
                binding.tvUsernameAvailability.text = "Error al verificar disponibilidad"
                binding.tvUsernameAvailability.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                isUsernameAvailable = false
                validateForm()
            }
    }

    private fun checkEmailAvailability(email: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        binding.tilEmail.error = null
                        isEmailAvailable = true
                    } else {
                        binding.tilEmail.error = "Este email ya está en uso"
                        binding.tilEmail.errorIconDrawable = null
                        isEmailAvailable = false
                    }
                } else {
                    binding.tilEmail.error = "Error al verificar el email"
                    binding.tilEmail.errorIconDrawable = null
                    isEmailAvailable = false
                }
                validateForm()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 