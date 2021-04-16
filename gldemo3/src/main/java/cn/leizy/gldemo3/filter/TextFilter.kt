package cn.leizy.gldemo3.filter

import android.content.Context
import android.graphics.*
import android.opengl.GLES20 as G20
import cn.leizy.gldemo3.R

/**
 * @author Created by wulei
 * @date 2021/4/16, 016
 * @description
 */
class TextFilter(context: Context) :
    AbstractFboFilter(context, R.raw.text_vert, R.raw.text_shader) {
    private var text: String? = null
    private var bitmap: Bitmap? = null
    private val paint = Paint()
    private var vMatrix: Int = 0
    private lateinit var mtx: FloatArray

    override fun initGLParams() {
        G20.glEnable(G20.GL_BLEND)
        G20.glPixelStorei(G20.GL_UNPACK_ALIGNMENT, 1)
        G20.glBlendFunc(G20.GL_NONE, G20.GL_ONE_MINUS_SRC_ALPHA)
        super.initGLParams()
        vMatrix = G20.glGetUniformLocation(program, "vMatrix")
    }

    fun setText(str: String) {
        this.text = str
        paint.color = Color.WHITE
        paint.textSize = 32f

        val metrics = paint.fontMetricsInt
        val height = metrics.bottom - metrics.top
        val width = Rect().let {
            paint.getTextBounds(str, 0, str.length, it)
            it.width()
        }
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } else {
            bitmap!!.width = width
            bitmap!!.height = height
        }
        val canvas = Canvas(bitmap!!)
        canvas.drawText(str, 0f, (-metrics.ascent).toFloat(), paint)
        canvas.save()
        setSize(width, height)
    }


    fun setTransformMatrix(mtx: FloatArray) {
        this.mtx = mtx
    }

    override fun beforeDraw(texture: Int) {
        storeImage(texture)
        super.beforeDraw(texture)
        G20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0)
        G20.glTexParameteri(G20.GL_TEXTURE_2D, G20.GL_TEXTURE_WRAP_S, G20.GL_REPEAT)
        G20.glTexParameteri(G20.GL_TEXTURE_2D, G20.GL_TEXTURE_WRAP_T, G20.GL_REPEAT)
//        G20.glTexImage2D(G20.GL_TEXTURE_2D,0,bitmap,0)
        G20.glGenerateMipmap(G20.GL_TEXTURE_2D)
    }

    private fun storeImage(texture: Int) {

    }

}