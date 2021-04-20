package cn.leizy.gldemo3.gl

import android.graphics.Bitmap
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import javax.microedition.khronos.opengles.GL10

/**
 * @author wulei
 * @date 4/10/21
 * @description
 */
object GLUtils {
    fun getExternalGLTextureID(): IntArray {
        return IntArray(1).also {
            GLES20.glGenTextures(1, it, 0)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, it[0])
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE
            )
            Log.i("GLUtils", "getExternalGLTextureID: ${it[0]}")
        }
    }

    fun loadProgram(vSource: String, fSource: String): Int {
        val vShader = compileVertexShader(vSource)
        val fShader = compileFragmentShader(fSource)
        return linkShader(vShader, fShader)
    }


    private fun linkShader(vertexShaderId: Int, fragmentShaderId: Int): Int {
        //创建着色器程序
        val programId = GLES20.glCreateProgram()//程序句柄
        if (programId == 0) {
            Log.e("GLUtils", "linkShader: Could not create new program.")
            return 0
        }
        //绑定顶点和片元
        GLES20.glAttachShader(programId, vertexShaderId)
        GLES20.glAttachShader(programId, fragmentShaderId)
        //链接
        GLES20.glLinkProgram(programId)
        glProgramStatus(programId, "link")
        GLES20.glDeleteShader(vertexShaderId)
        GLES20.glDeleteShader(fragmentShaderId)
        return programId
    }

    private fun compileVertexShader(shaderCode: String): Int {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode)
    }

    private fun compileFragmentShader(shaderCode: String): Int {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode)
    }

    private fun compileShader(type: Int, code: String): Int {
        //创建shader
        val id = GLES20.glCreateShader(type)//返回shader句柄
        if (id == 0) {
            Log.e("ShaderCode", code)
            Log.e("GLUtils", "compileShader: Could not create new shader at the type $type")
            return 0
        }
        //加载shader代码
        GLES20.glShaderSource(id, code)
        //编译shader代码
        GLES20.glCompileShader(id)
        glShaderCompileStatus(
            id,
            if (type == GLES20.GL_VERTEX_SHADER) "load vShader" else "load fShader", code = code
        )
        return id
    }

    private fun glProgramStatus(programId: Int, tag: String = "") {
        val status = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            GLES20.glDeleteProgram(programId)
            throw IllegalStateException(
                "$tag error in program $programId: " + GLES20.glGetProgramInfoLog(programId)
            )
        }
    }

    private fun glShaderCompileStatus(
        shader: Int,
        tag: String = "",
        pName: Int = GLES20.GL_COMPILE_STATUS,
        code: String
    ) {
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, pName, status, 0)
        if (status[0] != GLES20.GL_TRUE) {
            Log.e("code", code)
            GLES20.glDeleteShader(shader)
            throw IllegalStateException(
                "error in $tag: " + GLES20.glGetShaderInfoLog(shader)
            )
        }
    }

    fun getAttributeHandle(program: Int, name: String): Int {
        return GLES20.glGetAttribLocation(program, name)
    }

    fun loadBitmapTexture(bitmap: Bitmap): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureIds[0]
    }
}