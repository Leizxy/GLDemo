package cn.leizy.gldemo3.gl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.Surface
import cn.leizy.gldemo3.camera.CameraHelper
import cn.leizy.gldemo3.filter.CameraFilter
import cn.leizy.gldemo3.filter.SplitFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author Created by wulei
 * @date 2021/4/15, 015
 * @description
 */
class CameraRender(private val cameraView: CameraView, private val surface: Surface) :
    GLSurfaceView.Renderer,
    SurfaceTexture.OnFrameAvailableListener {
    private lateinit var cameraTexture: CameraTexture
    private var cameraHelper: CameraHelper
    private var textures: IntArray = IntArray(1)
    var mtx = FloatArray(16)

    private lateinit var cameraFilter: CameraFilter
    private lateinit var splitFilter: SplitFilter

    init {
        cameraHelper = CameraHelper(cameraView)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
//        cameraHelper.addSurface(surface)
        cameraTexture = CameraTexture().also {
//            it.surfaceTexture.detachFromGLContext()
//            it.surfaceTexture.attachToGLContext(textures[0])
            it.surfaceTexture.setOnFrameAvailableListener(this@CameraRender)
            cameraHelper.addSurfaceTexture(it.surfaceTexture)
        }
//        cameraFilter = CameraFilter(cameraView.context)
//        cameraFilter.init()
        splitFilter = SplitFilter(cameraView.context)
        splitFilter.init()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
//        cameraFilter.setSize(width, height)
        splitFilter.setSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (this::cameraTexture.isInitialized) {
            cameraTexture.surfaceTexture.updateTexImage()
            cameraTexture.draw(true)
            cameraTexture.surfaceTexture.getTransformMatrix(mtx)
//            cameraFilter.setTransformMatrix(mtx)
            var id = 0
//            id = cameraFilter.onDraw(cameraTexture.textureId[0])
            id = splitFilter.onDraw(id)
        }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        cameraView.requestRender()
    }

}