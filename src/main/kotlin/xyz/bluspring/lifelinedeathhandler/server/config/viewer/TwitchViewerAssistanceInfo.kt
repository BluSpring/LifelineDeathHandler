package xyz.bluspring.lifelinedeathhandler.server.config.viewer

import kotlinx.serialization.SerialName
import xyz.bluspring.lifelinedeathhandler.server.integration.TwitchSubscriptionTiers

@kotlinx.serialization.Serializable
sealed class TwitchViewerAssistanceInfo {
    abstract val type: ViewerAssistanceTypes
    abstract val tiers: List<TwitchSubscriptionTiers>

    @SerialName("LIFE_ADD_EVERY")
    @kotlinx.serialization.Serializable
    data class LifeAddEveryAssistanceData(
        override val tiers: List<TwitchSubscriptionTiers>,
        val per: Int,
        val add: Int,
        override val type: ViewerAssistanceTypes = ViewerAssistanceTypes.LIFE_ADD_EVERY
    ) : TwitchViewerAssistanceInfo()

    @SerialName("ITEM_GIVE")
    @kotlinx.serialization.Serializable
    data class ItemGiveAssistanceData(
        override val tiers: List<TwitchSubscriptionTiers>,
        val per: Int,
        val id: String,
        val count: Int,
        val nbt: String,
        override val type: ViewerAssistanceTypes = ViewerAssistanceTypes.ITEM_GIVE
    ) : TwitchViewerAssistanceInfo()
}