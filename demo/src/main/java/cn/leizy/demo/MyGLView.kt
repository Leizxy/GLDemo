package cn.leizy.demo

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Size
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.lifecycle.LifecycleOwner
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author Created by wulei
 * @date 2021/4/8, 008
 * @description
 */
class MyGLView : GLSurfaceView {
    private lateinit var filterRender: FilterRender

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attributeSet: AttributeSet) : super(context, attributeSet) {
        setEGLContextClientVersion(2)
        filterRender = FilterRender(this)
        setRenderer(filterRender)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun changeFilter(id: Int) {
        filterRender.changeFilter(id)
    }
}

class FilterRender(private val glView: MyGLView, private val id: Int = R.raw.camera_filters) :
    GLSurfaceView.Renderer,
    Preview.OnPreviewOutputUpdateListener,
    SurfaceTexture.OnFrameAvailableListener {
    private var cameraHelper: CameraHelper
    private var cameraTexture: SurfaceTexture? = null
    private var textures: IntArray = IntArray(1)
    private lateinit var screenFilter: ScreenFilter
    private val mtx = FloatArray(16)

    init {
        cameraHelper = CameraHelper(glView.context as LifecycleOwner, this)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        cameraTexture?.apply {
            attachToGLContext(textures[0])
            setOnFrameAvailableListener(this@FilterRender)
            screenFilter = ScreenFilter(glView.context, id)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        screenFilter.setSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        cameraTexture?.updateTexImage()
        cameraTexture?.getTransformMatrix(mtx)
        screenFilter.setTransformMatrix(mtx)
        screenFilter.onDraw(textures[0])
    }

    override fun onUpdated(output: Preview.PreviewOutput?) {
        cameraTexture = output?.surfaceTexture
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        glView.requestRender()
    }

    fun changeFilter(id: Int) {
        screenFilter.changeFilter(id)
    }
}

class CameraHelper(
    lifecycleOwner: LifecycleOwner,
    private val listener: Preview.OnPreviewOutputUpdateListener
) {
    init {
        val previewConfig = PreviewConfig.Builder().setTargetResolution(Size(1920, 1080))
            .setLensFacing(CameraX.LensFacing.BACK).build()
        Preview(previewConfig).apply {
            onPreviewOutputUpdateListener = listener
            CameraX.bindToLifecycle(lifecycleOwner, this)
        }
    }
}
