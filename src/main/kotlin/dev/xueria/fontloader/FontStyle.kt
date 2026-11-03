package dev.xueria.fontloader

import dev.xueria.fontloader.FontStyle.Bold
import dev.xueria.fontloader.FontStyle.BoldItalic
import dev.xueria.fontloader.FontStyle.Italic
import dev.xueria.fontloader.FontStyle.Regular
import java.awt.Font

enum class FontStyle {
    Regular,
    Bold,
    Italic,
    BoldItalic;

    companion object {
        fun of(bold: Boolean, italic: Boolean): FontStyle {
            return when {
                bold && italic -> BoldItalic
                bold           -> Bold
                italic         -> Italic
                else           -> Regular
            }
        }
    }
}

internal val FontStyle.awtStyle: Int
    get() = when (this) {
        Regular    -> Font.PLAIN
        Bold       -> Font.BOLD
        Italic     -> Font.ITALIC
        BoldItalic -> Font.BOLD or Font.ITALIC
    }
