package xyz.bluspring.lifelinedeathhandler.server.config

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TwitchApiConfig(
    @SerialName("client-id")
    val clientId: String,

    @SerialName("client-secret")
    val clientSecret: String
)
