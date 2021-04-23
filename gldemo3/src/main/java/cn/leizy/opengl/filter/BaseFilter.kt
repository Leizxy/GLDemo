package cn.leizy.opengl.filter

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author Created by wulei
 * @date 2021/4/20, 020
 * @description
 */
abstract class BaseFilter(context: Context) : Filter {
    //和VERTEX_POS，TEXTURE_POS有关系，如果2个值代表一个坐标就是2(vec2)，3个值代表一个坐标就是3(vec3)。
    protected var coordSize = 2
    //顶点坐标
    protected var VERTEX_POS = floatArrayOf(
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
    )

    //纹理坐标
    protected var TEXTURE_POS = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f
    )

    //顶点坐标buffer
    protected lateinit var vertexBuffer: FloatBuffer

    //纹理坐标buffer
    protected lateinit var textureBuffer: FloatBuffer

    init {
        initBuffer()
    }

    final override fun create() {
        onCreate()
    }

    final override fun setSize(width: Int, height: Int) {
        onSizeChanged(width, height)
    }

    final override fun draw(texture: Int): Int {
        return onDraw(texture)
    }

    //创建program
    //创建对应句柄
    abstract fun onCreate()

    abstract fun onSizeChanged(width: Int, height: Int)

    abstract fun onDraw(texture: Int): Int

    protected fun initBuffer() {
        ByteBuffer.allocateDirect(VERTEX_POS.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                it.clear()
                it.put(VERTEX_POS)
                it.position(0)
                vertexBuffer = it
            }
        ByteBuffer.allocateDirect(TEXTURE_POS.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().also {
                it.clear()
                it.put(TEXTURE_POS)
                it.position(0)
                textureBuffer = it
            }
    }

    protected fun readRawTextFile(context: Context, rawId: Int): String {
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
}