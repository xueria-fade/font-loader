package dev.xueria.fontloader

data class RasterizedGlyph(
    val codepoint: Int,
    val bounds: Bounds,
    val uv: Bounds,
    val advance: Float,
    val texture: Int
)