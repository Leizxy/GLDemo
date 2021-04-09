package cn.leizy.gldemo

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.*
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author Created by wulei
 * @date 2021/4/6, 006
 * @description
 */
var VERTEX = floatArrayOf(
    -1.0f, -1.0f,
    1.0f, -1.0f,
    -1.0f, 1.0f,
    1.0f, 1.0f
)

var TEXTURE = floatArrayOf(
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f
)
const val vertextShaderCode = "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec2 texCoord;" +
        "varying vec2 tc;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  tc = texCoord;" +
        "}"
const val fragmentShaderCode =
    "precision mediump float;" +
            "uniform sampler2D samplerY;" +
            "uniform sampler2D samplerU;" +
            "uniform sampler2D samplerV;" +
            "uniform sampler2D samplerUV;" +
            "uniform int yuvType;" +
            "varying vec2 tc;" +
            "void main() {" +
            "  vec4 c = vec4((texture2D(samplerY, tc).r - 16./255.) * 1.164);" +
            "  vec4 U; vec4 V;" +
            "  if (yuvType == 0){" +
            // 因为是YUV的一个平面，所以采样后的r,g,b,a这四个参数的数值是一样的
            "    U = vec4(texture2D(samplerU, tc).r - 128./255.);" +
            "    V = vec4(texture2D(samplerV, tc).r - 128./255.);" +
            "  } else if (yuvType == 1){" +
            // 因为NV12是2平面的，对于UV平面，在加载纹理时，会指定格式，让U值存在r,g,b中，V值存在a中
            "    U = vec4(texture2D(samplerUV, tc).r - 128./255.);" +
            "    V = vec4(texture2D(samplerUV, tc).a - 128./255.);" +
            "  } else {" +
            // 因为NV21是2平面的，对于UV平面，在加载纹理时，会指定格式，让U值存在a中，V值存在r,g,b中
            "    U = vec4(texture2D(samplerUV, tc).a - 128./255.);" +
            "    V = vec4(texture2D(samplerUV, tc).r - 128./255.);" +
            "  } " +
            "  c += V * vec4(1.596, -0.813, 0, 0);" +
            "  c += U * vec4(0, -0.392, 2.017, 0);" +
            "  c.a = 1.0;" +
            "  gl_FragColor = c;" +
            "}"

fun loadShader(type: Int, shaderCode: String): Int {
    return GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
    }
}

private fun checkGlError(op: String) {
    var error: Int = GLES20.glGetError()
    while (error != GLES20.GL_NO_ERROR) {
        Log.e("GLES", "***** $op: glError $error")
        error = GLES20.glGetError()
    }
}

