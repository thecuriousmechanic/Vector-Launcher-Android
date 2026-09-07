package com.vector.launch.core.ui

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

object DisplayControlUtils {

    /**
     * Builds a combined 4x5 ColorMatrix from Brightness, Contrast, and Saturation values.
     *
     * @param brightness Range: 0.0f (darkened) to 1.0f (normal / brightened)
     * @param contrast Range: 0.5f (low contrast) to 1.5f (high contrast)
     * @param saturation Range: 0.0f (grayscale) to 2.0f (vivid)
     */
    fun buildDisplayColorMatrix(
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): ColorMatrix {
        // 1. Saturation Matrix
        val satMatrix = ColorMatrix().apply {
            setToSaturation(saturation.coerceIn(0.0f, 2.0f))
        }

        // 2. Contrast Scale & Brightness Offset Matrix
        val c = contrast.coerceIn(0.5f, 1.5f)
        val b = brightness.coerceIn(0.0f, 1.0f)

        // Scale contrast around mid-tone 128, offset by brightness factor
        val scale = c
        val offset = (1.0f - c) * 128f + (b - 1.0f) * 255f

        val cbMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, offset,
                0f, scale, 0f, 0f, offset,
                0f, 0f, scale, 0f, offset,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // Combine contrast/brightness and saturation
        cbMatrix.timesAssign(satMatrix)
        return cbMatrix
    }

    fun buildDisplayColorFilter(
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): ColorFilter {
        return ColorFilter.colorMatrix(buildDisplayColorMatrix(brightness, contrast, saturation))
    }
}
