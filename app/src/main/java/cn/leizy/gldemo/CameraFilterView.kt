package cn.leizy.gldemo

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import com.example.opengldemo.YUVRender

/**
 * @author Created by wulei
 * @date 2021/4/6, 006
 * @description
 */
class CameraFilterView : GLSurfaceView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        //OpenGL2.0
        setEGLContextClientVersion(2)
//        setRenderer(CameraRender(this, holder.surface))
//        setRenderer(TestRender())
        setRenderer(YUVRender(this))
//        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onResume() {
        super.onResume()
        requestRender()
    }

   /* override fun surfaceCreated(holder: SurfaceHolder) {
        Log.i("CameraFilterView", "surfaceCreated: ${Thread.currentThread().name}")
        super.surfaceCreated(holder)
    }
*/
}