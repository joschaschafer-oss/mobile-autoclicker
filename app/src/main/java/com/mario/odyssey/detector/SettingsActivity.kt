package com.mario.odyssey.detector

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val confidenceSeekBar = findViewById<SeekBar>(R.id.confidenceSeekBar)
        val confidenceText = findViewById<TextView>(R.id.confidenceText)
        val vibrationSwitch = findViewById<Switch>(R.id.vibrationSwitch)
        val soundSwitch = findViewById<Switch>(R.id.soundSwitch)
        val recordingSwitch = findViewById<Switch>(R.id.recordingSwitch)
        val backButton = findViewById<Button>(R.id.backButton)

        val currentConfidence = (AppPreferences.getConfidenceThreshold(this) * 100).toInt()
        confidenceSeekBar.progress = currentConfidence
        confidenceText.text = "Konfidenz-Schwelle: $currentConfidence%"

        vibrationSwitch.isChecked = AppPreferences.isVibrationEnabled(this)
        soundSwitch.isChecked = AppPreferences.isSoundEnabled(this)
        recordingSwitch.isChecked = AppPreferences.isRecordingEnabled(this)

        confidenceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                confidenceText.text = "Konfidenz-Schwelle: $progress%"
                AppPreferences.setConfidenceThreshold(this@SettingsActivity, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setVibrationEnabled(this, isChecked)
        }

        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setSoundEnabled(this, isChecked)
        }

        recordingSwitch.setOnCheckedChangeListener { _, isChecked ->
            AppPreferences.setRecordingEnabled(this, isChecked)
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
