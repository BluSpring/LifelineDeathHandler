package xyz.bluspring.lifelinedeathhandler.server.config.viewer

@kotlinx.serialization.Serializable
data class ViewerAssistanceInfo(
    val type: ViewerAssistanceTypes,
    val data: AssistanceData
)