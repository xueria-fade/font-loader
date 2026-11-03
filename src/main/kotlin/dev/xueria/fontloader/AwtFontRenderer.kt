package dev.xueria.fontloader

import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.roundToInt

class AwtFontRenderer(
    private val face: FontFace
) {

    private val metrics: FaceMetrics = face.requireMetrics()
    private val caches: EnumMap<FontStyle, GlyphCache> = EnumMap(FontStyle::class.java)

    fun widthOf(text: String): Int {
        val scale: Float = scale()
        val palette = Palette(color = 0)
        var advance = 0f
        for (item: StyledCodepoint in TextStyleParser.parse(text, palette)) {
            val style: FontStyle = item.style.fontStyle
            val cache: GlyphCache = caches.getOrPut(style) {
                GlyphCache(face.style(style) ?: error("Font style $style is not prepared"))
            }
            val glyph: RasterizedGlyph = cache.glyph(item.codepoint) ?: continue
            advance += glyph.advance
        }
        return (advance * scale).roundToInt()
    }

    fun drawText(text: String, x: Int, y: Int, color: Int, shadow: Boolean): Int {
        val scale: Float = scale()
        val baseColor: Int = if ((color and 0xFC000000.toInt()) == 0) {
            color or (0xFF000000.toInt())
        } else {
            color
        }
        val alpha: Int = baseColor and 0xFF000000.toInt()
        // Vanilla renders shadows with a quarter of the base color.
        val shadowColor: Int =
            ((baseColor and 0xFCFCFC) shr 2) or (baseColor and 0xFF000000.toInt())
        val passes: Int = if (shadow) 2 else 1
        val previousTexture: Int = OpenGL.glGetInteger(OpenGL.GL_TEXTURE_BINDING_2D)
        val textureEnabled: Boolean = GL11.glIsEnabled(GL11.GL_TEXTURE_2D)
        val blendingEnabled: Boolean = GL11.glIsEnabled(GL11.GL_BLEND)
        val alphaTestEnabled: Boolean = GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        val blendSource: Int = GL11.glGetInteger(GL11.GL_BLEND_SRC)
        val blendDestination: Int = GL11.glGetInteger(GL11.GL_BLEND_DST)
        var boundTexture: Int = previousTexture
        var endX: Int = Int.MIN_VALUE
        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glDisable(GL11.GL_ALPHA_TEST)
            for (pass: Int in 0 until passes) {
                val shadowPass: Boolean = shadow && pass == 0
                val offset: Float = if (shadowPass) 1f else 0f
                val palette =
                    Palette(if (shadowPass) shadowColor else baseColor, shadowPass)
                val baseline: Float =
                    (y + offset) + metrics.capHeight * scale
                var cursor: Float = (x + offset)
                for (item: StyledCodepoint in TextStyleParser.parse(text, palette)) {
                    val style: FontStyle = item.style.fontStyle
                    val cache: GlyphCache = caches.getOrPut(style) {
                        GlyphCache(face.style(style) ?: error("Font style $style is not prepared"))
                    }
                    val glyph: RasterizedGlyph = cache.glyph(item.codepoint) ?: continue
                    val glyphColor: Int = (item.style.color and 0xFFFFFF) or alpha
                    GL11.glColor4f(
                        ColorUtils.red(glyphColor),
                        ColorUtils.green(glyphColor),
                        ColorUtils.blue(glyphColor),
                        ColorUtils.alpha(glyphColor)
                    )
                    val glyphWidth: Float = glyph.advance * scale
                    if (glyph.bounds.right > glyph.bounds.left &&
                        glyph.bounds.bottom > glyph.bounds.top
                    ) {
                        if (glyph.texture != boundTexture) {
                            OpenGL.glBindTexture(glyph.texture)
                            boundTexture = glyph.texture
                        }
                        val left: Float = cursor + glyph.bounds.left * scale
                        val top: Float = baseline + glyph.bounds.top * scale
                        val right: Float = cursor + glyph.bounds.right * scale
                        val bottom: Float = baseline + glyph.bounds.bottom * scale
                        GL11.glBegin(GL11.GL_QUADS)
                        GL11.glTexCoord2f(glyph.uv.left, glyph.uv.top)
                        GL11.glVertex2f(left, top)
                        GL11.glTexCoord2f(glyph.uv.left, glyph.uv.bottom)
                        GL11.glVertex2f(left, bottom)
                        GL11.glTexCoord2f(glyph.uv.right, glyph.uv.bottom)
                        GL11.glVertex2f(right, bottom)
                        GL11.glTexCoord2f(glyph.uv.right, glyph.uv.top)
                        GL11.glVertex2f(right, top)
                        GL11.glEnd()
                    }
                    if (item.style.underline || item.style.strikethrough) {
                        GL11.glDisable(GL11.GL_TEXTURE_2D)
                        GL11.glBegin(GL11.GL_QUADS)
                        if (item.style.strikethrough) {
                            val strikeTop: Float =
                                baseline + metrics.strikethrough.offset * scale
                            val strikeBottom: Float = strikeTop + maxOf(
                                metrics.strikethrough.thickness * scale,
                                1f
                            )
                            GL11.glVertex2f(cursor, strikeTop)
                            GL11.glVertex2f(cursor, strikeBottom)
                            GL11.glVertex2f(cursor + glyphWidth, strikeBottom)
                            GL11.glVertex2f(cursor + glyphWidth, strikeTop)
                        }
                        if (item.style.underline) {
                            val underlineTop: Float =
                                baseline + metrics.underline.offset * scale
                            val underlineBottom: Float = underlineTop + maxOf(
                                metrics.underline.thickness * scale,
                                1f
                            )
                            GL11.glVertex2f(cursor, underlineTop)
                            GL11.glVertex2f(cursor, underlineBottom)
                            GL11.glVertex2f(cursor + glyphWidth, underlineBottom)
                            GL11.glVertex2f(cursor + glyphWidth, underlineTop)
                        }
                        GL11.glEnd()
                        GL11.glEnable(GL11.GL_TEXTURE_2D)
                    }
                    cursor += glyphWidth
                }
                endX = maxOf(endX, cursor.roundToInt())
            }
        } finally {
            GL11.glBlendFunc(blendSource, blendDestination)
            if (!blendingEnabled) {
                GL11.glDisable(GL11.GL_BLEND)
            }
            if (alphaTestEnabled) {
                GL11.glEnable(GL11.GL_ALPHA_TEST)
            }
            OpenGL.glBindTexture(previousTexture)
            if (!textureEnabled) {
                GL11.glDisable(GL11.GL_TEXTURE_2D)
            }
        }
        return endX
    }

    private fun scale() = 1 / Global.Oversample

}
