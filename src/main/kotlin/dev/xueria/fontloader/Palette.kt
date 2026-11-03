package dev.xueria.fontloader

import net.minecraft.EnumChatFormatting

data class Palette(
    val color: Int,
    val shadow: Boolean = false
)
fun Palette.colorOf(formatting: EnumChatFormatting): Int =
    if (shadow) formatting.rgb_shadow else formatting.rgb