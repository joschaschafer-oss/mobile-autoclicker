package com.mario.odyssey.detector

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mario.odyssey.detector.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startButton.setOnClickListener {
            startActivity(Intent(this, DetectionActivity::class.java))
        }

        binding.infoText.text = """
            Mario Odyssey Echtzeit-Objekterkennung
            
            Diese App erkennt in Echtzeit:
            • Mario
            • Münzen
            • Gegenstände
            • Gegner
            • Andere Entitys
            
            Tippe "Start" um zu beginnen!
        """.trimIndent()
    }
}
