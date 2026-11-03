package dev.xueria.fontloader

object ColorUtils {
    fun alpha(color: Int): Float = ((color ushr 24) and 0xFF) / 255f

    fun red(color: Int): Float = ((color shr 16) and 0xFF) / 255f

    fun green(color: Int): Float = ((color shr 8) and 0xFF) / 255f

    fun blue(color: Int): Float = (color and 0xFF) / 255f
}