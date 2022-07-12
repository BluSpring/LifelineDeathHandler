package xyz.bluspring.lifelinedeathhandler.client

import com.charleskorn.kaml.Yaml
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.impl.networking.client.ClientNetworkingImpl
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.lifelinedeathhandler.client.config.LifelineClientConfig
import xyz.bluspring.lifelinedeathhandler.client.gui.WarningHud
import xyz.bluspring.lifelinedeathhandler.client.gui.WarningTypes
import java.io.File

class LifelineDeathHandlerClient : ClientModInitializer {
    private val logger: Logger = LoggerFactory.getLogger(LifelineDeathHandlerClient::class.java)

    override fun onInitializeClient() {
        config = try {
            Yaml.default.decodeFromString(LifelineClientConfig.serializer(), configFile.readText())
        } catch (e: Exception) {
            logger.error("Failed to read config! Creating new file...")
            e.printStackTrace()

            if (!configFile.exists())
                configFile.createNewFile()

            val configText = LifelineDeathHandlerClient::class.java.classLoader.getResource("client_config.yml")!!.readText()
            configFile.writeText(configText)
            Yaml.default.decodeFromString(LifelineClientConfig.serializer(), configText)
        }

        ClientPlayNetworking.registerGlobalReceiver(
            Identifier("lifelinesmp", "initialize")
        ) { _, _, _, _ ->
            isEnabled = true
            sendApiKey()

            ClientPlayNetworking.registerReceiver(
                Identifier("lifelinesmp", "integration_invalid")
            ) { _, _, _, _ ->
                WarningHud.warningType = WarningTypes.INVALID_API_KEY
            }

            ClientPlayNetworking.registerReceiver(
                Identifier("lifelinesmp", "life_update")
            ) { _, _, buf, _ ->

            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            isEnabled = false
        }
    }

    companion object {
        lateinit var config: LifelineClientConfig

        var isEnabled = false
        val configFile = File(FabricLoader.getInstance().configDir.toFile(), "lifeline_client_config.yml")

        fun sendApiKey() {
            if (isEnabled) {
                val buf = PacketByteBufs.create()
                buf.writeString(config.type.name)
                buf.writeString(config.apiKey)

                ClientPlayNetworking.send(Identifier("lifelinesmp", "stream_integration"), buf)
            }
        }
    }
}