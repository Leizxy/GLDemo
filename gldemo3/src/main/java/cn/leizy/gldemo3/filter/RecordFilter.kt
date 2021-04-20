package cn.leizy.gldemo3.filter

import android.content.Context
import android.util.Log
import cn.leizy.gldemo3.R

/**
 * @author Created by wulei
 * @date 2021/4/16, 016
 * @description
 */
class RecordFilter(context: Context) : AbstractFilter(context, R.raw.base_vert, R.raw.base_frag) {
    /*override fun getVertexCoord(): FloatArray {
        return floatArrayOf(
            -1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f
        )
    }
    override fun getTextureCoord(): FloatArray {
        Log.i("RecordFilter", "getTextureCoord: ")
        return floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
        )
    }*/
}