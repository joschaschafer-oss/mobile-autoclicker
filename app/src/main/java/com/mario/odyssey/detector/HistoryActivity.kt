package com.mario.odyssey.detector

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val historyText = findViewById<TextView>(R.id.historyText)
        val statsText = findViewById<TextView>(R.id.statsText)
        val backButton = findViewById<Button>(R.id.backButton)
        val clearButton = findViewById<Button>(R.id.clearButton)
        val exportButton = findViewById<Button>(R.id.exportButton)

        updateDisplay(historyText, statsText)

        backButton.setOnClickListener {
            finish()
        }

        clearButton.setOnClickListener {
            DetectionHistory.clear()
            updateDisplay(historyText, statsText)
        }

        exportButton.setOnClickListener {
            val filePath = DetectionHistory.exportToFile(this)
            statsText.text = "Exportiert zu:\n$filePath"
        }
    }

    private fun updateDisplay(historyText: TextView, statsText: TextView) {
        val topDetections = DetectionHistory.getTopDetections(15)

        historyText.text = buildString {
            appendLine("🏆 TOP ERKANNTE OBJEKTE:")
            appendLine()
            topDetections.forEachIndexed { index, (label, count) ->
                appendLine("${index + 1}. $label: $count mal")
            }
            if (topDetections.isEmpty()) {
                appendLine("Keine Daten vorhanden")
            }
        }

        val stats = DetectionHistory.getStatistics()
        statsText.text = buildString {
            appendLine("📊 STATISTIKEN:")
            appendLine("Gesamt: ${DetectionHistory.getHistory().size} Detektionen")
            appendLine("Einzigartige Objekte: ${stats.size}")
        }
    }
}
