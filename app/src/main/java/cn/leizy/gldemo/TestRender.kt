package cn.leizy.gldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author Created by wulei
 * @date 2021/4/7, 007
 * @description
 */
class TestRender : GLSurfaceView.Renderer {
    private var mProgram = 0
    private var vertexBuffer: FloatBuffer? = null
    private val vertexShaderCode = "attribute vec4 vPosition; " +
            "void main() {" +
            "gl_Position = vPosition;" +
            "}"

    private val fragmentShaderCode = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}"
    var triangleCoords = floatArrayOf(
        0.5f, 0.5f, 0.0f,  // top
        -0.5f, -0.5f, 0.0f,  // bottom left
        0.5f, -0.5f, 0.0f // bottom right
    )

    //设置颜色，依次为红绿蓝和透明通道
    var color = floatArrayOf(1.0f, 0f, 0f, 1.0f)

    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        val bb = ByteBuffer.allocateDirect(triangleCoords.size * 4)
            .also { it.order(ByteOrder.nativeOrder()) }
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer?.put(triangleCoords)
        vertexBuffer?.position(0)
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    private var mPositionHandle = 0
    private var mColorHandle = 0
    override fun onDrawFrame(gl: GL10?) {
        GLES20.glUseProgram(mProgram)
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }
}