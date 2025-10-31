package com.tftassistant.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Champion(
    val id: String,
    val name: String,
    val cost: Int,
    val traits: List<String>,
    val recommendedItems: List<String>
)

@Serializable
data class Trait(
    val id: String,
    val name: String,
    val description: String,
    val thresholds: List<Int>
)

@Serializable
data class Item(
    val id: String,
    val name: String,
    val effect: String,
    val buildsFrom: List<String> = emptyList()
)

@Serializable
data class Augment(
    val id: String,
    val name: String,
    val description: String,
    val style: String
)

@Serializable
data class MetaComposition(
    val id: String,
    val title: String,
    val coreChampions: List<String>,
    val carryItems: Map<String, List<String>>,
    val traits: List<String>,
    val notes: String
)

@Serializable
data class MetaSnapshot(
    val version: String,
    val lastUpdated: String,
    val champions: List<Champion>,
    val traits: List<Trait>,
    val items: List<Item>,
    val augments: List<Augment>,
    @SerialName("compositions") val compositions: List<MetaComposition>
)

@Serializable
data class GameState(
    val gold: Int,
    val level: Int,
    val health: Int,
    val shop: List<String>,
    val bench: List<String>,
    val board: List<String>,
    val items: List<String>,
    val augments: List<String>
)

@Serializable
data class Recommendation(
    val title: String,
    val details: String,
    val priority: Priority
) {
    enum class Priority { HIGH, MEDIUM, LOW }
}

@Serializable
data class RecommendationBundle(
    val targetComposition: MetaComposition?,
    val purchaseSuggestions: List<Recommendation>,
    val itemSuggestions: List<Recommendation>,
    val economyTips: List<Recommendation>,
    val positioningTips: List<Recommendation>
)
