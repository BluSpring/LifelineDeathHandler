package xyz.bluspring.lifelinedeathhandler.server.integration

import io.socket.client.IO
import io.socket.client.Socket
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.slf4j.Logger
import xyz.bluspring.lifelinedeathhandler.LifelineDeathHandler
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType
import xyz.bluspring.lifelinedeathhandler.common.WarningTypes

abstract class StreamIntegration(val player: ServerPlayerEntity, val apiKey: String, val twitchUsername: String) {
    abstract val integrationType: StreamIntegrationType
    protected lateinit var socket: Socket
    abstract val logger: Logger

    var authorized = false

    open fun start() {
        logger.info("Starting ${integrationType.integrationName} Stream Integration for player ${player.name.string}.")
        socket = createSocket()
        socket.connect()
    }

    open fun stop() {
        logger.info("Stopping ${integrationType.integrationName} Stream Integration for player ${player.name.string}.")
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
        ServerPlayNetworking.send(player, Identifier("lifelinesmp", "warning_type"), PacketByteBufs.create().apply {
            writeEnumConstant(WarningTypes.INVALID_API_KEY)
        })

        player.sendMessage(
            Text.literal("Your ${integrationType.integrationName} failed to connect due to an invalid API key, please fix it!").formatted(
            Formatting.RED))
    }
}