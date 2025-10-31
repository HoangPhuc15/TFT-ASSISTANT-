package com.tftassistant.app.domain

import com.tftassistant.app.data.MetaRepository
import com.tftassistant.app.data.model.GameState
import com.tftassistant.app.data.model.MetaSnapshot
import com.tftassistant.app.data.model.RecommendationBundle
import com.tftassistant.app.recommendation.RecommendationEngine
import com.tftassistant.app.state.GameStateBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TftAssistantController @Inject constructor(
    private val metaRepository: MetaRepository,
    private val gameStateBuilder: GameStateBuilder,
    private val recommendationEngine: RecommendationEngine
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        scope.launch {
            val snapshot = metaRepository.bootstrapSnapshot()
            _uiState.update { it.copy(metaSnapshot = snapshot) }
            gameStateBuilder.gameState.collectLatest { state ->
                updateRecommendations(state)
            }
        }
    }

    fun refreshMeta() {
        scope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            runCatching { metaRepository.refreshSnapshot() }
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(metaSnapshot = snapshot, isSyncing = false) }
                    updateRecommendations(gameStateBuilder.gameState.value)
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isSyncing = false, error = throwable.message) }
                }
        }
    }

    private fun updateRecommendations(state: GameState) {
        val snapshot = _uiState.value.metaSnapshot ?: return
        val bundle = recommendationEngine.buildRecommendations(state, snapshot)
        _uiState.update { current ->
            current.copy(
                gameState = state,
                recommendationBundle = bundle
            )
        }
    }
}

data class AssistantUiState(
    val gameState: GameState = GameState(
        gold = 0,
        level = 1,
        health = 100,
        shop = emptyList(),
        bench = emptyList(),
        board = emptyList(),
        items = emptyList(),
        augments = emptyList()
    ),
    val recommendationBundle: RecommendationBundle? = null,
    val metaSnapshot: MetaSnapshot? = null,
    val isSyncing: Boolean = false,
    val error: String? = null
)
