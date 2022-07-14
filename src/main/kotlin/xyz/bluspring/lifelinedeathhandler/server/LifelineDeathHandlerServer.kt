package xyz.bluspring.lifelinedeathhandler.server

import com.charleskorn.kaml.Yaml
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType
import xyz.bluspring.lifelinedeathhandler.server.config.LifelineServerConfig
import xyz.bluspring.lifelinedeathhandler.server.integration.StreamIntegrationManager
import xyz.bluspring.lifelinedeathhandler.team.LifelineTeam
import java.io.File

class LifelineDeathHandlerServer : DedicatedServerModInitializer {
    private val logger: Logger = LoggerFactory.getLogger(LifelineDeathHandlerServer::class.java)

    override fun onInitializeServer() {
        config = try {
            Yaml.default.decodeFromString(LifelineServerConfig.serializer(), configFile.readText())
        } catch (e: Exception) {
            logger.error("Failed to read config! Creating new file...")
            e.printStackTrace()

            if (!configFile.exists())
                configFile.createNewFile()

            val configText = LifelineDeathHandlerServer::class.java.classLoader.getResource("server_config.yml")!!.readText()
            configFile.writeText(configText)
            Yaml.default.decodeFromString(LifelineServerConfig.serializer(), configText)
        }

        ServerPlayConnectionEvents.INIT.register { handler, server ->
            ServerPlayNetworking.send(handler.getPlayer(), Identifier("lifelinesmp", "initialize"), PacketByteBufs.empty())

            ServerPlayNetworking.registerReceiver(handler, Identifier("lifelinesmp", "stream_integration"))
            { _, player, _, buf, _ ->
                try {
                    val twitchUsername = buf.readString()
                    val integrationType = StreamIntegrationType.valueOf(buf.readString())
                    val apiKey = buf.readString()

                    StreamIntegrationManager.registerIntegration(player, twitchUsername, integrationType, apiKey)
                } catch (e: Exception) {
                    handler.disconnect(Text.of("LifelineDeathHandler Error whilst parsing stream integration: $e"))
                }
            }
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            StreamIntegrationManager.integrations.remove(handler.getPlayer())
        }
    }

    companion object {
        lateinit var config: LifelineServerConfig

        val configFile = File(FabricLoader.getInstance().configDir.toFile(), "lifeline_server_config.yml")
        val teams = mutableMapOf<String, LifelineTeam>()
    }
}