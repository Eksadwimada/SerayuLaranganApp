package com.example.serayularanganapp.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serayularanganapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_SCREEN_DURATION = 3000L // Durasi splash screen dalam milidetik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        checkInternetConnectionAndProceed()
    }

    private fun checkInternetConnectionAndProceed() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            // Jika ada koneksi internet, lanjutkan dengan menampilkan splash screen
            showSplashScreenWithDelay(SPLASH_SCREEN_DURATION)
        } else {
            // Jika tidak ada koneksi internet, tampilkan pesan dan tahan splash screen
            showNoInternetMessage()
        }
    }

    private fun showSplashScreenWithDelay(delayMillis: Long) {
        GlobalScope.launch(Dispatchers.Main) {
            delay(delayMillis)
            navigateToNextScreen()
        }
    }

    private fun showNoInternetMessage() {
        Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
        // Tahan splash screen, tidak memanggil navigateToNextScreen()
    }

    private fun navigateToNextScreen() {
        val intent = Intent(this, OnBoardingActivity::class.java)
        startActivity(intent)
        finish()
    }
}

