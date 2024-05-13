package com.example.serayularanganapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.serayularanganapp.R
import com.example.serayularanganapp.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()

        // Validasi email menggunakan RxTextView
        val emailStream = RxTextView.textChanges(binding.emailEt)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe {
            showEmailValidAlert(it)
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this) { reset ->
                    if (reset.isSuccessful) {
                        // Jika pengaturan ulang kata sandi berhasil, arahkan ke LoginActivity
                        Intent(this, LoginActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                            Toast.makeText(this, "Periksa email untuk mengatur ulang kata sandi!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, reset.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.tvBackLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

    }

    // Menampilkan pesan kesalahan untuk validasi email
    private fun showEmailValidAlert(isNotValid: Boolean) {
        if (isNotValid) {
            binding.emailEt.error = "Email tidak valid!"
            binding.btnResetPassword.isEnabled = false
            binding.btnResetPassword.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey)
        } else {
            binding.emailEt.error = null
            binding.btnResetPassword.isEnabled = true
            binding.btnResetPassword.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green)
        }
    }
}