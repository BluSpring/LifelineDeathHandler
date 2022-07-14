package xyz.bluspring.lifelinedeathhandler.server.integration

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class StreamElementsStreamIntegration(player: ServerPlayerEntity, apiKey: String, twitchUsername: String) : StreamIntegration(player, apiKey, twitchUsername) {
    override fun start() {

    }

    override fun stop() {

    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(StreamElementsStreamIntegration::class.java)
    }
}