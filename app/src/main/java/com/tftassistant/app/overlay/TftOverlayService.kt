package com.tftassistant.app.overlay

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.tftassistant.app.domain.AssistantUiState
import com.tftassistant.app.domain.TftAssistantController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TftOverlayService : LifecycleService() {

    @Inject
    lateinit var controller: TftAssistantController

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlay()
        lifecycleScope.launch {
            controller.uiState.collectLatest { state ->
                overlayView?.setContent {
                    OverlayRoot(state)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        overlayView?.let { windowManager.removeViewImmediate(it) }
        overlayView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createOverlay() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { OverlayRoot(AssistantUiState()) }
        }
        windowManager.addView(composeView, params)
        overlayView = composeView
    }
}

@Composable
private fun OverlayRoot(state: AssistantUiState) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCC111111))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Vàng: ${state.gameState.gold} | Cấp: ${state.gameState.level} | HP: ${state.gameState.health}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            state.recommendationBundle?.let { bundle ->
                RecommendationSection("Đội hình gợi ý", bundle.targetComposition?.title ?: "Chưa xác định")
                RecommendationSection(
                    "Mua thêm",
                    bundle.purchaseSuggestions.joinToString("\n") { it.title }
                )
                RecommendationSection(
                    "Ghép đồ",
                    bundle.itemSuggestions.joinToString("\n") { it.details }
                )
                RecommendationSection(
                    "Kinh tế",
                    bundle.economyTips.joinToString("\n") { it.details }
                )
                RecommendationSection(
                    "Vị trí",
                    bundle.positioningTips.joinToString("\n") { it.details }
                )
            }
            if (state.isSyncing) {
                Text(
                    text = "Đang đồng bộ meta…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF80CBC4)
                )
            }
            state.error?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF8A80)
                )
            }
        }
    }
}

@Composable
private fun RecommendationSection(title: String, body: String) {
    if (body.isBlank()) return
    Card(colors = CardDefaults.cardColors(containerColor = Color(0x661E1E1E))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color(0xFFFFB74D))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
    }
}
