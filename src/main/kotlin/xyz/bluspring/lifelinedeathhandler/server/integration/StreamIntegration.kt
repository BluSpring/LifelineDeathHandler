package xyz.bluspring.lifelinedeathhandler.server.integration

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

abstract class StreamIntegration(val player: ServerPlayerEntity, val apiKey: String, val twitchUsername: String) {
    abstract fun start()
    abstract fun stop()

    protected fun sendInvalidIntegrationError() {
        ServerPlayNetworking.send(player, Identifier("lifelinesmp", "integration_invalid"), PacketByteBufs.empty())
    }
}