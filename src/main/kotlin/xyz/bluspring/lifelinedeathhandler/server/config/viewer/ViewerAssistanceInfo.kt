package xyz.bluspring.lifelinedeathhandler.server.config.viewer

import kotlinx.serialization.SerialName
import xyz.bluspring.lifelinedeathhandler.server.integration.TwitchSubscriptionTiers

@kotlinx.serialization.Serializable
sealed class ViewerAssistanceInfo {
    abstract val type: ViewerAssistanceTypes
    abstract val per: Int

    @SerialName("LIFE_ADD_EVERY")
    @kotlinx.serialization.Serializable
    data class LifeAddEveryAssistanceData(
        val tiers: List<TwitchSubscriptionTiers>,
        override val per: Int,
        val add: Int,
        override val type: ViewerAssistanceTypes = ViewerAssistanceTypes.LIFE_ADD_EVERY
    ) : ViewerAssistanceInfo()

    @SerialName("ITEM_GIVE")
    @kotlinx.serialization.Serializable
    data class ItemGiveAssistanceData(
        val tiers: List<TwitchSubscriptionTiers>,
        override val per: Int,
        val id: String,
        val count: Int,
        val nbt: String,
        override val type: ViewerAssistanceTypes = ViewerAssistanceTypes.LIFE_ADD_EVERY
    ) : ViewerAssistanceInfo()
}