class CameraRender(private val view: CameraFilterView, private val surface: Surface) :
    GLSurfaceView.Renderer,
    ImageAnalysis.Analyzer {

    private var program: Int = 0
    private var planeTextureHandles = IntBuffer.wrap(IntArray(3))
    private var width: Int = 0
    private var height: Int = 0
    private val cameraHelper: CameraHelper = CameraHelper(view, analyzer = this)
    private val surfaceTexture: SurfaceTexture? = null
    private val lock: ReentrantLock = ReentrantLock()
    private lateinit var y: ByteBuffer
    private lateinit var u: ByteBuffer
    private lateinit var v: ByteBuffer
    private var uv: ByteBuffer = ByteBuffer.allocate(0)
    private lateinit var nv21: ByteArray
    private lateinit var nv21_rotated: ByteArray

    var vertexBuffer //顶点坐标缓存区
            : FloatBuffer? = null
    var textureBuffer // 纹理坐标
            : FloatBuffer? = null

    // handles
    private var mPositionHandle = -1
    private var mCoordHandle = -1
    private var mVPMatrixHandle: Int = -1

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val mSampleHandle = IntArray(3)

    // vertices buffer
    private var mVertexBuffer: FloatBuffer? = null
    private var mCoordBuffer: FloatBuffer? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.i("CameraRender", "onSurfaceCreated: ")
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertextShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().also {
            checkGlError("glCreateProgram")
            // add the vertex shader to program
            GLES20.glAttachShader(it, vertexShader)

            // add the fragment shader to program
            GLES20.glAttachShader(it, fragmentShader)

            // creates OpenGL ES program executables
            GLES20.glLinkProgram(it)
        }
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(program)
            program = 0
        }
        checkGlError("glCreateProgram")
        GLES20.glGenTextures(3, planeTextureHandles)
        checkGlError("glGenTextures")
        vertexBuffer =
            ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer?.clear()
        vertexBuffer?.put(VERTEX)

        textureBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        textureBuffer?.clear()
        textureBuffer?.put(TEXTURE)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.i("CameraRender", "onSurfaceChanged: $width $height")
        GLES20.glViewport(0, 0, width, height)
        if (width > 0 && height > 0) {

        }

    }

    override fun onDrawFrame(gl: GL10?) {
        if (!this::y.isInitialized) return
        /*Log.i(
            "CameraRender",
            "onDrawFrame: ${y.capacity()}, ${u.capacity()}, ${v.capacity()}"
        )*/
        synchronized(this) {
            if (y.capacity() > 0) {
                y.position(0)
                u.position(0)
                v.position(0)
//                uv.position(0)
                feedTextureImageData(y, u, v, width, height)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
                try {
                    drawTexture(vPMatrix)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun drawTexture(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)
        checkGlError("glUseProgram")

        mPositionHandle = GLES20.glGetAttribLocation(program, "vPosition").also {
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
            GLES20.glEnableVertexAttribArray(it)
        }

        mCoordHandle = GLES20.glGetAttribLocation(program, "texCoord").also {
            GLES20.glVertexAttribPointer(it, 2, GLES20.GL_FLOAT, false, 8, textureBuffer)
            GLES20.glEnableVertexAttribArray(it)
        }
        mVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, mvpMatrix, 0)

        val yuvType = GLES20.glGetUniformLocation(program, "yuvType")
        checkGlError("glGetUniformLocation yuvtype")
        GLES20.glUniform1i(yuvType, 0)
        mSampleHandle[0] = GLES20.glGetUniformLocation(program, "samplerY")
        mSampleHandle[1] = GLES20.glGetUniformLocation(program, "samplerU")
        mSampleHandle[2] = GLES20.glGetUniformLocation(program, "samplerV")
        (0 until 3).forEach {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + it)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planeTextureHandles[it])
            GLES20.glUniform1i(mSampleHandle[it], it)
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")
        GLES20.glFinish()

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mCoordHandle)
    }

    private fun feedTextureImageData(
        y: ByteBuffer,
        u: ByteBuffer,
        v: ByteBuffer,
        width: Int,
        height: Int
    ) {
        textureYUV(y, width, height, 0)
        textureYUV(u, width / 2, height / 2, 1)
        textureYUV(v, width / 2, height / 2, 2)
    }

    private fun feedTextureImageData(y: ByteBuffer, uv: ByteBuffer, width: Int, height: Int) {
        textureYUV(y, width, height, 0)
        textureNV21(uv, width / 2, height / 2, 1)
    }

    private fun textureNV21(imageData: ByteBuffer, width: Int, height: Int, index: Int) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planeTextureHandles[index])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, width, height, 0,
            GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, imageData
        )
    }

    private fun textureYUV(buffer: ByteBuffer, width: Int, height: Int, index: Int) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planeTextureHandles[index])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_LUMINANCE,
            width,
            height,
            0,
            GLES20.GL_LUMINANCE,
            GLES20.GL_UNSIGNED_BYTE,
            buffer
        )
    }

    override fun analyze(image: ImageProxy) {
//        Log.i("CameraRender", "analyze: " + image.format)
        lock.lock()
        width = image.width
        height = image.height
        val planes = image.planes
        if (!this::y.isInitialized) {
            val ySize = width * height
            y = ByteBuffer.allocate(ySize).order(ByteOrder.nativeOrder())
            u = ByteBuffer.allocate(ySize / 2).order(ByteOrder.nativeOrder())
            v = ByteBuffer.allocate(ySize / 2).order(ByteOrder.nativeOrder())
        }
        y.clear()
        y.put(planes[0].buffer)
        y.position(0)
        u.clear()
        u.put(planes[1].buffer)
        u.position(0)
        v.clear()
        v.put(planes[2].buffer)
        v.position(0)
        view.requestRender()
        lock.unlock()
        image.close()
    }

}