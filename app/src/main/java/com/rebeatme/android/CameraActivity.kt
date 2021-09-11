package com.rebeatme.android

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import androidx.annotation.Nullable
import androidx.camera.core.Camera
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import android.graphics.Bitmap
import android.provider.MediaStore.Images.Media.getBitmap

import androidx.camera.core.ImageProxy



class CameraActivity : AppCompatActivity() {

    var poseDetector: PoseDetector? = null

    private var camera: Camera? = null
    private var videoCapture: VideoCapture? = null
    private var previewView: PreviewView? = null
    private var gameView: GameView? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var frameLayout: FrameLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_game_layout)
        previewView = findViewById(R.id.previewView)
        frameLayout = findViewById(R.id.frame_layout)
        cameraExecutor = Executors.newSingleThreadExecutor()

        //create game view and add it to frame
        val screenSize = Point()
        println("point x:" + screenSize.x + " y:" + screenSize.y)
        windowManager.defaultDisplay.getSize(screenSize)

        val scoreView = TextView(this)
        scoreView.text = "Score: 0"
        scoreView.textSize = 25f
        scoreView.gravity = Gravity.CENTER_HORIZONTAL
        scoreView.setTextColor(Color.parseColor("#333333"))
        scoreView.setTypeface(null, Typeface.BOLD)

        gameView = GameView(this, scoreView, screenSize.x, screenSize.y)

        frameLayout.addView(scoreView)

        val middleLineView = MiddleLineView(this, screenSize)
        frameLayout.addView(middleLineView)

        requestRuntimePermission()
    }

    private fun requestRuntimePermission() {
        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .withListener(multiplePermissionsListener)
                .check()
    }

    private val multiplePermissionsListener = object : ShortenMultiplePermissionListener() {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
            if (report.areAllPermissionsGranted()) {
                onPermissionGrant()
            } else {
                onPermissionDenied()
            }
        }
    }

    open class ShortenMultiplePermissionListener : MultiplePermissionsListener {
        override fun onPermissionsChecked(report: MultiplePermissionsReport) {
        }

        override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest?>?, token: PermissionToken?) {
            token?.continuePermissionRequest()
        }
    }

    private fun onPermissionGrant() {
        setupCameraProvider()
    }

    private fun onPermissionDenied() {
        showResultMessage("Poshel nahuy")
        finish()
    }

    private fun showResultMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupCameraProvider() {
        ProcessCameraProvider.getInstance(this).also { provider ->
            provider.addListener({
                bindPreview(provider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder()
                .build()

        // TODO Wait until they make it public for implementation
//        videoCapture = VideoCapture.Builder()
//            .build()

        val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

//        val imageCapture = ImageCapture.Builder()
//                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                .build()

        val options = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
        val poseDetector = PoseDetection.getClient(options)

        val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer(poseDetector, gameView))
                }
        cameraProvider.unbindAll()
        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(previewView?.surfaceProvider)
        } catch (exc: Exception) {
            Log.e("TAG", "Use case binding failed", exc)
        }

    }

   private class LuminosityAnalyzer(val poseDetector: PoseDetector, val gameview: GameView?) : ImageAnalysis.Analyzer {

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            println("!!!CALL")

            var bitmap = BitmapUtils.getBitmap(imageProxy)
            gameview?.setImage(bitmap)

            @SuppressLint("UnsafeExperimentalUsageError") val mediaImage: Image? = imageProxy.getImage()
            if (mediaImage != null) {
                var image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees())

                val currentTimeMillis = System.currentTimeMillis()
                val result: Task<Pose> = poseDetector.process(image)
                        .addOnSuccessListener(
                                OnSuccessListener(fun(pose: Pose?) {
                                    captureMovement(pose)

                                    val allPoseLandmarks = pose?.allPoseLandmarks
                                    println("pose landmarks: " + allPoseLandmarks?.size)
                                    val leftShoulder = pose?.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                                    val position = leftShoulder?.position
                                    println("!!!!!!!!!LEFT SHOULDER: $position")
                                }))
                        .addOnFailureListener(
                                OnFailureListener { e: java.lang.Exception? ->
                                    println("failure: " + e.toString())
                                })
                        .addOnCompleteListener(
                                OnCompleteListener(fun(pose: Task<Pose>?) {
                                    println("on_complete: ")
                                    imageProxy.close()
                                    mediaImage.close()
                                }))
            }
        }

       fun captureMovement(pose: Pose?) {
           val allPoseLandmarks = pose?.allPoseLandmarks
           println("pose landmarks: " + allPoseLandmarks?.size)

           val leftHand = listOf(
                   pose?.getPoseLandmark(PoseLandmark.LEFT_WRIST),
                   pose?.getPoseLandmark(PoseLandmark.LEFT_PINKY),
                   pose?.getPoseLandmark(PoseLandmark.LEFT_INDEX),
                   pose?.getPoseLandmark(PoseLandmark.LEFT_THUMB),
           )

           val rightHand = listOf(
                   pose?.getPoseLandmark(PoseLandmark.RIGHT_WRIST),
                   pose?.getPoseLandmark(PoseLandmark.RIGHT_PINKY),
                   pose?.getPoseLandmark(PoseLandmark.RIGHT_INDEX),
                   pose?.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
           )

           sendToGame(leftHand, rightHand)
       }

       private fun sendToGame(leftHand: List<PoseLandmark?>, rightHand: List<PoseLandmark?>) {
           gameview?.processRecognition(leftHand, rightHand)


       }
    }

    override fun onResume() {
        super.onResume()
        gameView?.resume()
        Handler().postDelayed({
            frameLayout.addView(gameView)
        }, 1000)
    }

    override fun onPause() {
        super.onPause()
        gameView?.pause()
    }

}