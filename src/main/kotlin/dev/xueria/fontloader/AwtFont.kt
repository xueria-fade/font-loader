package dev.xueria.fontloader

import java.awt.Font
import java.awt.font.FontRenderContext

internal val Font.renderContext: FontRenderContext
    get() = FontRenderContext(transform, true, true)