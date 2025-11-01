package com.tftassistant.app.ocr

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import com.tftassistant.app.capture.DetectedIcon
import com.tftassistant.app.capture.Rect
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconClassifier @Inject constructor() {

    suspend fun detect(image: Image): List<DetectedIcon> {
        val bitmap = image.toBitmap() ?: return emptyList()
        val width = bitmap.width
        val height = bitmap.height
        val centerX = width / 2
        val centerY = height / 2
        return listOf(
            DetectedIcon(
                label = "board",
                confidence = 0.5f,
                boundingBox = Rect(centerX - 200, centerY - 200, centerX + 200, centerY + 200)
            )
        )
    }
}

private fun Image.toBitmap(): Bitmap? {
    if (format != ImageFormat.YUV_420_888) return null
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
        eraseColor(android.graphics.Color.TRANSPARENT)
    }
}
