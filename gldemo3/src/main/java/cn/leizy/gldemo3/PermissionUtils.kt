package cn.leizy.gldemo3

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

/**
 * @author Created by wulei
 * @date 2021/4/15, 015
 * @description
 */
object PermissionUtils {
    fun askPermission(context: Activity, permissions: Array<String>, req: Int, run: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val result = ActivityCompat.checkSelfPermission(context, permissions[0])
            if (result == PackageManager.PERMISSION_GRANTED) {
                run.invoke()
            } else {
                ActivityCompat.requestPermissions(context, permissions, req)
            }
        } else {
            run.invoke()
        }
    }

    fun onRequestPer(
        isReq: Boolean,
        grantResults: IntArray,
        okRun: () -> Unit,
        deniedRun: () -> Unit
    ) {
        if (isReq) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                okRun.invoke()
            } else {
                deniedRun.invoke()
            }
        }
    }
}