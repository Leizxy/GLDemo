package cn.leizy.gldemo2.gl.filter

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import cn.leizy.gldemo2.R

/**
 * @author Created by wulei
 * @date 2021/4/8, 008
 * @description
 */
class ScreenFilter(context: Context, id: Int = R.raw.camera_filters) :
    AbstractFilter(context, R.raw.camera_vert, id) {
    private var vMatrix: Int = 0
    private var typeHandle: Int = 0

    private var type: Int = 1
    fun changeFilter(id: Int) {
        type = id
    }

    override fun initGLParams() {
        super.initGLParams()
        //变换矩阵
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix")
        //type
        typeHandle = GLES20.glGetUniformLocation(program, "type")
    }

    override fun beforeDraw() {
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0)
        GLES20.glUniform1i(typeHandle, type)
    }
}

