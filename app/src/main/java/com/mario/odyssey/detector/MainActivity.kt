package com.mario.odyssey.detector

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        val exitButton = findViewById<Button>(R.id.exitButton)
        val infoText = findViewById<TextView>(R.id.infoText)

        infoText.text = "Mario Odyssey Scanner\n\nEchtzeitErkennung von:\n• Mario\n• Münzen\n• Gegenstände\n• Gegner"

        startButton.setOnClickListener {
            startActivity(Intent(this, DetectionActivity::class.java))
        }

        exitButton.setOnClickListener {
            finish()
        }
    }
}
