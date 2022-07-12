package xyz.bluspring.lifelinedeathhandler.server.config

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class LifelineServerConfig(
    @SerialName("default-lives")
    var defaultLives: Int = 50,

    @SerialName("max-group-size")
    var maxGroupSize: Int = 4,

    @SerialName("viewer-assistance")
    val viewerAssistance: ViewerAssistanceConfig
)
