package xyz.bluspring.lifelinedeathhandler.server.integration.twitch

import java.util.UUID

data class StreamChannel(
    val player: UUID,
    val twitchChannel: String,
    var isLive: Boolean = false,
    var lastCheck: Long = 0L
)
