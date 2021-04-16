package cn.leizy.gldemo3

import android.Manifest
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionUtils.askPermission(this, arrayOf(Manifest.permission.CAMERA), 100, this::init)
    }

    private fun init() {
        setContentView(R.layout.activity_main)
        val img = findViewById<ImageView>(R.id.img)
        img.setImageBitmap(getBitmap("哈哈哈"))
    }

    private var bitmap: Bitmap? = null
    private val paint = Paint()

    private fun getBitmap(str: String): Bitmap {
        paint.color = Color.RED
        paint.textSize = 32f

        val metrics = paint.fontMetricsInt
        val height = metrics.bottom - metrics.top
        val width = Rect().let {
            paint.getTextBounds(str, 0, str.length, it)
            it.width()
        }
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } else {
            bitmap!!.width = width
            bitmap!!.height = height
        }
        val canvas = Canvas(bitmap!!)
        canvas.drawText(str, 0f, (-metrics.ascent).toFloat(), paint)
        canvas.save()
        return bitmap!!
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