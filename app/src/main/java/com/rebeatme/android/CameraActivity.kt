package com.rebeatme.android

import android.Manifest
import android.annotation.SuppressLint
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
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

class CameraActivity : AppCompatActivity() {

    var poseDetector: PoseDetector? = null

    private var camera: Camera? = null
    private var videoCapture: VideoCapture? = null
    private var previewView: PreviewView? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var frameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_game_layout)
        previewView = findViewById(R.id.previewView)
//        frameLayout = findViewById(R.id.frameLayout)
        cameraExecutor = Executors.newSingleThreadExecutor()
        //create game view and add it to frame
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
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer(poseDetector))
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

    private class LuminosityAnalyzer(val poseDetector: PoseDetector) : ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            println("!!!CALL")

            @SuppressLint("UnsafeExperimentalUsageError") val mediaImage: Image? = imageProxy.getImage()
            if (mediaImage != null) {

                var image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees())

                val currentTimeMillis = System.currentTimeMillis()
                val result: Task<Pose> = poseDetector.process(image)
                        .addOnSuccessListener(
                                OnSuccessListener(fun(pose: Pose?) {
                                    val allPoseLandmarks = pose?.allPoseLandmarks
                                    println("pose landmarks: " + allPoseLandmarks?.size)
                                    val leftShoulder = pose?.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                                    val position = leftShoulder?.position
                                    println("!!!!!!!!!LEFT SHOULDER: $position")
                                    var instance = java.time.Instant.ofEpochMilli(System.currentTimeMillis());
                                    var localDateTime = java.time.LocalDateTime
                                            .ofInstant(instance, java.time.ZoneId.systemDefault());
                                    println("!!!!TIME $localDateTime")
                                    println("success")
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
    }

    override fun onPause() {
        super.onPause()
    }
//                val pose = result.result
//                val allPoseLandmarks = result.result.allPoseLandmarks
//                println("pose landmarks: " + allPoseLandmarks.size)
//                val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
//                val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
//                val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
//                val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
//                val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
//                val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
//                val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
//                val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
//                val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
//                val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
//                val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
//                val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
//                val leftPinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY)
//                val rightPinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
//                val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
//                val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
//                val leftThumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB)
//                val rightThumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
//                val leftHeel = pose.getPoseLandmark(PoseLandmark.LEFT_HEEL)
//                val rightHeel = pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
//                val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
//                val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)
//                val nose = pose.getPoseLandmark(PoseLandmark.NOSE)
//                val leftEyeInner = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
//                val leftEye = pose.getPoseLandmark(PoseLandmark.LEFT_EYE)
//                val leftEyeOuter = pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
//                val rightEyeInner = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
//                val rightEye = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)
//                val rightEyeOuter = pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
//                val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
//                val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
//                val leftMouth = pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
//                val rightMouth = pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)
//                println("!!!!!!!!!LEFT SHOULDER: $leftShoulder")

}