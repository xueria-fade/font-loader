package dev.xueria.fontloader

import java.nio.file.Path

sealed interface Source {

    data object Unspecified : Source

    data class System(val name: String) : Source

    data class Internal(val filename: String) : Source

    data class External(val file: Path) : Source

}