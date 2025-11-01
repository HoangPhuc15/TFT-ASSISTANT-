package com.tftassistant.app.capture

data class FrameCapture(
    val timestamp: Long,
    val frameId: Long,
    val ocrTextBlocks: List<OcrTextBlock>,
    val detectedIcons: List<DetectedIcon>
)

data class OcrTextBlock(
    val text: String,
    val boundingBox: Rect
)

data class Rect(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

data class DetectedIcon(
    val label: String,
    val confidence: Float,
    val boundingBox: Rect
)
