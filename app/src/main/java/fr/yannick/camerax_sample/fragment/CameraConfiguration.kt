package fr.yannick.camerax_sample.fragment

import android.util.Rational
import android.util.Size
import androidx.camera.core.CameraX
import androidx.camera.core.FlashMode
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture


data class CameraConfiguration(
    val aspectRatio: Rational,
    val rotation: Int = 0,
    val resolution: Size,

    // Preview
    var lensFacing: CameraX.LensFacing = CameraX.LensFacing.BACK,

    // Image capture
    val flashMode: FlashMode = FlashMode.AUTO,
    val captureMode: ImageCapture.CaptureMode = ImageCapture.CaptureMode.MIN_LATENCY,

    // Image analysis
    val readerMode: ImageAnalysis.ImageReaderMode = ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE,
    val queueDepth: Int = 5
)