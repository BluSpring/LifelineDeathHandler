package xyz.bluspring.lifelinedeathhandler.client.config

import kotlinx.serialization.SerialName
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType

@kotlinx.serialization.Serializable
data class LifelineClientConfig(
    var type: StreamIntegrationType,
    @SerialName("api-key")
    var apiKey: String = "",
    @SerialName("twitch-username")
    var twitchUsername: String = ""
)
