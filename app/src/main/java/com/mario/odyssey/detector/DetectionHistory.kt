package com.mario.odyssey.detector

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class DetectionLog(
    val timestamp: Long,
    val objectLabel: String,
    val confidence: Float
)

object DetectionHistory {
    
    private val detections = mutableListOf<DetectionLog>()
    private val maxEntries = 500
    
    fun addDetection(label: String, confidence: Float) {
        detections.add(DetectionLog(System.currentTimeMillis(), label, confidence))
        
        if (detections.size > maxEntries) {
            detections.removeAt(0)
        }
    }
    
    fun getHistory(): List<DetectionLog> = detections.toList()
    
    fun getStatistics(): Map<String, Int> {
        return detections.groupingBy { it.objectLabel }
            .eachCount()
            .toSortedMap { a, b -> detections.count { it.objectLabel == b }
                .compareTo(detections.count { it.objectLabel == a })
            }
    }
    
    fun getTopDetections(limit: Int = 10): List<Pair<String, Int>> {
        return getStatistics().entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key to it.value }
    }
    
    fun clear() {
        detections.clear()
    }
    
    fun exportToFile(context: Context): String {
        val fileName = "detection_${System.currentTimeMillis()}.txt"
        val file = File(context.filesDir, fileName)
        
        val content = buildString {
            appendLine("Mario Odyssey Detection Report")
            appendLine("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}")
            appendLine("Total Detections: ${detections.size}")
            appendLine("\nTop Detections:")
            getTopDetections().forEachIndexed { index, (label, count) ->
                appendLine("${index + 1}. $label: $count times")
            }
            appendLine("\nDetailed Log:")
            detections.forEach {
                val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(it.timestamp))
                appendLine("[$time] ${it.objectLabel} (${String.format("%.0f", it.confidence * 100)}%)")
            }
        }
        
        file.writeText(content)
        return file.absolutePath
    }
}
