package xyz.bluspring.lifelinedeathhandler.client

import com.charleskorn.kaml.Yaml
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.lifelinedeathhandler.client.config.LifelineClientConfig
import xyz.bluspring.lifelinedeathhandler.client.gui.WarningHud
import xyz.bluspring.lifelinedeathhandler.client.gui.WarningTypes
import xyz.bluspring.lifelinedeathhandler.team.LifelinePlayer
import xyz.bluspring.lifelinedeathhandler.team.LifelineTeam
import java.io.File
import java.util.*

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
                val team = teams[buf.readString()] ?: return@registerReceiver
                val totalLives = buf.readInt()

                team.lives = totalLives
            }

            ClientPlayNetworking.registerReceiver(
                Identifier("lifelinesmp", "team_update")
            ) { _, _, buf, _ ->
                val teamId = buf.readString()
                val teamName = buf.readText()
                val teamLives = buf.readInt()
                val teamPlayers = buf.readList {
                    val uuid = it.readUuid()
                    val name = it.readString()

                    LifelinePlayer(name, uuid).apply {
                        getSkinTexture() // Cache the texture beforehand so it's ready
                    }
                }

                teams[teamId] = LifelineTeam(
                    teamLives,
                    teamName,
                    teamPlayers
                )
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            isEnabled = false
        }

        // testing purposes
        if (ENABLE_TESTING_STUFF) {
            teams["test1"] = LifelineTeam(
                50, Text.of("Experimental Team 1"),
                mutableListOf(
                    LifelinePlayer("BluSpring", UUID.fromString("031c8ca8-03b1-41a3-a745-fc0415a67adf")),
                    LifelinePlayer("Dove_612", UUID.fromString("87789563-ea0a-47f7-b777-8022212d52c7")),
                    LifelinePlayer("driftingbubbles", UUID.fromString("dbe08ad2-4289-48af-9122-4584b3118d58")),
                    LifelinePlayer("Wookuz", UUID.fromString("65edeeef-ae5d-4438-998a-a8bd1c88fedf"))
                )
            )

            teams["test2"] = LifelineTeam(
                2, Text.of("Experimental Team 2"),
                mutableListOf(
                    LifelinePlayer("FuriousSyndrome", UUID.fromString("536c4f64-ca63-40cc-bef8-45f49a1609af")),
                    LifelinePlayer("princetobi", UUID.fromString("89794f04-0ef0-4fee-a46a-07ea419281af")),
                    LifelinePlayer("SilckySilver", UUID.fromString("34c370b3-e87e-4548-b886-894bec079af6")),
                    LifelinePlayer("Rift115", UUID.fromString("77880497-2c91-4242-8e81-364d7e0a28f5"))
                )
            )

            teams["test3"] = LifelineTeam(
                39, Text.of("Experimental Team 3"),
                mutableListOf(
                    LifelinePlayer("FuriousSyndrome", UUID.fromString("536c4f64-ca63-40cc-bef8-45f49a1609af")),
                    LifelinePlayer("princetobi", UUID.fromString("89794f04-0ef0-4fee-a46a-07ea419281af")),
                    LifelinePlayer("SilckySilver", UUID.fromString("34c370b3-e87e-4548-b886-894bec079af6")),
                    LifelinePlayer("Rift115", UUID.fromString("77880497-2c91-4242-8e81-364d7e0a28f5"))
                )
            )

            teams["test4"] = LifelineTeam(
                16, Text.of("Experimental Team 4"),
                mutableListOf(
                    LifelinePlayer("FuriousSyndrome", UUID.fromString("536c4f64-ca63-40cc-bef8-45f49a1609af")),
                    LifelinePlayer("princetobi", UUID.fromString("89794f04-0ef0-4fee-a46a-07ea419281af")),
                    LifelinePlayer("SilckySilver", UUID.fromString("34c370b3-e87e-4548-b886-894bec079af6")),
                    LifelinePlayer("Rift115", UUID.fromString("77880497-2c91-4242-8e81-364d7e0a28f5"))
                )
            )

            teams["test5"] = LifelineTeam(
                24, Text.of("Experimental Team 5"),
                mutableListOf(
                    LifelinePlayer("FuriousSyndrome", UUID.fromString("536c4f64-ca63-40cc-bef8-45f49a1609af")),
                    LifelinePlayer("princetobi", UUID.fromString("89794f04-0ef0-4fee-a46a-07ea419281af")),
                    LifelinePlayer("SilckySilver", UUID.fromString("34c370b3-e87e-4548-b886-894bec079af6")),
                    LifelinePlayer("Rift115", UUID.fromString("77880497-2c91-4242-8e81-364d7e0a28f5"))
                )
            )

            teams["test6"] = LifelineTeam(
                32, Text.of("Experimental Team 6"),
                mutableListOf(
                    LifelinePlayer("FuriousSyndrome", UUID.fromString("536c4f64-ca63-40cc-bef8-45f49a1609af")),
                    LifelinePlayer("princetobi", UUID.fromString("89794f04-0ef0-4fee-a46a-07ea419281af")),
                    LifelinePlayer("SilckySilver", UUID.fromString("34c370b3-e87e-4548-b886-894bec079af6")),
                    LifelinePlayer("Rift115", UUID.fromString("77880497-2c91-4242-8e81-364d7e0a28f5"))
                )
            )
        }
    }

    companion object {
        const val ENABLE_TESTING_STUFF = false

        lateinit var config: LifelineClientConfig

        var isEnabled = false
        val configFile = File(FabricLoader.getInstance().configDir.toFile(), "lifeline_client_config.yml")
        val teams = mutableMapOf<String, LifelineTeam>()

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