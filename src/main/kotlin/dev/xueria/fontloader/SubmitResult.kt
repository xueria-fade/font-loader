package dev.xueria.fontloader

sealed interface SubmitResult {

    data object Failure : SubmitResult

    data object Outbound : SubmitResult

    data class Succuss(val glyph: RasterizedGlyph) : SubmitResult

}