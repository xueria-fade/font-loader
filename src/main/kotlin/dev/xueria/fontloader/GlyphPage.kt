package dev.xueria.fontloader

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GLContext
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.nio.ByteBuffer

class GlyphPage(
    private val font: Font,
    private val size: Int = Constants.AtlasPageSize
) : AutoCloseable {

    companion object {
        private const val GlyphPadding: Int = 4
    }

    private val texture: Int = OpenGL.glGenTexture()
    private val root: AreaNode = AreaNode(0, 0, size, size)
    private var buffer: ByteBuffer = BufferUtils.createByteBuffer(1024)

    init {
        OpenGL.withTexture(texture = texture) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE)
            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D, 0,
                GL11.GL_RGBA,
                size, size, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null as ByteBuffer?
            )
        }
    }

    fun trySubmit(codepoint: Int): SubmitResult {
        val renderContext = font.renderContext
        val glyphVector = font.createGlyphVector(renderContext, String(Character.toChars(codepoint)))
        val glyphMetrics = glyphVector.getGlyphMetrics(0)
        val advance = glyphMetrics.advanceX
        val box = glyphVector.getGlyphPixelBounds(0, renderContext, 0f, 0f)
        val glyphWidth = box.width
        val glyphHeight = box.height

        // 空白字符
        if (box.isEmpty) {
            val glyph = RasterizedGlyph(
                codepoint = codepoint,
                bounds = Bounds.Empty,
                uv = Bounds.Empty,
                advance = advance,
                texture = texture
            )
            return SubmitResult.Succuss(glyph)
        }

        val paddedWidth = glyphWidth + GlyphPadding * 2
        val paddedHeight = glyphHeight + GlyphPadding * 2

        // 如果这个字符大于整个画布
        // 返回失败结果
        // 虽然不太可能出现这种情况
        if (paddedWidth > size || paddedHeight > size) {
            return SubmitResult.Failure
        }

        val node = root.insert(paddedWidth, paddedHeight)
            ?: return SubmitResult.Outbound

        val image = BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
            graphics.color = Color.WHITE
            graphics.drawGlyphVector(glyphVector, GlyphPadding - box.x.toFloat(), GlyphPadding - box.y.toFloat())
        } finally {
            graphics.dispose()
        }

        val required = paddedWidth * paddedHeight * 4
        if (buffer.capacity() < required) {
            buffer = BufferUtils.createByteBuffer(required)
        }
        buffer.clear()
        val argb = (image.raster.dataBuffer as DataBufferInt).data
        var index = 0
        while (index < argb.size) {
            val c = argb[index]
            buffer.put(-1)
            buffer.put(-1)
            buffer.put(-1)
            buffer.put((c ushr 24).toByte())
            index++
        }
        buffer.flip()
        OpenGL.withTexture(texture = texture) {
            GL11.glTexSubImage2D(
                GL11.GL_TEXTURE_2D, 0,
                node.x, node.y,
                paddedWidth, paddedHeight,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer
            )
        }

        val glyph = RasterizedGlyph(
            codepoint = codepoint,
            bounds = Bounds(
                left = box.x.toFloat(),
                top = box.y.toFloat(),
                right = (box.x + box.width).toFloat(),
                bottom = (box.y + box.height).toFloat()
            ),
            uv = Bounds(
                left = (node.x + GlyphPadding) / size.toFloat(),
                top = (node.y + GlyphPadding) / size.toFloat(),
                right = (node.x + GlyphPadding + glyphWidth) / size.toFloat(),
                bottom = (node.y + GlyphPadding + glyphHeight) / size.toFloat()
            ),
            advance = advance,
            texture = texture
        )

        return SubmitResult.Succuss(glyph)
    }

    override fun close() {
        OpenGL.glDeleteTexture(texture)
    }

    class AreaNode(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    ) {
        private var left: AreaNode? = null
        private var right: AreaNode? = null
        private var occupied: Boolean = false

        fun insert(glyphWidth: Int, glyphHeight: Int): AreaNode? {
            if (left != null && right != null) {
                return left?.insert(glyphWidth, glyphHeight) ?: right?.insert(glyphWidth, glyphHeight)
            }

            if (occupied) {
                return null
            }

            if (glyphWidth > width || glyphHeight > height) {
                return null
            }

            if (glyphWidth == width && glyphHeight == height) {
                occupied = true
                return this
            }

            val deltaWidth = width - glyphWidth
            val deltaHeight = height - glyphHeight

            if (deltaWidth > deltaHeight) {
                left = AreaNode(x, y, glyphWidth, height)
                right = AreaNode(x + glyphWidth + 1, y, width - glyphWidth - 1, height)
            } else {
                left = AreaNode(x, y, width, glyphHeight)
                right = AreaNode(x, y + glyphHeight + 1, width, height - glyphHeight - 1)
            }

            return left?.insert(glyphWidth, glyphHeight)
        }
    }
}