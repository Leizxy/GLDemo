package cn.leizy.gldemo2.gl.filter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.opengl.GLES20
import cn.leizy.gldemo2.R

/**
 * @author Created by wulei
 * @date 2021/4/14, 014
 * @description
 */
class TextFilter(context: Context) : AbstractFboFilter(context, R.raw.text_vert, R.raw.text_shader) {
    private lateinit var text: String
    private var bitmap: Bitmap? = null
    private val paint = Paint()
    fun setText(t: String) {
        this.text = t
        paint.setColor(Color.WHITE)
        paint.textSize = 32f

        val height = paint.fontMetricsInt.let {
            it.bottom - it.top
        }
        val width = Rect().let {
            paint.getTextBounds(t, 0, t.length, it)
            it.width()
        }
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } else {
            bitmap!!.width = width
            bitmap!!.height = height
        }
        setSize(width, height)
    }

    override fun beforeDraw(texture: Int) {
        super.beforeDraw(texture)
//        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,bitmap,0)
    }
}