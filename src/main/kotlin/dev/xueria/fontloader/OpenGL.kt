package dev.xueria.fontloader

import org.lwjgl.opengl.GL11

object OpenGL {

    const val GL_TEXTURE_2D: Int = GL11.GL_TEXTURE_2D

    const val GL_TEXTURE_BINDING_2D: Int = GL11.GL_TEXTURE_BINDING_2D

    fun glGetInteger(p: Int): Int {
        return GL11.glGetInteger(p)
    }

    fun glGenTexture(): Int {
        return GL11.glGenTextures()
    }

    fun glDeleteTexture(id: Int) {
        GL11.glDeleteTextures(id)
    }

    fun glBindTexture(id: Int) {
        GL11.glBindTexture(GL_TEXTURE_2D, id)
    }

    fun <T> withTexture(texture: Int, block: () -> T): T {
        val previous = glGetInteger(GL_TEXTURE_BINDING_2D)
        glBindTexture(texture)
        try {
            return block()
        } finally {
            glBindTexture(previous)
        }
    }

}