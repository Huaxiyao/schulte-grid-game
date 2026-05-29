package com.schulte.grid.model

/**
 * 舒尔特方格支持的网格尺寸
 */
enum class GridSize(val size: Int, val label: String) {
    SIZE_3(3, "3×3"),
    SIZE_4(4, "4×4"),
    SIZE_5(5, "5×5"),
    SIZE_6(6, "6×6"),
    SIZE_7(7, "7×7");

    val totalNumbers: Int get() = size * size

    companion object {
        val DEFAULT = SIZE_5

        fun fromSize(size: Int): GridSize =
            entries.firstOrNull { it.size == size } ?: DEFAULT
    }
}
