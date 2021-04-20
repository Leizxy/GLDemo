package cn.leizy.gldemo3

import android.graphics.*

/**
 * @author Created by wulei
 * @date 2021/4/20, 020
 * @description
 */
object BitmapUtil {
    private var bitmap: Bitmap? = null
    private val paint = Paint()
    fun getStrBitmap(str: String): Bitmap {
        paint.color = Color.parseColor("#ff03DAC5")
        paint.textSize = 60f

        val metrics = paint.fontMetricsInt
        var height = metrics.bottom - metrics.top
        var width = Rect().let {
            paint.getTextBounds(str, 0, str.length, it)
            it.width()
        }
        width += 5
        height += 5
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } else {
            bitmap!!.width = width
            bitmap!!.height = height
        }
        val canvas = Canvas(bitmap!!)
        canvas.drawText(str, 0f, (-metrics.ascent).toFloat(), paint)
        canvas.save()
        return bitmap!!
    }
}