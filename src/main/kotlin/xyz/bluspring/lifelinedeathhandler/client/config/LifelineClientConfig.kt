package xyz.bluspring.lifelinedeathhandler.client.config

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class LifelineClientConfig(
    var type: StreamIntegrationType,
    @SerialName("api-key")
    var apiKey: String = ""
)
