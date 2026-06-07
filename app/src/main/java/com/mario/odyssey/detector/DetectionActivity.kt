package com.mario.odyssey.detector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.previewview.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DetectionActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var previewView: PreviewView
    private lateinit var surfaceView: SurfaceView
    private lateinit var statsTextView: TextView
    private lateinit var backButton: Button
    private lateinit var settingsButton: ImageButton
    private lateinit var historyButton: ImageButton
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var surfaceHolder: SurfaceHolder

    private var detectedObjects = mutableListOf<DetectionResult>()
    private var fps = 0
    private var fpsCounter = 0
    private var lastTime = System.currentTimeMillis()
    private var isRunning = true
    private var lastDetectionTime = 0L

    data class DetectionResult(
        val label: String,
        val confidence: Float,
        val boundingBox: android.graphics.RectF
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection)

        previewView = findViewById(R.id.previewView)
        surfaceView = findViewById(R.id.surfaceView)
        statsTextView = findViewById(R.id.statsTextView)
        backButton = findViewById(R.id.backButton)
        settingsButton = findViewById(R.id.settingsButton)
        historyButton = findViewById(R.id.historyButton)

        cameraExecutor = Executors.newSingleThreadExecutor()

        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        backButton.setOnClickListener {
            isRunning = false
            finish()
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, ObjectDetectionAnalyzer())
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e(TAG, "Camera error: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class ObjectDetectionAnalyzer : ImageAnalysis.Analyzer {

        private val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        private val objectDetector = ObjectDetection.getClient(options)

        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            val confidenceThreshold = AppPreferences.getConfidenceThreshold(this@DetectionActivity)

            objectDetector.process(inputImage)
                .addOnSuccessListener { detectedObjectsList ->
                    if (isRunning) {
                        detectedObjects.clear()

                        for (detectedObject in detectedObjectsList) {
                            val boundingBox = detectedObject.boundingBox
                            val labels = detectedObject.labels

                            for (label in labels) {
                                if (label.confidence > confidenceThreshold) {
                                    detectedObjects.add(
                                        DetectionResult(
                                            label = label.text,
                                            confidence = label.confidence,
                                            boundingBox = boundingBox
                                        )
                                    )

                                    val now = System.currentTimeMillis()
                                    if (now - lastDetectionTime > 300) {
                                        SoundManager.playDetectionSound(this@DetectionActivity)
                                        SoundManager.vibrate(this@DetectionActivity)
                                        lastDetectionTime = now
                                    }

                                    DetectionHistory.addDetection(label.text, label.confidence)
                                }
                            }
                        }

                        updateFPS()
                        drawDetections()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Detection error: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun drawDetections() {
        if (!surfaceHolder.surface.isValid) return

        val canvas: Canvas = try {
            surfaceHolder.lockCanvas()
        } catch (e: Exception) {
            return
        } ?: return

        try {
            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

            val paint = Paint().apply {
                color = Color.GREEN
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 35f
                isAntiAlias = true
                setShadowLayer(2f, 2f, 2f, Color.BLACK)
            }

            val surfaceWidth = canvas.width.toFloat()
            val surfaceHeight = canvas.height.toFloat()

            for (result in detectedObjects) {
                val box = result.boundingBox

                val left = (box.left / 1000f * surfaceWidth).coerceIn(0f, surfaceWidth)
                val top = (box.top / 1000f * surfaceHeight).coerceIn(0f, surfaceHeight)
                val right = (box.right / 1000f * surfaceWidth).coerceIn(0f, surfaceWidth)
                val bottom = (box.bottom / 1000f * surfaceHeight).coerceIn(0f, surfaceHeight)

                canvas.drawRect(left, top, right, bottom, paint)

                val text = "${result.label} ${String.format("%.0f", result.confidence * 100)}%"
                canvas.drawText(text, left + 5, top - 10f, textPaint)
            }

        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas)
        }
    }

    private fun updateFPS() {
        fpsCounter++
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTime >= 1000) {
            fps = fpsCounter
            fpsCounter = 0
            lastTime = currentTime

            runOnUiThread {
                statsTextView.text = "FPS: $fps | Objekte: ${detectedObjects.size}"
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface created")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "MarioDetection"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
