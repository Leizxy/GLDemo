package cn.leizy.gldemo3.filter

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import cn.leizy.gldemo3.gl.GLUtils

/**
 * @author Created by wulei
 * @date 2021/4/15, 015
 * @description
 */
abstract class AbstractFboFilter(context: Context, vertexShaderId: Int, fragmentShaderId: Int) :
    AbstractFilter(context, vertexShaderId, fragmentShaderId) {
    protected var frameBuffer: IntArray? = null
    protected var frameTextures: IntArray? = null

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        releaseFrame()
        frameBuffer = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffer, 0)
        Log.i("AbstractFboFilter", "${javaClass.simpleName} setSize: $width,$height frameBuffer ${frameBuffer!![0]}")
        //生成纹理
//        frameTextures = GLUtils.getExternalGLTextureID()
        frameTextures = IntArray(1)
        GLES20.glGenTextures(frameTextures!!.size, frameTextures, 0)
        //配置纹理
        Log.i("AbstractFboFilter", "${javaClass.simpleName} setSize: frameTextures ${frameTextures!![0]}")
        for (i in frameTextures!!.indices) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures!![i])
            Log.i("AbstractFboFilter", "${javaClass.simpleName} setSize: for $i")
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_NEAREST
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures!![0])
        Log.i("AbstractFboFilter", "${javaClass.simpleName} setSize: ${frameTextures!![0]}")
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        //绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!![0])
        //绑定FBO和纹理
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            frameTextures!![0],
            0
        )
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.i("AbstractFboFilter", "${javaClass.simpleName} setSize: glFramebufferTexture2D error!")
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    }

    override fun onDraw(texture: Int): Int {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer!![0])
        super.onDraw(texture)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        return frameTextures!![0]
    }

    private fun releaseFrame() {
        if (frameTextures != null) {
            GLES20.glDeleteTextures(1, frameTextures, 0)
            frameTextures = null
        }
        if (frameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0)
        }
    }
}