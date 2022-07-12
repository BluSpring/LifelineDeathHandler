package xyz.bluspring.lifelinedeathhandler.server.config

import kotlinx.serialization.SerialName
import xyz.bluspring.lifelinedeathhandler.server.config.viewer.*

@kotlinx.serialization.Serializable
data class ViewerAssistanceConfig(
    @SerialName("twitch-subscription-gift")
    val twitchSubscriptionGift: TwitchViewerAssistance,

    @SerialName("twitch-cheer")
    val twitchCheer: ViewerAssistance,

    @SerialName("twitch-subscription")
    val twitchSubscription: TwitchViewerAssistance,

    @SerialName("donation")
    val donation: ViewerAssistance,

    @SerialName("twitch-follow")
    val twitchFollow: ViewerAssistance,

    @SerialName("twitch-point-redemption")
    val twitchPointRedemption: ViewerAssistance,

    @SerialName("twitch-raid")
    val twitchRaid: ViewerAssistance,

    @SerialName("twitch-host")
    val twitchHost: ViewerAssistance
)
