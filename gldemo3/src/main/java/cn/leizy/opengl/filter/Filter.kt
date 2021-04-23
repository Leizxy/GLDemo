package cn.leizy.opengl.filter

/**
 * @author Created by wulei
 * @date 2021/4/20, 020
 * @description
 */
interface Filter {
    fun create()

    fun setSize(width:Int, height:Int)

    fun draw(texture: Int): Int
}