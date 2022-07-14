package xyz.bluspring.lifelinedeathhandler.server.integration

import io.socket.client.IO
import io.socket.client.Socket
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.slf4j.Logger
import xyz.bluspring.lifelinedeathhandler.LifelineDeathHandler
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType

abstract class StreamIntegration(val player: ServerPlayerEntity, val apiKey: String, val twitchUsername: String) {
    abstract val integrationType: StreamIntegrationType
    protected lateinit var socket: Socket
    abstract val logger: Logger

    var authorized = false

    open fun start() {
        logger.info("Starting ${integrationType.integrationName} Stream Integration for player ${player.name}.")
        socket = createSocket()
    }

    open fun stop() {
        logger.info("Stopping ${integrationType.integrationName} Stream Integration for player ${player.name}.")
        socket.close()
    }

    protected open fun generateOptions(): IO.Options {
        return IO.Options().apply {
            forceNew = true
            reconnection = true
            transports = arrayOf("websocket")
        }
    }

    protected fun createSocket(): Socket {
        try {
            val options = generateOptions()
            val socket = IO.socket(integrationType.url, options)

            socket.on(Socket.EVENT_CONNECT) {
                onConnect(*it)
            }

            socket.on(Socket.EVENT_DISCONNECT) {
                onDisconnect(*it)
            }

            socket.on("event") {
                onEvent(*it)
            }

            if (LifelineDeathHandler.TESTING)
                socket.on("event:test") {
                    onEvent(*it)
                }

            return socket
        } catch (e: Exception) {
            throw e
        }
    }

    protected abstract fun onConnect(vararg args: Any)
    protected abstract fun onDisconnect(vararg args: Any)
    protected abstract fun onEvent(vararg args: Any)

    protected fun sendInvalidIntegrationError() {
        ServerPlayNetworking.send(player, Identifier("lifelinesmp", "integration_invalid"), PacketByteBufs.empty())
    }
}