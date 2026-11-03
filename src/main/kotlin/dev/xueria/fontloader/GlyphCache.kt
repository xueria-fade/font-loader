package dev.xueria.fontloader

import java.awt.Font

class GlyphCache(private val font: Font) : AutoCloseable {

    private val glyphs: MutableMap<Int, RasterizedGlyph> = hashMapOf()
    private val pages: MutableList<GlyphPage> = mutableListOf()

    fun glyph(codepoint: Int): RasterizedGlyph? {
        if (glyphs.containsKey(codepoint)) {
            return glyphs[codepoint]
        }
        val glyph = submit(codepoint) ?: return null
        glyphs[codepoint] = glyph
        return glyph
    }

    private fun submit(codepoint: Int): RasterizedGlyph? =
        when (val result = lastPage().trySubmit(codepoint)) {
            SubmitResult.Failure    -> null
            SubmitResult.Outbound   -> {
                newPage()
                submit(codepoint)
            }

            is SubmitResult.Succuss -> result.glyph
        }

    private fun lastPage(): GlyphPage = pages.lastOrNull() ?: newPage()

    private fun newPage(): GlyphPage = GlyphPage(font).also { pages.add(it) }

    override fun close() {
        pages.clear()
        glyphs.clear()
    }

}