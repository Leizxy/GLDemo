package cn.leizy.gldemo3.gl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.Surface
import cn.leizy.gldemo3.camera.CameraHelper
import cn.leizy.gldemo3.filter.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author Created by wulei
 * @date 2021/4/15, 015
 * @description
 */
class CameraRender2(private val cameraView: CameraView, private val surface: Surface) :
    GLSurfaceView.Renderer,
    SurfaceTexture.OnFrameAvailableListener {
    private lateinit var surfaceTexture: SurfaceTexture

    //    private lateinit var cameraTexture: CameraTexture
    private var cameraHelper: CameraHelper

    private var textures: IntArray = IntArray(1)
    var mtx = FloatArray(16)

    private lateinit var recordFilter: RecordFilter
    private lateinit var cameraFilter: CameraFilter
    private lateinit var splitFilter: SplitFilter
    private lateinit var textFilter: TextFilter
    private lateinit var bitmapFilter: BitmapFilter

    init {
        cameraHelper = CameraHelper(cameraView)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //创建一个SurfaceTexture用于接收相机的数据，并后续渲染
        val ids = GLUtils.getExternalGLTextureID()
        surfaceTexture = SurfaceTexture(ids[0])
        surfaceTexture.setDefaultBufferSize(cameraView.measuredWidth, cameraView.measuredHeight)
        //attach 之前必须先detach 不然会报错，以下2行貌似可以去掉。
        //在CameraX 1.0.0-alpha05版本里面生成SurfaceTexture传的texName是0，
        //所以先attach不会报错。
        surfaceTexture.detachFromGLContext()
        surfaceTexture.attachToGLContext(textures[0])

        surfaceTexture.setOnFrameAvailableListener(this)
        cameraHelper.addSurfaceTexture(surfaceTexture)
//        cameraHelper.addSurface(surface)
/*        cameraTexture = CameraTexture().also {
//            it.surfaceTexture.detachFromGLContext()
//            it.surfaceTexture.attachToGLContext(textures[0])
            it.surfaceTexture.setOnFrameAvailableListener(this@CameraRender2)
            cameraHelper.addSurfaceTexture(it.surfaceTexture)
        }*/
        cameraFilter = CameraFilter(cameraView.context)
        cameraFilter.init()
        splitFilter = SplitFilter(cameraView.context)
        splitFilter.init()
        recordFilter = RecordFilter(cameraView.context)
        recordFilter.init()
//        textFilter = TextFilter(cameraView.context)
//        textFilter.init()
        bitmapFilter = BitmapFilter(cameraView.context)
        bitmapFilter.init()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        cameraFilter.setSize(width, height)
        splitFilter.setSize(width, height)
        recordFilter.setSize(width, height)
        bitmapFilter.setSize(width, height)
//        textFilter.setText("hahah")
    }

    override fun onDrawFrame(gl: GL10?) {
        if (this::surfaceTexture.isInitialized) {
            surfaceTexture.updateTexImage()
            surfaceTexture.getTransformMatrix(mtx)
            cameraFilter.setTransformMatrix(mtx)
//            textFilter.setTransformMatrix(mtx)
            var id = textures[0]
            id = cameraFilter.onDraw(id)
//            id = splitFilter.onDraw(id)
//            id = textFilter.onDraw(id)
            id = bitmapFilter.onDraw(id)
            id = recordFilter.onDraw(id)
        }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        cameraView.requestRender()
    }

}