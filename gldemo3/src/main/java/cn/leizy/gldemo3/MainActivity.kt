package cn.leizy.gldemo3

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionUtils.askPermission(this, arrayOf(Manifest.permission.CAMERA), 100, this::init)
    }

    private fun init() {
        setContentView(R.layout.activity_main)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPer(requestCode == 100, grantResults, this::init) {
            finish()
        }
    }
}