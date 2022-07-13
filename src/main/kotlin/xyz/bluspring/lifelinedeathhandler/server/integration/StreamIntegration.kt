package xyz.bluspring.lifelinedeathhandler.server.integration

import net.minecraft.server.network.ServerPlayerEntity

abstract class StreamIntegration(val player: ServerPlayerEntity, val apiKey: String) {
    abstract fun authenticate()


}