package cn.leizy.gldemo2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import cn.leizy.gldemo2.camera.CameraHelper
import cn.leizy.gldemo2.gl.GLView

class MainActivity : AppCompatActivity() {
    private lateinit var glView: GLView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glView = findViewById(R.id.glview)
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions()
        } else {
            glView.startPreview()
        }
    }

    private fun permissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                ), 1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            glView.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    fun changeCamera(view: View) {
        glView.switchCamera()
        glView.changeScreenFilter(0)
    }

    fun changeFilter(view: View) {
        when(view.id){
            R.id.btn1 -> glView.changeScreenFilter(1)
            R.id.btn2 -> glView.changeScreenFilter(2)
            R.id.btn3 -> glView.changeScreenFilter(3)
            R.id.btn4 -> glView.changeScreenFilter(4)
        }
    }
}