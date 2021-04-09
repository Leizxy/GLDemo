package cn.leizy.gldemo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.databinding.DataBindingUtil
import cn.leizy.gldemo.camera.MyCameraX
import cn.leizy.gldemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), ImageAnalysis.Analyzer {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        permissions()
//        CameraHelper(binding.pre)
        camera()
    }

    fun camera() {
        binding.pre.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        reflection()
        MyCameraX(binding.pre, analyzer = this)
    }

    private fun reflection() {
        val previewView: PreviewView = binding.pre
        val clazz = previewView.javaClass
        Log.i("MainActivity", "reflection: ${clazz.name}")
//        Log.i("MainActivity", "reflection: ${clazz.fi}")
        for (filed in clazz.declaredFields) {
            Log.i("MainActivity", "reflection: ${filed.name}")
        }
        val mImplementation = clazz.getDeclaredField("mImplementation")
//        mImplementation.isAccessible = true
//        val o: PreviewViewImplementation = mImplementation.get()
        Log.i("MainActivity", "reflection: ${mImplementation.type}")
        val textClazz = mImplementation.javaClass
        Log.i("MainActivity", "reflection: texture -> ${textClazz.name}")
        for (filed in textClazz.declaredFields) {
            Log.i("MainActivity", "reflection: texture -> ${filed.name}")
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

    override fun onResume() {
        super.onResume()
//        binding.filter.onResume()
    }

    override fun onPause() {
        super.onPause()
//        binding.filter.onPause()
    }

    override fun analyze(image: ImageProxy) {
//        Log.i("MainActivity", "analyze: ")
        image.close()
    }

}