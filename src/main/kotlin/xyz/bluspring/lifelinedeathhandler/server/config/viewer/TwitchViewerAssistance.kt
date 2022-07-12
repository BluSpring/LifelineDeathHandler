package xyz.bluspring.lifelinedeathhandler.server.config.viewer

@kotlinx.serialization.Serializable
data class TwitchViewerAssistance(
    val enabled: Boolean,
    val types: List<TwitchViewerAssistanceInfo>
)
