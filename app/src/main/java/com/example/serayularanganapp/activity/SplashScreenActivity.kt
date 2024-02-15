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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menghilangkan batas atas layar
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        checkInternetConnectionAndDelay()
    }

    private fun checkInternetConnectionAndDelay() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (networkInfo != null && networkInfo.isConnected) {
            // Ada koneksi internet, lanjutkan dengan menampilkan splash screen
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    // Lakukan operasi unduhan
                    delay(3000) //
                    navigateToNextScreen()
                } catch (e: Exception) {
                    // Tangani kesalahan atau tampilkan pesan yang sesuai
                    showErrorMessage()
                }
            }
        } else {
            showNoInternetMessage()
            navigateToNextScreen()
        }
    }

    private fun showNoInternetMessage() {
        // Tampilkan pesan tidak ada koneksi internet menggunakan Toast
        Toast.makeText(this, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage() {
        // Tampilkan pesan kesalahan menggunakan Toast
        Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToNextScreen() {
        // Membuat intent untuk beralih ke aktivitas berikutnya
        val intent = Intent(this, OnBoardingActivity::class.java)
        startActivity(intent)
        finish()
    }
}
