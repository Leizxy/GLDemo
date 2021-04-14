package cn.leizy.gldemo2.gl

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import cn.leizy.gldemo2.R
import cn.leizy.gldemo2.camera.CameraHelper
import cn.leizy.gldemo2.gl.filter.AbstractFilter
import cn.leizy.gldemo2.gl.filter.ScreenFilter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author wulei
 * @date 4/10/21
 * @description
 */

const val VERTEX_SHADER = "attribute vec4 vPosition;" +
        "attribute vec2 inputTextureCoordinate;" +
        "varying vec2 textureCoordinate;" +
        "void main() {" +
        "gl_Position = vPosition;" +
        "textureCoordinate = inputTextureCoordinate;" +
        "}"
const val FRAGMENT_SHADER = "" +
        "#extension GL_OES_EGL_image_external : require\n" +
        "precision mediump float;" +
        "varying vec2 textureCoordinate;" +
        "uniform samplerExternalOES s_texture;" +
        "void main() {" +
        "gl_FragColor = texture2D(s_texture, textureCoordinate);" +
        "}"
val VERTEXES = floatArrayOf(
    -1.0f, 1.0f,
    -1.0f, -1.0f,
    1.0f, -1.0f,
    1.0f, 1.0f,
)

val TEXTURE_BACK = floatArrayOf(
    0.0f, 1.0f,
    1.0f, 1.0f,
    1.0f, 0.0f,
    0.0f, 0.0f,
)
val TEXTURE_FRONT = floatArrayOf(
    1.0f, 1.0f,
    0.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f
)
val VERTEX_ORDER = byteArrayOf(
    0, 1, 2, 3
)

class CameraTexture {
    var textureId: IntArray
    var surfaceTexture: SurfaceTexture
    private var vertexBuffer: FloatBuffer
    private var backTextureBuffer: FloatBuffer
    private var frontTextureBuffer: FloatBuffer
    private var drawBuffer: ByteBuffer

    var program: Int = 0
    private var positionHandle = 0
    private var textureHandle = 0

    init {
        textureId = GLUtils.getExternalGLTextureID()
        surfaceTexture = SurfaceTexture(textureId[0])
        vertexBuffer = ByteBuffer.allocateDirect(VERTEXES.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(VERTEXES).position(0)

        backTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_BACK.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        backTextureBuffer.clear()
        backTextureBuffer.put(TEXTURE_BACK).position(0)

        frontTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_FRONT.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        frontTextureBuffer.clear()
        frontTextureBuffer.put(TEXTURE_FRONT).position(0)

        drawBuffer = ByteBuffer.allocateDirect(VERTEX_ORDER.size).order(ByteOrder.nativeOrder())
        drawBuffer.clear()
        drawBuffer.put(VERTEX_ORDER).position(0)

        program = GLUtils.loadProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        initGLParameters(program)
    }

    private fun initGLParameters(program: Int) {
        positionHandle = GLUtils.getAttributeHandle(program, "vPosition")
        textureHandle = GLUtils.getAttributeHandle(program, "inputTextureCoordinate")
    }


    fun draw(/*textureId: Int,*/ isFront: Boolean = false) {
        GLES20.glUseProgram(program)
        GLES20.glEnable(GLES20.GL_CULL_FACE)//启动剔除
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])

        //句柄生效
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        if (isFront) {
            GLES20.glVertexAttribPointer(
                textureHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                frontTextureBuffer
            )
        } else {
            GLES20.glVertexAttribPointer(
                textureHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                backTextureBuffer
            )
        }
        GLES20.glEnableVertexAttribArray(textureHandle)

        //绘制
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLE_FAN,
            VERTEX_ORDER.size,
            GLES20.GL_UNSIGNED_BYTE,
            drawBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(textureHandle)

    }
}


class CameraRender(private val glView: GLSurfaceView, private val cameraHelper: CameraHelper) :
    GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private val mtx = FloatArray(16)
    private var isBackState: Boolean = false
    private var textureId: Int = 0
    var surfaceTexture: SurfaceTexture? = null
    private lateinit var cameraTexture: CameraTexture

    private var preState = cameraHelper.isFront()

//    lateinit var screenFilter: ScreenFilter

    private val filters = mutableListOf<AbstractFilter>()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.i("CameraRender", "onSurfaceCreated: ")
        cameraTexture = CameraTexture()
        surfaceTexture = cameraTexture.surfaceTexture
        surfaceTexture?.setOnFrameAvailableListener(this)

        cameraHelper.addSurfaceTexture(surfaceTexture!!)
//        cameraHelper.startPreview()
//        screenFilter = ScreenFilter(glView.context, R.raw.camera_filters)
        filters.forEach { it.init() }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
//        screenFilter.setSize(width, height)
        filters.forEach {
            it.setSize(width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
//        GLES20.glClearColor(0f, 0f, 0f, 0f)
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (this::cameraTexture.isInitialized) {
            surfaceTexture?.updateTexImage()
            if (preState == cameraHelper.isFront()) {
                cameraTexture.draw(cameraHelper.isFront())
            } else {
                preState = cameraHelper.isFront()
            }
            surfaceTexture?.getTransformMatrix(mtx)
            filters.forEach {
                it.setTransformMatrix(mtx)
                it.onDraw(cameraTexture.textureId[0])
            }
//            screenFilter.setTransformMatrix(mtx)
//            screenFilter.onDraw(cameraTexture.textureId[0])
        }
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        if (isBackState) return
        glView.requestRender()
    }

    fun onResume() {
        isBackState = false
    }

    fun onStop() {
        isBackState = true
    }

    fun addFilter(filter: AbstractFilter) {
        Log.i("CameraRender", "addFilter: ")
//        this.screenFilter = screenFilter
        filters.add(filter)
    }

    fun removeFilter(filter: AbstractFilter) {
        filters.remove(filter)
    }
}
