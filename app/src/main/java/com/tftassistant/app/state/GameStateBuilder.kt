package com.tftassistant.app.state

import com.tftassistant.app.capture.FrameCapture
import com.tftassistant.app.data.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class GameStateBuilder @Inject constructor() {

    private val _gameState = MutableStateFlow(
        GameState(
            gold = 0,
            level = 1,
            health = 100,
            shop = emptyList(),
            bench = emptyList(),
            board = emptyList(),
            items = emptyList(),
            augments = emptyList()
        )
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun handleFrame(frame: FrameCapture) {
        val current = _gameState.value
        val text = frame.ocrTextBlocks.joinToString("\n") { it.text }
        val gold = parseValue(text, GOLD_PATTERN) ?: current.gold
        val level = parseValue(text, LEVEL_PATTERN) ?: current.level
        val health = parseValue(text, HEALTH_PATTERN) ?: current.health
        val shop = parseList(text, SHOP_PATTERN, current.shop)
        val items = parseList(text, ITEM_PATTERN, current.items)
        val augments = parseList(text, AUGMENT_PATTERN, current.augments)
        _gameState.value = current.copy(
            gold = gold,
            level = level,
            health = health,
            shop = shop,
            items = items,
            augments = augments
        )
    }

    private fun parseValue(text: String, regex: Regex): Int? =
        regex.find(text)?.groupValues?.getOrNull(1)?.toIntOrNull()

    private fun parseList(text: String, regex: Regex, fallback: List<String>): List<String> =
        regex.find(text)?.groupValues?.getOrNull(1)?.split(',')
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.takeIf { it.isNotEmpty() }
            ?: fallback

    companion object {
        private val GOLD_PATTERN = Regex("Gold\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        private val LEVEL_PATTERN = Regex("Level\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        private val HEALTH_PATTERN = Regex("HP\\s*:?\\s*(\\d+)", RegexOption.IGNORE_CASE)
        private val SHOP_PATTERN = Regex("Shop\\s*:?\\s*([A-Za-z0-9 ,]+)", RegexOption.IGNORE_CASE)
        private val ITEM_PATTERN = Regex("Items\\s*:?\\s*([A-Za-z0-9 ,]+)", RegexOption.IGNORE_CASE)
        private val AUGMENT_PATTERN = Regex("Augments\\s*:?\\s*([A-Za-z0-9 ,]+)", RegexOption.IGNORE_CASE)
    }
}
