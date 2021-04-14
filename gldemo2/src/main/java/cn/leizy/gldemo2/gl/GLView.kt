package cn.leizy.gldemo2.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import cn.leizy.gldemo2.camera.CameraHelper
import cn.leizy.gldemo2.gl.filter.ScreenFilter
import kotlin.math.roundToInt

/**
 * @author wulei
 * @date 4/10/21
 * @description
 */
class GLView constructor(context: Context, attributes: AttributeSet) :
    GLSurfaceView(context, attributes) {
    private lateinit var screenFilter: ScreenFilter
    private val cameraHelper = CameraHelper(this)
    private lateinit var cameraRender: CameraRender

    private var aspectRatio = 0f

    fun startPreview() {
        setEGLContextClientVersion(2)
        cameraRender = CameraRender(this, cameraHelper)
        screenFilter = ScreenFilter(context)
        cameraRender.addFilter(screenFilter)
        setRenderer(cameraRender)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun changeScreenFilter(id: Int) {
        screenFilter.changeFilter(id)
    }

    override fun onResume() {
        super.onResume()
        cameraRender.onResume()
        cameraHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraRender.onStop()
        cameraHelper.onStop()
    }

    fun switchCamera() {
        cameraHelper.switchCamera()
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
            Log.i("GLView", "onMeasure: $width X $height")
            Log.i("GLView", "onMeasure: $newWidth X $newHeight")
            setMeasuredDimension(newWidth, newHeight)
        }
    }

    override fun onDetachedFromWindow() {
        cameraHelper.onDestroy()
        super.onDetachedFromWindow()
        Log.i("GLView", "onDetachedFromWindow: ")
    }

}