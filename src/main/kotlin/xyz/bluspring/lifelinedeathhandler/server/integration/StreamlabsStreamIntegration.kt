package xyz.bluspring.lifelinedeathhandler.server.integration

import io.socket.client.IO
import io.socket.client.Socket
import net.minecraft.server.network.ServerPlayerEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType

class StreamlabsStreamIntegration(player: ServerPlayerEntity, apiKey: String, twitchUsername: String) : StreamIntegration(player, apiKey, twitchUsername) {
    override val integrationType = StreamIntegrationType.STREAMLABS
    override val logger: Logger = LoggerFactory.getLogger("${integrationType.integrationName}: $twitchUsername")

    override fun generateOptions(): IO.Options {
        return super.generateOptions().apply {
            query = "token=$apiKey"
        }
    }

    override fun onConnect(vararg args: Any) {

    }

    override fun onDisconnect(vararg args: Any) {

    }

    override fun onEvent(vararg args: Any) {

    }
}