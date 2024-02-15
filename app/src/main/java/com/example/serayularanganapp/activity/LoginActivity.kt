package com.example.serayularanganapp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.serayularanganapp.R
import com.example.serayularanganapp.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable

@SuppressLint("CheckResult")
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

//  Auth
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

//  Username Validation
        val usernameStream = RxTextView.textChanges(binding.emailEt)
            .skipInitialValue()
            .map { username ->
                username.isEmpty()
            }
        usernameStream.subscribe {
            showTextMinimalAlert(it, "Username")
        }

//  Password Validation
        val passwordStream = RxTextView.textChanges(binding.passEt)
            .skipInitialValue()
            .map { password ->
                password.isEmpty()
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it, "Password")
        }

//  Button Enable
        val invalidFieldStream = Observable.combineLatest(
            usernameStream,
            passwordStream,
            { usernameInvalid: Boolean, passwordInvalid: Boolean ->
                !usernameInvalid && !passwordInvalid
            })
        invalidFieldStream.subscribe { isValid ->
            if (isValid) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
            } else {
                binding.btnLogin.isEnabled = false
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey)
            }
        }

// click
        binding.btnLogin.setOnClickListener {
            val emailOrUsername = binding.emailEt.text.toString().trim()
            val password = binding.passEt.text.toString().trim()
            loginUser(emailOrUsername, password)
        }
        binding.tvHaventAccountLog.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPass.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if (text == "Email/Username")
            binding.emailEt.error = if (isNotValid) "Tidak Boleh Kosong!" else null
        else if (text == "password")
            binding.passEt.error = if (isNotValid) "Tidak Boleh Kosong!" else null
    }

    private fun loginUser(emailOrUsername: String, pass: String) {
        // Cek apakah input adalah email atau username
        val isEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches()

        if (isEmail) {
            // Jika input adalah email, gunakan signInWithEmailAndPassword
            auth.signInWithEmailAndPassword(emailOrUsername, pass)
                .addOnCompleteListener(this) { login ->
                    handleLoginResult(login)
                }
        } else {
            // Jika input adalah username, tambahkan metode untuk mendapatkan email dari username
            getUserEmailFromUsername(emailOrUsername) { email ->
                if (email != null) {
                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this) { login ->
                            handleLoginResult(login)
                        }
                } else {
                    // Handle jika username tidak ditemukan
                    Toast.makeText(this, "Username tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Menangani hasil dari proses login
    private fun handleLoginResult(loginTask: Task<AuthResult>) {
        if (loginTask.isSuccessful) {
            Intent(this, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
                Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, loginTask.exception?.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserEmailFromUsername(username: String, onComplete: (String?) -> Unit) {
        // Ambil referensi dari Realtime Database atau Firestore sesuai dengan struktur data pengguna Anda
        val usersRef = database.getReference("users")

        // Cari pengguna berdasarkan username
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Jika username ditemukan, ambil email
                    val user = snapshot.children.first()
                    val email = user.child("email").getValue(String::class.java)
                    onComplete(email)
                } else {
                    onComplete(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onComplete(null)
            }
        })
    }
}