package xyz.bluspring.lifelinedeathhandler.server.config.viewer

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
sealed class TwitchViewerAssistanceInfo {
    @SerialName("LIFE_ADD_EVERY")
    @kotlinx.serialization.Serializable
    data class LifeAddEveryAssistanceData(
        val tiers: List<TwitchSubscriptionTiers>,
        val per: Int,
        val add: Int
    )

    @SerialName("ITEM_GIVE")
    @kotlinx.serialization.Serializable
    data class ItemGiveAssistanceData(
        val tiers: List<TwitchSubscriptionTiers>,
        val per: Int,
        val id: String,
        val count: Int,
        val nbt: String
    )
}