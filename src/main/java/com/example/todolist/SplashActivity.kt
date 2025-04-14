package com.example.todolist

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Optionally, if you want the splash screen theme to be applied immediately,
        // set the theme in the manifest (see step 3)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Delay for a set time (e.g., 2 seconds) then start MainActivity.
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000) // 2000 milliseconds = 2 seconds
    }
}
