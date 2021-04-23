package cn.leizy.opengl.filter

import android.content.Context
import android.opengl.GLES20 as G20
import cn.leizy.gldemo3.R
import cn.leizy.opengl.util.GL20Utils
import javax.microedition.khronos.opengles.GL

/**
 * @author Created by wulei
 * @date 2021/4/22, 022
 * @description
 */
class CameraFilter(private val context: Context) : BaseFilter(context) {
    private var program: Int = 0

    //顶点坐标句柄
    private var vPosition: Int = 0

    //纹理坐标句柄
    private var vCoord: Int = 0

    //采样点坐标
    private var vTexture: Int = 0

    //摄像头矩阵句柄
    private var vMatrix: Int = 0

    private lateinit var mtx: FloatArray

    override fun onCreate() {
        program = GL20Utils.loadProgram(
            readRawTextFile(context, R.raw.camera_vert),
            readRawTextFile(context, R.raw.camera_frag)
        )
        vPosition = G20.glGetAttribLocation(program, "vPosition")
        vCoord = G20.glGetAttribLocation(program, "vCoord")
        vTexture = G20.glGetUniformLocation(program, "vTexture")
        //
        vMatrix = G20.glGetUniformLocation(program, "vMatrix")
    }

    override fun onSizeChanged(width: Int, height: Int) {
        //指定渲染区域
        G20.glViewport(0, 0, width, height)
    }

    fun setTransformMatrix(mtx: FloatArray) {
        this.mtx = mtx
    }

    override fun onDraw(texture: Int): Int {
        G20.glUseProgram(program)
        vertexBuffer.position(0)
        G20.glVertexAttribPointer(vPosition, 2, G20.GL_FLOAT, false, 0, vertexBuffer)
        G20.glEnableVertexAttribArray(vPosition)

        textureBuffer.position(0)
        G20.glVertexAttribPointer(vCoord, 2, G20.GL_FLOAT, false, 0, textureBuffer)
        G20.glEnableVertexAttribArray(vCoord)
        //使用第几个图层，这里设置第0个
        G20.glActiveTexture(G20.GL_TEXTURE0)
        //生成采样
        G20.glBindTexture(G20.GL_TEXTURE_2D, texture)
        G20.glUniform1i(vTexture, 0)
        beforeDraw(texture)
        G20.glDrawArrays(G20.GL_TRIANGLE_STRIP, 0, 4)
        G20.glBindTexture(G20.GL_TEXTURE_2D, 0)
        return texture
    }

    private fun beforeDraw(texture: Int) {
        G20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0)
    }
}