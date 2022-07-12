package xyz.bluspring.lifelinedeathhandler.client

import com.charleskorn.kaml.Yaml
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.lifelinedeathhandler.client.config.LifelineClientConfig
import java.io.File

class LifelineDeathHandlerClient : ClientModInitializer {
    private val logger: Logger = LoggerFactory.getLogger(LifelineDeathHandlerClient::class.java)

    override fun onInitializeClient() {
        val configFile = File(FabricLoader.getInstance().configDir.toFile(), "lifeline_client_config.yml")

        try {
            config = Yaml.default.decodeFromString(LifelineClientConfig.serializer(), configFile.readText())
        } catch (e: Exception) {
            logger.error("Failed to read config! Creating new file...")
            e.printStackTrace()

            if (!configFile.exists())
                configFile.createNewFile()

            configFile.writeText(LifelineDeathHandlerClient::class.java.classLoader.getResource("client_config.yml")!!.readText())
        }
    }

    companion object {
        lateinit var config: LifelineClientConfig

        var isEnabled = false
    }
}