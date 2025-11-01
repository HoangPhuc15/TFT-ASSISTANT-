package com.tftassistant.app.ocr

import android.media.Image
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tftassistant.app.capture.OcrTextBlock
import com.tftassistant.app.capture.Rect
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrProcessor @Inject constructor() {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun process(image: Image): List<OcrTextBlock> {
        val inputImage = InputImage.fromMediaImage(image, 0)
        val visionText = recognizer.process(inputImage).await()
        return visionText.textBlocks.map { block ->
            OcrTextBlock(
                text = block.text.trim(),
                boundingBox = block.boundingBox?.let {
                    Rect(it.left, it.top, it.right, it.bottom)
                } ?: Rect(0, 0, 0, 0)
            )
        }
    }
}
