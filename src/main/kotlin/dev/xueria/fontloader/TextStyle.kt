package dev.xueria.fontloader

data class TextStyle(
    val color: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val obfuscated: Boolean = false
)

val TextStyle.fontStyle: FontStyle
    get() = FontStyle.of(bold, italic)