package com.tftassistant.app.recommendation

import com.tftassistant.app.data.model.GameState
import com.tftassistant.app.data.model.MetaComposition
import com.tftassistant.app.data.model.MetaSnapshot
import com.tftassistant.app.data.model.Recommendation
import com.tftassistant.app.data.model.RecommendationBundle
import javax.inject.Inject

class RecommendationEngine @Inject constructor() {

    fun buildRecommendations(
        state: GameState,
        snapshot: MetaSnapshot
    ): RecommendationBundle {
        val targetComp = selectComposition(state, snapshot.compositions)
        val purchases = recommendPurchases(state, targetComp)
        val items = recommendItems(state, targetComp, snapshot)
        val economy = recommendEconomy(state)
        val positioning = recommendPositioning(state, targetComp)
        return RecommendationBundle(
            targetComposition = targetComp,
            purchaseSuggestions = purchases,
            itemSuggestions = items,
            economyTips = economy,
            positioningTips = positioning
        )
    }

    private fun selectComposition(
        state: GameState,
        compositions: List<MetaComposition>
    ): MetaComposition? {
        if (state.board.isEmpty()) return compositions.firstOrNull()
        return compositions.maxByOrNull { comp ->
            state.board.count { comp.coreChampions.contains(it) }
        }
    }

    private fun recommendPurchases(
        state: GameState,
        composition: MetaComposition?
    ): List<Recommendation> {
        if (composition == null) return emptyList()
        val missing = composition.coreChampions.filterNot { state.board.contains(it) || state.bench.contains(it) }
        return missing.map {
            Recommendation(
                title = "Mua $it",
                details = "Hoàn thiện đội hình ${composition.title}.",
                priority = Recommendation.Priority.HIGH
            )
        }
    }

    private fun recommendItems(
        state: GameState,
        composition: MetaComposition?,
        snapshot: MetaSnapshot
    ): List<Recommendation> {
        val recommended = composition?.carryItems?.flatMap { (champion, items) ->
            items.filterNot { state.items.contains(it) }.map { champion to it }
        } ?: emptyList()
        val recipes = snapshot.items.associateBy { it.id }
        return recommended.map { (champion, itemId) ->
            val item = recipes[itemId]
            Recommendation(
                title = "Ghép $itemId",
                details = "Ưu tiên cho $champion${item?.let { ": ${it.effect}" } ?: ""}",
                priority = Recommendation.Priority.MEDIUM
            )
        }
    }

    private fun recommendEconomy(state: GameState): List<Recommendation> {
        val tips = mutableListOf<Recommendation>()
        if (state.gold < 30) {
            tips += Recommendation(
                title = "Giữ lợi tức",
                details = "Cân nhắc lên 50 vàng trước khi roll mạnh.",
                priority = Recommendation.Priority.MEDIUM
            )
        }
        if (state.level < 7) {
            tips += Recommendation(
                title = "Kế hoạch lên cấp",
                details = "Lên cấp ${state.level + 1} ở vòng tiếp theo nếu đủ vàng.",
                priority = Recommendation.Priority.LOW
            )
        }
        return tips
    }

    private fun recommendPositioning(
        state: GameState,
        composition: MetaComposition?
    ): List<Recommendation> {
        if (composition == null) return emptyList()
        return listOf(
            Recommendation(
                title = "Sắp xếp chủ lực",
                details = "Đặt carry chính ở hàng sau, góc an toàn để tận dụng kỹ năng.",
                priority = Recommendation.Priority.MEDIUM
            ),
            Recommendation(
                title = "Chắn sát thương",
                details = "Đặt tanker tuyến đầu có trait ${composition.traits.firstOrNull() ?: "tanker"}.",
                priority = Recommendation.Priority.LOW
            )
        )
    }
}
