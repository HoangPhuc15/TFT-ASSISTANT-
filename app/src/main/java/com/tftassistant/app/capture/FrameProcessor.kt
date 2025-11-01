package com.tftassistant.app.capture

import android.content.Context
import android.graphics.PixelFormat
import android.media.ImageReader
import android.media.projection.MediaProjection
import com.tftassistant.app.ocr.IconClassifier
import com.tftassistant.app.ocr.OcrProcessor
import com.tftassistant.app.state.GameStateBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ocrProcessor: OcrProcessor,
    private val iconClassifier: IconClassifier,
    private val gameStateBuilder: GameStateBuilder
) {
    private var captureJob: Job? = null

    fun start(scope: CoroutineScope, projection: MediaProjection) {
        if (captureJob?.isActive == true) return
        captureJob = scope.launch {
            captureFrames(projection).collectLatest { image ->
                val textBlocks = ocrProcessor.process(image)
                val icons = iconClassifier.detect(image)
                val frameCapture = FrameCapture(
                    timestamp = System.currentTimeMillis(),
                    frameId = image.timestamp,
                    ocrTextBlocks = textBlocks,
                    detectedIcons = icons
                )
                gameStateBuilder.handleFrame(frameCapture)
                image.close()
            }
        }
    }

    fun stop() {
        captureJob?.cancel()
        captureJob = null
    }

    private fun captureFrames(projection: MediaProjection): Flow<ImageReader.Image> = callbackFlow {
        val metrics = context.resources.displayMetrics
        val density = metrics.densityDpi
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 3)
        val virtualDisplay = projection.createVirtualDisplay(
            "tft-assistant-capture",
            width,
            height,
            density,
            0,
            reader.surface,
            null,
            null
        )
        val handlerThread = android.os.HandlerThread("tft-capture").apply { start() }
        val handler = android.os.Handler(handlerThread.looper)
        reader.setOnImageAvailableListener({ imageReader ->
            imageReader.acquireLatestImage()?.let { trySend(it) }
        }, handler)
        awaitClose {
            reader.close()
            virtualDisplay.release()
            handlerThread.quitSafely()
        }
    }
}
