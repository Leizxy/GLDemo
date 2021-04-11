package cn.leizy.gldemo2.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.SurfaceHolder
import cn.leizy.gldemo2.camera.CameraHelper

/**
 * @author wulei
 * @date 4/10/21
 * @description
 */
class GLView constructor(context: Context, attributes: AttributeSet) :
    GLSurfaceView(context, attributes) {
    private val cameraHelper = CameraHelper(this)

    fun startPreview() {
        setEGLContextClientVersion(2)
        setRenderer(CameraRender(this, cameraHelper))
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        super.surfaceCreated(holder)

    }

    override fun onResume() {
        super.onResume()
        cameraHelper.onResume()
    }

    override fun onPause() {
        super.onPause()
        cameraHelper.onStop()
    }

}