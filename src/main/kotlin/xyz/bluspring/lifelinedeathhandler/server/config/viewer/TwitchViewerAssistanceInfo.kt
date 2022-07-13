package xyz.bluspring.lifelinedeathhandler.server.config.viewer

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
sealed class TwitchViewerAssistanceInfo {
    @kotlinx.serialization.Serializable
    abstract val tiers: List<TwitchSubscriptionTiers>

    @SerialName("LIFE_ADD_EVERY")
    @kotlinx.serialization.Serializable
    data class LifeAddEveryAssistanceData(
        val per: Int,
        val add: Int
    )

    @SerialName("ITEM_GIVE")
    @kotlinx.serialization.Serializable
    data class ItemGiveAssistanceData(
        val per: Int,
        val id: String,
        val count: Int,
        val nbt: String
    )
}