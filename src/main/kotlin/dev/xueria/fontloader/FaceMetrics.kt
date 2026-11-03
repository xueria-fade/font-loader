package dev.xueria.fontloader

import java.awt.Font
import java.awt.font.FontRenderContext

data class FaceMetrics(
    val ascent: Float,
    val descent: Float,
    val capHeight: Float,
    val underline: Decoration,
    val strikethrough: Decoration
) {
    data class Decoration(
        val offset: Float,
        val thickness: Float
    )
}

private const val MetricsSample: String = "Ag"
private const val CapSample: String = "H"

fun Font.measure(): FaceMetrics {
    val context: FontRenderContext = renderContext
    val line = getLineMetrics(MetricsSample, context)
    val capBounds = createGlyphVector(context, CapSample)
        .getGlyphPixelBounds(0, context, 0f, 0f)
    val capHeight =
        if (capBounds.height > 0) -capBounds.y.toFloat() else line.ascent
    return FaceMetrics(
        ascent = line.ascent,
        descent = line.descent,
        capHeight = capHeight,
        underline = FaceMetrics.Decoration(line.underlineOffset, line.underlineThickness),
        strikethrough = FaceMetrics.Decoration(
            line.strikethroughOffset,
            line.strikethroughThickness
        )
    )
}