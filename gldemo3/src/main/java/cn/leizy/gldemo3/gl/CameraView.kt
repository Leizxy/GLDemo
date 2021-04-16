package cn.leizy.gldemo3.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import kotlin.math.roundToInt

/**
 * @author Created by wulei
 * @date 2021/4/15, 015
 * @description
 */
class CameraView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    private lateinit var cameraRender: CameraRender2
    private var aspectRatio = 0f

    constructor(context: Context?) : this(context, null) {
//        startPreview()
    }

    init {
        startPreview()
    }


    fun startPreview() {
        Log.i("CameraView", "startPreview: ")
        setEGLContextClientVersion(2)
        CameraRender2(this, holder.surface).also {
            cameraRender = it
        }
        setRenderer(cameraRender)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setAspectRatio(size: Size) {
        require(size.width > 0 && size.height > 0)
        aspectRatio = size.width.toFloat() / size.height.toFloat()
        holder.setFixedSize(size.width, size.height)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (aspectRatio == 0f) {
            setMeasuredDimension(width, height)
        } else {
            val newWidth: Int
            val newHeight: Int
            val actualRatio = if (width > height) aspectRatio else 1f / aspectRatio
            if (width > height * actualRatio) {
                newHeight = height
                newWidth = (height * actualRatio).roundToInt()
            } else {
                newWidth = width
                newHeight = (width / actualRatio).roundToInt()
            }
            Log.i("CameraView", "onMeasure: $width X $height")
            Log.i("CameraView", "onMeasure: $newWidth X $newHeight")
            setMeasuredDimension(newWidth, newHeight)
        }
    }
}