package cn.leizy.gldemo2.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @author wulei
 * @date 4/10/21
 * @description
 */
data class CameraInfo(val cameraId: String, val size: Size)

class CameraHelper(private val view: View) {
    private var havePreviewing: Boolean = false
    private var previewOutputSize: Size? = null
    private var targetSurfaces = arrayListOf<Surface>()
    private var previewSurface: Surface? = null
    private var surfaceTexture: SurfaceTexture? = null
    private val cameraManager: CameraManager
    private val cameraList: MutableList<CameraInfo> = arrayListOf()
    private var session: CameraCaptureSession? = null
    private var captureRequest: CaptureRequest? = null

    private var cameraId = "1"
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    init {
        cameraManager = view.context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.cameraIdList.forEach {
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(it)
            val orientation =
                lensOrientationString(cameraCharacteristics[CameraCharacteristics.LENS_FACING]!!)
            val capabilities =
                cameraCharacteristics[CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES]!!
            val cameraConfig =
                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]!!

            if (capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE)) {
                cameraConfig.getOutputSizes(MediaRecorder::class.java).forEach { size ->
                    Log.i("CameraHelper", "info : Camera $it $orientation ($size)")
                    cameraList.add(CameraInfo(it, size))
                }
            }
        }

        view.post {
            previewOutputSize = getPreviewOutputSize(
                view.display,
                cameraManager.getCameraCharacteristics(cameraId),
                SurfaceTexture::class.java
            )
//            initializeCamera(view.context as LifecycleOwner)
        }
    }

    private inline fun lensOrientationString(value: Int) = when (value) {
        CameraCharacteristics.LENS_FACING_BACK -> "Back"
        CameraCharacteristics.LENS_FACING_FRONT -> "Front"
        CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
        else -> "Unknown"
    }


    private fun initializeCamera(lifecycleOwner: LifecycleOwner) =
        lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val camera: CameraDevice = openCamera(cameraManager, cameraId, cameraHandler)
            //先判断是否有surfacetexture，若有创建一个surface用于渲染
            surfaceTexture?.apply {
                previewOutputSize?.also {
                    Log.i("CameraHelper", "setSurfaceTexture: ${it.width} * ${it.height}")
                    setDefaultBufferSize(it.width, it.height)
                    previewSurface = Surface(surfaceTexture).also { s ->
                        targetSurfaces.add(s)
                    }
                }
            }
            if (targetSurfaces.isNotEmpty()) {
                session = createCaptureSession(camera, cameraHandler)
                captureRequest = previewSurface?.let {
                    val build = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    build.addTarget(it)
                    build.build()
                }
                startPreview()
            }
        }

    private suspend fun createCaptureSession(
        device: CameraDevice,
        handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine {
        device.createCaptureSession(
            targetSurfaces,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    Log.i("CameraHelper", "onConfigured: ")
                    it.resume(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.i("CameraHelper", "onConfigureFailed: ")
                    it.resumeWithException(RuntimeException(""))
                }
            },
            handler
        )
    }

    fun startPreview() {
        Log.i("CameraHelper", "startPreview: $captureRequest")
        Log.i("CameraHelper", "startPreview: $session")
        captureRequest?.let {
            havePreviewing = true
            session?.setRepeatingRequest(it, null, cameraHandler)
        }
    }

    fun openCamera(view: View) {
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        cameraHandler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                Log.i("CameraHelper", "onOpened: ")
                cont.resume(camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
                Log.i("CameraHelper", "onDisconnected: ")
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.i("CameraHelper", "onError: ")
                when (error) {
                    ERROR_CAMERA_DEVICE -> "Error device"
                    ERROR_CAMERA_DISABLED -> "Device disabled"
                    ERROR_CAMERA_IN_USE -> "Camera is using"
                    ERROR_CAMERA_SERVICE -> "Error service"
                    ERROR_MAX_CAMERAS_IN_USE -> "maximum cameras in use"
                    else -> "UNKNOW"
                }.also {
                    if (cont.isActive)
                        cont.resumeWithException(RuntimeException("Camera $cameraId error: ($error) $it").also { e -> e.printStackTrace() })
                }
            }
        }, cameraHandler)
    }

    fun addSurface(surface: Surface) {
        targetSurfaces.add(surface)
//        this.surface = surface
    }

    fun addSurfaceTexture(surfaceTexture: SurfaceTexture) {
        this.surfaceTexture = surfaceTexture
        initializeCamera(view.context as LifecycleOwner)
    }

    fun onResume() {
        if (havePreviewing)
            session?.setRepeatingRequest(captureRequest!!, null, cameraHandler)
    }

    fun onStop() {
        if (havePreviewing) {
            Log.i("CameraHelper", "onPause: ")
//            session?.abortCaptures()
            session?.stopRepeating()
        }
    }

    fun onDestroy() {
        previewSurface?.release()
    }

}