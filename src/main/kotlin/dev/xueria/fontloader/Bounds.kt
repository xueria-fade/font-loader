package dev.xueria.fontloader

data class Bounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {

    companion object {
        val Empty: Bounds = Bounds(0f, 0f, 0f, 0f)
    }

}