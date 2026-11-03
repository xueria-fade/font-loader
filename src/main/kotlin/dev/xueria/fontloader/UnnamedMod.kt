package dev.xueria.fontloader

import net.fabricmc.api.ModInitializer
import java.awt.Font

object UnnamedMod : ModInitializer {

    lateinit var renderer: AwtFontRenderer

    override fun onInitialize() {
    }

    fun init() {
        val face = FontFace("Microsoft YaHei")
        face.prepare(Font("Microsoft YaHei", Font.PLAIN, 9))
        renderer = AwtFontRenderer(face)
    }

}