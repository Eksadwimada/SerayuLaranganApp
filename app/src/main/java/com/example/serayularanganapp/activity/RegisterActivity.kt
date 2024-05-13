package com.example.serayularanganapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.serayularanganapp.R
import com.example.serayularanganapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

// Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

//  Username Validation
        val usernameStream = RxTextView.textChanges(binding.userEt)
            .skipInitialValue()
            .map { username ->
                username.length < 6
            }
        usernameStream.subscribe {
            showTextMinimalAlert(it, "Username")
        }

//  Email Validation
        val emailStream = RxTextView.textChanges(binding.emailEt)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showEmailValidAlert(it)
        }

//  Password Validation
        val passwordStream = RxTextView.textChanges(binding.passEt)
            .skipInitialValue()
            .map { password ->
                password.length < 6
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it, "Password")
        }

//  Confirm Pass Validation
        val passwordConfirmStream = Observable.merge(
            RxTextView.textChanges(binding.passEt)
                .skipInitialValue()
                .map { password ->
                    password.toString() != binding.confPassEt.text.toString()
                },
            RxTextView.textChanges(binding.confPassEt)
                .skipInitialValue()
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.passEt.text.toString()
                })
        passwordConfirmStream.subscribe {
            showPasswordConfAlert(it)
        }

//  Button Enable
        val invalidFieldStream = Observable.combineLatest(
            usernameStream,
            emailStream,
            passwordStream,
            passwordConfirmStream,
            { usernameInvalid: Boolean, emailInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmInvalid: Boolean ->
                !usernameInvalid && !emailInvalid && !passwordInvalid && !passwordConfirmInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            if (isValid) {
                binding.btnRegis.isEnabled = true
                binding.btnRegis.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
            } else {
                binding.btnRegis.isEnabled = false
                binding.btnRegis.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey)
            }
        }

//  Click
        binding.btnRegis.setOnClickListener {
            val username = binding.userEt.text.toString().trim()
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passEt.text.toString().trim()
            registerUser(username, email, password)
        }
        binding.tvHaventAccountReg.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, OnBoardingActivity::class.java))
        }
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if (text == "Username")
            binding.userEt.error = if (isNotValid) "Harus lebih dari 6 huruf!" else null
        else if (text == "password")
            binding.passEt.error = if (isNotValid) "Harus lebih dari 8 huruf!" else null
    }

    private fun showEmailValidAlert(isNotValid: Boolean) {
        binding.emailEt.error = if (isNotValid) "Email tidak valid!" else null
    }

    private fun showPasswordConfAlert(isNotValid: Boolean) {
        binding.confPassEt.error = if (isNotValid) "Password tidak sama!" else null
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Pengguna berhasil dibuat
                    val user = auth.currentUser
                    val userId = user?.uid

                    // Simpan data pengguna ke Realtime Database
                    if (userId != null) {
                        val userRef = database.getReference("users")

                        val userData = HashMap<String, Any>()
                        userData["username"] = username
                        userData["email"] = email

                        userRef.child(userId).setValue(userData)
                            .addOnCompleteListener { databaseTask ->
                                if (databaseTask.isSuccessful) {
                                    startActivity(Intent(this, LoginActivity::class.java))
                                    Toast.makeText(this, "Berhasil Membuat Akun", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, databaseTask.exception?.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    // Tangani kesalahan saat pendaftaran
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        // Jika alamat email telah terdaftar sebelumnya
                        Toast.makeText(this, "Alamat email sudah terdaftar. Silakan gunakan alamat email lain.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Kesalahan lainnya
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

}