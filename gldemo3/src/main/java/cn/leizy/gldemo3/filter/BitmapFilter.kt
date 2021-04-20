package cn.leizy.gldemo3.filter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import cn.leizy.gldemo3.BitmapUtil
import android.opengl.GLES20 as G20
import cn.leizy.gldemo3.R
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Created by wulei
 * @date 2021/4/19, 019
 * @description
 */
class BitmapFilter(context: Context) :
    AbstractFboFilter(context, R.raw.text_vert_1, R.raw.text_shader) {
    private var waterTexId: Int = 0
    private var vboId: Int = 0
    private var markTextureId: Int = 0
    private lateinit var bitmap: Bitmap
    private val bitmapTopLeft = floatArrayOf(0f, 0f)
    override fun beforeInit(context: Context) {
        super.beforeInit(context)
//        bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.watermark)
        bitmap = BitmapUtil.getStrBitmap("水印\n相机")
        /*val r = 1.0f * bitmap.width / bitmap.height
        val w = r * 0.1f
        //↙
        vertex[12] = 0.8f - w
        vertex[13] = -0.8f

        vertex[14] = 0.8f
        vertex[15] = -0.8f

        vertex[8] = 0.8f - w
        vertex[9] = -0.7f

        vertex[10] = 0.8f
        vertex[11] = -0.7f

        for (i in vertex.indices) {
            Log.i("BitmapFilter", "beforeInit: ${vertex[i]}")
        }*/
        val bitmapTopLeft = floatArrayOf(0f, 0f)
        val width = 1080f
        val height = 1920f
        val bW = bitmap.width.toFloat() * 2 / width
        val bH = bitmap.height.toFloat() * 2 / height
        Log.i("BitmapFilter", "beforeInit: $bW,$bH")
        //↙
        vertex[12] = bitmapTopLeft[0]
        vertex[13] = bitmapTopLeft[1] - bH
        //↘
        vertex[14] = bitmapTopLeft[0] + bW
        vertex[15] = bitmapTopLeft[1] - bH
        //↖
        vertex[8] = bitmapTopLeft[0]
        vertex[9] = bitmapTopLeft[1]
        //↗
        vertex[10] = bitmapTopLeft[0] + bW
        vertex[11] = bitmapTopLeft[1]

        for (i in vertex.indices) {
            Log.i("BitmapFilter", "beforeInit: ${vertex[i]}")
        }
    }

    override fun getVertexCoord(): FloatArray {
        return floatArrayOf(
            -1.0f, -1.0f, //↙
            1.0f, -1.0f,  //↘
            -1.0f, 1.0f,  //↖
            1.0f, 1.0f,     //↗
            0f, 0f,     //水印预留
            0f, 0f,
            0f, 0f,
            0f, 0f
        )
    }

    /*override fun setSize(width: Int, height: Int) {
        val bW = bitmap.width.toFloat() * 2 / width.toFloat()
        val bH = bitmap.height.toFloat() * 2 / height.toFloat()
        Log.i("BitmapFilter", "setSize: $bW,$bH")
        //↙
        vertex[12] = bitmapTopLeft[0]
        vertex[13] = bitmapTopLeft[1] - bH
        //↘
        vertex[14] = bitmapTopLeft[0] + bW
        vertex[15] = bitmapTopLeft[1] - bH
        //↖
        vertex[8] = bitmapTopLeft[0]
        vertex[9] = bitmapTopLeft[1]
        //↗
        vertex[10] = bitmapTopLeft[0] + bW
        vertex[11] = bitmapTopLeft[1]

        for (i in vertex.indices) {
            Log.i("BitmapFilter", "beforeInit: ${vertex[i]}")
        }
//        vertexBuffer.clear()
//        vertexBuffer.put(vertex)
//        vertexBuffer.position(0)
        ByteBuffer.allocateDirect(vertex.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer().also {
                it.clear()
                it.put(vertex)
                vertexBuffer = it
                vertexBuffer.position(0)
            }
        super.setSize(width, height)
    }*/

    override fun initGLParams() {
        G20.glEnable(G20.GL_BLEND)
        G20.glBlendFunc(G20.GL_SRC_ALPHA, G20.GL_ONE_MINUS_SRC_ALPHA)
        super.initGLParams()
        createVBO()
        createWaterTextureId()
    }

    private fun createVBO() {
        //创建VBO
        val vbos = IntArray(1)
        G20.glGenBuffers(vbos.size, vbos, 0)
        vboId = vbos[0]
        //绑定VBO
        G20.glBindBuffer(G20.GL_ARRAY_BUFFER, vboId)
        //分配VBO需要的缓存大小
        G20.glBufferData(
            G20.GL_ARRAY_BUFFER,
            vertex.size * 4 + texture.size * 4,
            null,
            G20.GL_STATIC_DRAW
        )
        //设置VBO顶点数据
        G20.glBufferSubData(G20.GL_ARRAY_BUFFER, 0, vertex.size * 4, vertexBuffer)
        G20.glBufferSubData(G20.GL_ARRAY_BUFFER, vertex.size * 4, texture.size * 4, textureBuffer)
        //解绑VBO
        G20.glBindBuffer(G20.GL_ARRAY_BUFFER, 0)
    }

    private fun createWaterTextureId() {
        val textureIds = IntArray(1)
        //创建纹理
        G20.glGenTextures(1, textureIds, 0)
        waterTexId = textureIds[0]
        //绑定纹理
        G20.glBindTexture(G20.GL_TEXTURE_2D, waterTexId)
        //环绕
        G20.glTexParameteri(G20.GL_TEXTURE_2D, G20.GL_TEXTURE_WRAP_S, G20.GL_REPEAT)
        G20.glTexParameteri(G20.GL_TEXTURE_2D, G20.GL_TEXTURE_WRAP_T, G20.GL_REPEAT)
        //过滤
        G20.glTexParameteri(G20.GL_TEXTURE_2D, G20.GL_TEXTURE_MIN_FILTER, G20.GL_LINEAR)
        G20.glTexParameteri(G20.GL_TEXTURE_2D, G20.GL_TEXTURE_MAG_FILTER, G20.GL_LINEAR)

        val bitmapBuffer = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)//RGBA
        bitmap.copyPixelsToBuffer(bitmapBuffer)
        bitmapBuffer.flip()
        //设置内存大小绑定内存地址
        G20.glTexImage2D(
            G20.GL_TEXTURE_2D,
            0,
            G20.GL_RGBA,
            bitmap.width,
            bitmap.height,
            0,
            G20.GL_RGBA,
            G20.GL_UNSIGNED_BYTE,
            bitmapBuffer
        )
        //解绑纹理
        G20.glBindTexture(G20.GL_TEXTURE_2D, 0)
    }

    override fun onDraw(texture: Int): Int {
        G20.glBindFramebuffer(G20.GL_FRAMEBUFFER, frameBuffer!![0])

        G20.glClear(G20.GL_COLOR_BUFFER_BIT)
//        G20.glClearColor(1.0f,1.0f,1.0f)

        G20.glUseProgram(program)

        G20.glBindTexture(G20.GL_TEXTURE_2D, texture)

        G20.glEnableVertexAttribArray(vPosition)
        G20.glEnableVertexAttribArray(vCoord)

        useVboSetVertex()
        G20.glDrawArrays(G20.GL_TRIANGLE_STRIP, 0, 4)
        G20.glDisableVertexAttribArray(vPosition)
        G20.glDisableVertexAttribArray(vCoord)
        G20.glBindTexture(G20.GL_TEXTURE_2D, 0)
        drawWater()

        G20.glBindFramebuffer(G20.GL_FRAMEBUFFER, 0)
        return frameTextures!![0]
    }

    private fun useVboSetVertex() {
        G20.glBindBuffer(G20.GL_ARRAY_BUFFER, vboId)

        G20.glVertexAttribPointer(vPosition, 2, G20.GL_FLOAT, false, 2 * 4, 0)
        G20.glVertexAttribPointer(vCoord, 2, G20.GL_FLOAT, false, 2 * 4, vertex.size * 4)

        G20.glBindBuffer(G20.GL_ARRAY_BUFFER, 0)
    }

    private fun drawWater() {
        G20.glBindBuffer(G20.GL_ARRAY_BUFFER, vboId)
        G20.glBindTexture(G20.GL_TEXTURE_2D, waterTexId)

        G20.glEnableVertexAttribArray(vPosition)
        G20.glEnableVertexAttribArray(vCoord)

        G20.glVertexAttribPointer(vPosition, 2, G20.GL_FLOAT, false, 2 * 4, 2 * 4 * 4)
        G20.glVertexAttribPointer(vCoord, 2, G20.GL_FLOAT, false, 2 * 4, vertex.size * 4)

        G20.glDrawArrays(G20.GL_TRIANGLE_STRIP, 0, 4)

        G20.glDisableVertexAttribArray(vPosition)
        G20.glDisableVertexAttribArray(vCoord)

        G20.glBindTexture(G20.GL_TEXTURE_2D, 0)
        G20.glBindBuffer(G20.GL_ARRAY_BUFFER, 0)
    }
}