package xyz.bluspring.lifelinedeathhandler.server.config.viewer

@kotlinx.serialization.Serializable
data class TwitchViewerAssistanceInfo(
    val type: ViewerAssistanceTypes,
    val data: AssistanceData,
    val tiers: List<TwitchSubscriptionTiers>
)
