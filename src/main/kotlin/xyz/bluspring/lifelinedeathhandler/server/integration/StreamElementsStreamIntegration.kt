package xyz.bluspring.lifelinedeathhandler.server.integration

import net.minecraft.server.network.ServerPlayerEntity
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.lifelinedeathhandler.LifelineDeathHandler
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType
import java.util.*

class StreamElementsStreamIntegration(player: ServerPlayerEntity, apiKey: String, twitchUsername: String) : StreamIntegration(player, apiKey, twitchUsername) {
    override val integrationType = StreamIntegrationType.STREAMELEMENTS
    override val logger: Logger = LoggerFactory.getLogger("${integrationType.integrationName}: $twitchUsername")

    override fun onConnect(vararg args: Any) {
        logger.info("Connected to StreamElements' Socket API. Attempting authentication.")

        try {
            val auth = JSONObject().apply {
                put("method", "jwt")
                put("token", apiKey)
            }

            socket.emit("authenticate", auth)
        } catch (_: Exception) {}

        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (!authorized) {
                    logger.warn("Not authorized to connect to StreamElements' Socket API!")

                    stop()
                    sendInvalidIntegrationError()
                }
            }
        }, 5000)

        socket.on("authenticated") {
            logger.info("Successfully authenticated with the StreamElements Socket API!")
            authorized = true

            timer.cancel()
        }
    }

    override fun onDisconnect(vararg args: Any) {
        logger.info("Disconnected from StreamElements' Socket API.")
    }

    override fun onEvent(vararg args: Any) {
        val event = LifelineDeathHandler.convertJsonToGson(args[0] as JSONObject)

        if (!event.has("data") || !event.get("data").isJsonObject) {
            logger.warn("Discarding unexpected StreamElements packet: $event")
            return
        }

        val type = event.get("type").asString
        val provider = event.get("provider").asString

        val data = event.getAsJsonObject("data")

        when (type) {
            "subscriber" -> {
                StreamIntegrationManager.handleTwitchSubscription(
                    player,
                    TwitchSubscriptionTiers.values().first { it.id == data.get("tier").asString }
                )
            }

            else -> {}
        }
    }
}