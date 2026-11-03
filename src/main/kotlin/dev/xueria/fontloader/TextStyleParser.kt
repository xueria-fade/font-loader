package dev.xueria.fontloader

import net.minecraft.EnumChatFormatting
import kotlin.random.Random

object TextStyleParser {

    private const val SectionSign: Char = '\u00a7'
    private val ObfuscationSamples: List<Int> =
        "abcdefghijklmnopqrstuvwxyz0123456789".map(Char::code)

    private val random: Random = Random

    fun parse(text: String, palette: Palette): List<StyledCodepoint> {
        val result: MutableList<StyledCodepoint> = ArrayList(text.length)
        var style = TextStyle(palette.color)
        var index = 0
        while (index < text.length) {
            val character: Char = text[index]
            if (character == SectionSign && index + 1 < text.length) {
                style = applyCode(style, text[index + 1], palette)
                index += 2
                continue
            }
            result += StyledCodepoint(character.code, style)
            index++
        }
        return result
    }

    private fun obfuscate(item: StyledCodepoint): Int =
        ObfuscationSamples[random.nextInt(ObfuscationSamples.size)]

    private fun applyCode(style: TextStyle, code: Char, palette: Palette): TextStyle {
        val formatting: EnumChatFormatting =
            EnumChatFormatting.getByChar(code.lowercaseChar()) ?: return style
        if (formatting == EnumChatFormatting.RESET) return TextStyle(palette.color)
        if (formatting.isColor) return TextStyle(palette.colorOf(formatting))
        return applyFlag(style, formatting)
    }

    private fun applyFlag(style: TextStyle, formatting: EnumChatFormatting): TextStyle =
        when (formatting) {
            EnumChatFormatting.BOLD          -> style.copy(bold = true)
            EnumChatFormatting.ITALIC        -> style.copy(italic = true)
            EnumChatFormatting.UNDERLINE     -> style.copy(underline = true)
            EnumChatFormatting.STRIKETHROUGH -> style.copy(strikethrough = true)
            EnumChatFormatting.OBFUSCATED    -> style.copy(obfuscated = true)
            else                             -> style
        }

}