package cn.leizy.gldemo.camera

import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * @author Created by wulei
 * @date 2021/4/6, 006
 * @description
 */
//todo textureview?
class MyCameraX(
    private val view: PreviewView,
    private val targetSize: Size = Size(0, 0),
    private val analyzer: ImageAnalysis.Analyzer
) {
    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }

    //    private val previewView = view as PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    init {
        Log.i("CameraHelper", ": init ${Thread.currentThread().name}")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(view.context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }
            Log.i("CameraHelper", ": ${Thread.currentThread().name}")
            bindCameraUses()
        }, ContextCompat.getMainExecutor(view.context))
    }

    private fun bindCameraUses() {
        val metrics = DisplayMetrics().also { view.display.getRealMetrics(it) }
        Log.i(
            "CameraHelper",
            "bindCameraUses: view display ${metrics.widthPixels} X ${metrics.heightPixels}"
        )
        val screenAspectRatio =
            if ((targetSize.width == 0) or (targetSize.height == 0)) {
                aspectRatio(targetSize.width, targetSize.height)
            } else {
                aspectRatio(metrics.widthPixels, metrics.heightPixels)
            }
        Log.i(
            "CameraHelper",
            "bindCameraUses: ${if (screenAspectRatio == AspectRatio.RATIO_4_3) "4:3" else "16:9"}"
        )
        val rotation = view.display.rotation
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        val preview =
            Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio).setTargetRotation(rotation)
                .build()

        val imgAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation).build().also {
                it.setAnalyzer(cameraExecutor, analyzer)
            }
        cameraProvider.unbindAll()
        try {
            cameraProvider.bindToLifecycle(
                view.context as LifecycleOwner,
                cameraSelector,
                preview,
                imgAnalyzer
            )
            preview.setSurfaceProvider(cameraExecutor, view.surfaceProvider)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }
}