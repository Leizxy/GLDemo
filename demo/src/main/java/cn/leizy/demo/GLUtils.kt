package cn.leizy.demo

import android.opengl.GLES20
import android.util.Log

/**
 * @author Created by wulei
 * @date 2021/4/8, 008
 * @description
 */
object GLUtils {
    private var programId: Int = 0
    private var vShader: Int = 0
    private var fShader: Int = 0

    fun loadProgram(vSource: String, fSource: String): Int {
        vShader = compileVertexShader(vSource)
        fShader = compileFragmentShader(fSource)
        return linkShader(vShader, fShader).also { programId = it }
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
}