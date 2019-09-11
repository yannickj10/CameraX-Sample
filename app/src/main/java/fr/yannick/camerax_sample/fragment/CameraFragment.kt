package fr.yannick.camerax_sample.fragment

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.src.R
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.nio.ByteBuffer


class CameraFragment : Fragment() {

    private lateinit var config: CameraConfiguration

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view_finder.post { setupCamera() }
    }

    private fun setupCamera() {
        val metrics = DisplayMetrics().also { view_finder.display.getRealMetrics(it) }
        config = CameraConfiguration(
            aspectRatio = Rational(metrics.widthPixels, metrics.heightPixels),
            rotation = view_finder.display.rotation,
            resolution = Size(metrics.widthPixels, metrics.heightPixels)
        )

        CameraX.unbindAll()
        CameraX.bindToLifecycle(
            this,
            buildPreviewUseCase(),
            buildImageCaptureUseCase(),
            buildImageAnalysisUseCase()
        )
    }

    private fun buildPreviewUseCase(): Preview {
        val previewConfig = PreviewConfig.Builder()
            .setTargetAspectRatio(config.aspectRatio)
            .setTargetRotation(config.rotation)
            .setTargetResolution(config.resolution)
            .setLensFacing(config.lensFacing)
            .build()
        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener { previewOutput ->
            val parent = view_finder.parent as ViewGroup
            parent.removeView(view_finder)
            parent.addView(view_finder, 0)

            view_finder.surfaceTexture = previewOutput.surfaceTexture
        }

        return preview
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val captureConfig = ImageCaptureConfig.Builder()
            .setTargetAspectRatio(config.aspectRatio)
            .setTargetRotation(config.rotation)
            .setTargetResolution(config.resolution)
            .setFlashMode(config.flashMode)
            .setCaptureMode(config.captureMode)
            .build()
        val capture = ImageCapture(captureConfig)

        camera_capture_button.setOnClickListener {
            val fileName = "myPhoto"
            val fileFormat = ".jpg"
            val imageFile = createTempFile(fileName, fileFormat)

            capture.takePicture(imageFile, object : ImageCapture.OnImageSavedListener {
                override fun onImageSaved(file: File) {

                    val arguments = ImagePreviewFragment.arguments(file.absolutePath)
                    Navigation.findNavController(requireActivity(), R.id.mainContent)
                        .navigate(R.id.imagePreviewFragment, arguments)

                    Toast.makeText(requireContext(), "Image saved", Toast.LENGTH_LONG).show()
                }

                override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {

                    Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                    Log.e("CameraFragment", "Capture error $useCaseError: $message", cause)
                }
            })
        }

        return capture
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {

        val analysisConfig = ImageAnalysisConfig.Builder()
            .setImageReaderMode(config.readerMode)
            .setImageQueueDepth(config.queueDepth)
            .build()
        val analysis = ImageAnalysis(analysisConfig)

        analysis.setAnalyzer { image, rotationDegrees ->
            val buffer = image.planes[0].buffer
            // Extract image data from callback object
            val data = buffer.toByteArray()
            // Convert the data into an array of pixel values
            val pixels = data.map { it.toInt() and 0xFF }
            // Compute average luminance for the image
            val luma = pixels.average()
            Log.d("CameraFragment", "Luminance: $luma")
        }

        return analysis
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }
}