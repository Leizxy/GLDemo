package cn.leizy.gldemo2.gl.filter

import android.content.Context
import cn.leizy.gldemo2.R

/**
 * @author Created by wulei
 * @date 2021/4/15, 015
 * @description
 */
class SplitFilter(context: Context) :
    AbstractFboFilter(context, R.raw.base_vert, R.raw.split2_screen) {

    override fun onDraw(texture: Int): Int {
        super.onDraw(texture)
        return frameTextures!![0]
    }
}