package dev.xueria.fontloader

import java.awt.Font

class FontFace @JvmOverloads constructor(
    val name: String,
    val source: Source = Source.Unspecified
) {

    @Volatile
    private var styles: Map<FontStyle, LoadedStyle> = emptyMap()

    fun style(style: FontStyle): Font? = styles[style]?.font

    fun metrics(style: FontStyle): FaceMetrics? = styles[style]?.metrics

    fun requireMetrics() =
        styles[FontStyle.Regular]?.metrics ?: error("")

    fun prepare(baseFont: Font) {
        styles = FontStyle.entries.associateWith { style ->
            val font = baseFont.deriveFont(style.awtStyle, baseFont.size * Global.Oversample)
            LoadedStyle(font = font, metrics = font.measure())
        }
    }

    private data class LoadedStyle(
        val font: Font,
        val metrics: FaceMetrics
    )

}