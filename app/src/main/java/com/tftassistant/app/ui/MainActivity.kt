package com.tftassistant.app.ui

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.tftassistant.app.R
import com.tftassistant.app.capture.MediaProjectionService
import com.tftassistant.app.domain.AssistantUiState
import com.tftassistant.app.overlay.TftOverlayService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val mediaProjectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val intent = Intent(this, MediaProjectionService::class.java).apply {
                    putExtra(MediaProjectionService.EXTRA_RESULT_CODE, result.resultCode)
                    putExtra(MediaProjectionService.EXTRA_RESULT_DATA, result.data)
                }
                ContextCompat.startForegroundService(this, intent)
                startService(Intent(this, TftOverlayService::class.java))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val uiState by viewModel.uiState.collectAsState()
                MainScreen(
                    state = uiState,
                    onRequestOverlay = { ensureOverlayPermissionAndCapture() },
                    onRefreshMeta = { viewModel.refreshMeta() }
                )
            }
        }
    }

    private fun ensureOverlayPermissionAndCapture() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }
        val manager = getSystemService(MediaProjectionManager::class.java)
        mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
    }
}

@Composable
private fun MainScreen(
    state: AssistantUiState,
    onRequestOverlay: () -> Unit,
    onRefreshMeta: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = LocalContext.current.getString(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Vàng: ${state.gameState.gold} | Cấp: ${state.gameState.level} | HP: ${state.gameState.health}",
                style = MaterialTheme.typography.bodyLarge
            )
            state.recommendationBundle?.let { bundle ->
                RecommendationBlock("Đội hình", bundle.targetComposition?.title ?: "Chưa xác định")
                RecommendationBlock(
                    title = "Mua thêm",
                    content = bundle.purchaseSuggestions.joinToString("\n") { it.title }
                )
                RecommendationBlock(
                    title = "Ghép đồ",
                    content = bundle.itemSuggestions.joinToString("\n") { it.details }
                )
                RecommendationBlock(
                    title = "Kinh tế",
                    content = bundle.economyTips.joinToString("\n") { it.details }
                )
                RecommendationBlock(
                    title = "Vị trí",
                    content = bundle.positioningTips.joinToString("\n") { it.details }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onRequestOverlay) {
                    Text(text = LocalContext.current.getString(R.string.start_overlay))
                }
                TextButton(onClick = onRefreshMeta, enabled = !state.isSyncing) {
                    Text(text = LocalContext.current.getString(R.string.sync_meta))
                }
            }
            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun RecommendationBlock(title: String, content: String) {
    if (content.isBlank()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(content, style = MaterialTheme.typography.bodyMedium)
    }
}
