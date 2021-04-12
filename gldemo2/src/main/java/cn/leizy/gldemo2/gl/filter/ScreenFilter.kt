package cn.leizy.gldemo2.gl.filter

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import cn.leizy.gldemo2.R
import cn.leizy.gldemo2.gl.GLUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author Created by wulei
 * @date 2021/4/8, 008
 * @description
 */
class ScreenFilter(private val context: Context, id: Int = R.raw.camera_filters) :
    AbstractFilter() {

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
    private var vertexBuffer: FloatBuffer
    private var textureBuffer: FloatBuffer

    private var program: Int = 0

    private var vPosition: Int = 0
    private var vCoord: Int = 0
    private var vTexture: Int = 0
    private var vMatrix: Int = 0
    private var typeHandle: Int = 0

    private var type: Int = 1

    private lateinit var mtx: FloatArray

    private var mWidth = 0
    private var mHeight = 0

    init {
        ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).apply {
            vertexBuffer = asFloatBuffer()
        }
        vertexBuffer.clear()
        vertexBuffer.put(VERTEX)

        ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).apply {
            textureBuffer = asFloatBuffer()
        }
        textureBuffer.clear()
        textureBuffer.put(TEXTURE)

        val vertexShader = readRawTextFile(context, R.raw.camera_vert)
        val fragShader = readRawTextFile(context, id)
        //创建gpu程序
        loadProgram(vertexShader, fragShader)
        initGLParams()
    }

    fun changeFilter(id: Int) {
        type = id
    }

    private fun loadProgram(vShaderText: String, fShaderText: String) {
        program = GLUtils.loadProgram(vShaderText, fShaderText)
    }

    private fun initGLParams() {
        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        //接收纹理坐标，接收采样器采样图片的坐标
        vCoord = GLES20.glGetAttribLocation(program, "vCoord")
        //采样点坐标
        vTexture = GLES20.glGetUniformLocation(program, "vTexture")
        //变换矩阵
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix")
        //type
        typeHandle = GLES20.glGetUniformLocation(program, "type")
    }

    fun setSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    fun setTransformMatrix(mtx: FloatArray) {
        this.mtx = mtx
    }

    //渲染
    fun onDraw(texture: Int) {
        if (program == 0) return
        //大小
        GLES20.glViewport(0, 0, mWidth, mHeight)
        //使用程序
        GLES20.glUseProgram(program)
        //从0的地方开始读
        vertexBuffer.position(0)

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        //生效
        GLES20.glEnableVertexAttribArray(vPosition)

        textureBuffer.position(0)
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        GLES20.glEnableVertexAttribArray(vCoord)

        //gpu 读取数据
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //生成采样
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glUniform1i(vTexture, 0)
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0)
        GLES20.glUniform1i(typeHandle, type)
//        Log.i("ScreenFilter", "onDraw: $type")

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}

fun readRawTextFile(context: Context, rawId: Int): String {
    val `is` = context.resources.openRawResource(rawId)
    val br = BufferedReader(InputStreamReader(`is`))
    var line: String?
    val sb = StringBuilder()
    try {
        while (br.readLine().also { line = it } != null) {
            sb.append(line)
            sb.append("\n")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    try {
        br.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return sb.toString()
}