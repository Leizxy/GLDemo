package cn.leizy.gldemo2.camera

import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import android.view.Display
import android.view.SurfaceHolder
import kotlin.math.max
import kotlin.math.min

/**
 * @author wulei
 * @date 4/10/21
 * @description
 */
class SmartSize(width: Int, height: Int) {
    var size = Size(width, height)
    var long = max(size.width, size.height)
    var short = min(size.width, size.height)
    override fun toString() = "SmartSize ${long} x $short"
}

val SIZE_1080: SmartSize = SmartSize(1920, 1080)

fun getDisplaySmartSize(display: Display): SmartSize {
    return Point().run {
        display.getRealSize(this)
        SmartSize(x, y)
    }
}

fun <T> getPreviewOutputSize(
    display: Display,
    characteristics: CameraCharacteristics,
    targetClass: Class<T>,
    format: Int? = null
): Size {
    val maxSize = getDisplaySmartSize(display).run {
        if ((long >= SIZE_1080.long) || (short >= SIZE_1080.short))
            SIZE_1080
        else
            this
    }
    val validSizes =
        //获取摄像头相关参数
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            //根据format或者target获取所有输出尺寸
            .let {
                if (format == null)
                    it.getOutputSizes(targetClass)
                else
                    it.getOutputSizes(format)
            }
            //排序
            .sortedWith(compareBy { it.height * it.width })
            //转换
            .map { SmartSize(it.width, it.height) }
            //反转
            .reversed()
    return validSizes.first { it.long <= maxSize.long && it.short <= maxSize.short }.size
}