package cn.leizy.gldemo2.gl.filter

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import cn.leizy.gldemo2.gl.GLUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author Created by wulei
 * @date 2021/4/12, 012
 * @description
 */
abstract class AbstractFilter(private val context: Context, private val vertexShaderId: Int, private val fragmentShaderId: Int) {
    var mWidth: Int = 0
    var mHeight: Int = 0
    val vertexBuffer: FloatBuffer
    val textureBuffer: FloatBuffer
    lateinit var mtx: FloatArray
    private val VERTEX = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
    )
    private val TEXTURE = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f
    )

    var program: Int = 0
    var vPosition: Int = 0
    var vCoord: Int = 0
    var vTexture: Int = 0

    init {
        Log.i("AbstractFilter", "init{}")
        ByteBuffer.allocateDirect(4 * 4 * 2)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().also {
                it.put(VERTEX)
                vertexBuffer = it
            }
        ByteBuffer.allocateDirect(4 * 4 * 2)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().also {
                it.put(TEXTURE)
                textureBuffer = it
            }

    }

    fun init() {
        val vertexShader = readRawTextFile(context, vertexShaderId)
        val fragShader = readRawTextFile(context, fragmentShaderId)
        program = GLUtils.loadProgram(vertexShader, fragShader)
        initGLParams()
    }

    open fun initGLParams() {
        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        //接收纹理坐标，接收采样器采样图片的坐标
        vCoord = GLES20.glGetAttribLocation(program, "vCoord")
        //采样点坐标
        vTexture = GLES20.glGetUniformLocation(program, "vTexture")
    }

    open fun setSize(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    fun setTransformMatrix(mtx: FloatArray) {
        this.mtx = mtx
    }


    //渲染
    open fun onDraw(texture: Int): Int {
        if (program == 0) return 0
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

        //gpu 读取数据 使用第几个图层
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //生成采样
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glUniform1i(vTexture, 0)
//        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0)
//        GLES20.glUniform1i(typeHandle, type)
//        Log.i("ScreenFilter", "onDraw: $type")
        beforeDraw(texture)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        return texture
    }

    open fun beforeDraw(texture: Int) {

    }

    fun release() {
        GLES20.glDeleteProgram(program)
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