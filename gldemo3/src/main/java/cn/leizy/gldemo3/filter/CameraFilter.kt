package cn.leizy.gldemo3.filter

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import cn.leizy.gldemo3.R

/**
 * @author Created by wulei
 * @date 2021/4/15, 015
 * @description
 */
class CameraFilter(context: Context) :
    AbstractFboFilter(context, R.raw.camera_vert, R.raw.camera_frag) {
    private lateinit var mtx: FloatArray
    private var vMatrix: Int = 0

    override fun initGLParams() {
        super.initGLParams()
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix")
        Log.i("CameraFilter", "initGLParams: ")
    }

    override fun beforeDraw(texture: Int) {
        super.beforeDraw(texture)
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0)
    }

    fun setTransformMatrix(mtx: FloatArray) {
        this.mtx = mtx
    }


}