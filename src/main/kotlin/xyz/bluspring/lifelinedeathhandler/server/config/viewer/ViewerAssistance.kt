package xyz.bluspring.lifelinedeathhandler.server.config.viewer

@kotlinx.serialization.Serializable
data class ViewerAssistance(
    val enabled: Boolean,
    val types: List<ViewerAssistanceInfo>
)
