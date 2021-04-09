package cn.leizy.gldemo.camera

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.core.content.ContextCompat

/**
 * @author Created by wulei
 * @date 2021/4/8, 008
 * @description
 */
class CameraXGLView : GLSurfaceView {
    constructor(context: Context?) : super(context) {

    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

    }

    private final val surfaceProvider = object : Preview.SurfaceProvider {
        @SuppressLint("RestrictedApi", "UnsafeExperimentalUsageError")
        override fun onSurfaceRequested(request: SurfaceRequest) {
            val camera = request.camera
            request.setTransformationInfoListener(ContextCompat.getMainExecutor(context)) {
                val isFront =
                    camera.cameraInfoInternal.lensFacing == CameraSelector.LENS_FACING_FRONT


            }
        }
    }
}