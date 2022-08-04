package xyz.bluspring.lifelinedeathhandler.server

import com.charleskorn.kaml.Yaml
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.command.argument.TextArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.world.GameMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType
import xyz.bluspring.lifelinedeathhandler.common.WarningTypes
import xyz.bluspring.lifelinedeathhandler.server.config.LifelineServerConfig
import xyz.bluspring.lifelinedeathhandler.server.integration.StreamIntegrationManager
import xyz.bluspring.lifelinedeathhandler.server.integration.twitch.LiveManager
import xyz.bluspring.lifelinedeathhandler.server.integration.twitch.StreamChannel
import xyz.bluspring.lifelinedeathhandler.server.team.LifelineTeamManager
import xyz.bluspring.lifelinedeathhandler.team.LifelinePlayer
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

        ServerPlayConnectionEvents.INIT.register { handler, _ ->
            ServerPlayNetworking.send(handler.getPlayer(), Identifier("lifelinesmp", "initialize"), PacketByteBufs.empty())
            updateTeams(LifelineTeamManager.teams, listOf(handler.player))

            ServerPlayNetworking.registerReceiver(handler, Identifier("lifelinesmp", "stream_integration"))
            { _, player, _, buf, _ ->
                try {
                    val twitchUsername = buf.readString()
                    val integrationType = StreamIntegrationType.valueOf(buf.readString())
                    val apiKey = buf.readString()

                    if (twitchUsername.isBlank()) {
                        ServerPlayNetworking.send(player, Identifier("lifelinesmp", "warning_type"), PacketByteBufs.create().apply {
                            writeEnumConstant(WarningTypes.INVALID_API_KEY)
                        })

                        return@registerReceiver
                    }

                    if (integrationType == StreamIntegrationType.STREAMLABS) {
                        player.sendMessage(Text.literal("LifelineDeathHandler: You're currently using an unsupported stream integration type! (${integrationType.integrationName})").formatted(Formatting.RED))
                        return@registerReceiver
                    }

                    StreamIntegrationManager.registerIntegration(player, twitchUsername, integrationType, apiKey)

                    liveManager.addChannel(player, twitchUsername)
                } catch (e: Exception) {
                    handler.disconnect(Text.of("LifelineDeathHandler: Error whilst parsing stream integration: $e"))
                }
            }
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.getPlayer()
            val team = LifelineTeamManager.getPlayerTeam(player) ?: return@register

            val scoreboardTeam = player.server.scoreboard.getTeam("lf_${LifelineTeamManager.teams.keys.find { key -> LifelineTeamManager.teams[key] == team }}")

            if (scoreboardTeam != null) {
                handler.player.server.scoreboard.addPlayerToTeam(player.entityName, scoreboardTeam)
            }

            if (team.lives <= 0) {
                player.changeGameMode(GameMode.SPECTATOR)
                player.sendMessage(Text.literal("[!] Your team has lost all of their lives, and as a result you're unable to continue playing."))
            }
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            StreamIntegrationManager.unregisterIntegration(handler.getPlayer())
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(
                CommandManager.literal("lifeline")
                    .requires {
                        it.hasPermissionLevel(2)
                    }
                    .then(
                        CommandManager.literal("playerinfo")
                            .executes {
                                it.source.sendFeedback(
                                    Text.literal("Players integrated with LifelineDeathHandler:")
                                        .formatted(Formatting.DARK_AQUA),
                                    false
                                )

                                it.source.server.playerManager.playerList.forEach { player ->
                                    it.source.sendFeedback(
                                        Text.literal("- ")
                                            .append(
                                                player.name.copy().setStyle(
                                                    Style.EMPTY
                                                        .withColor(
                                                        LifelineTeamManager.getPlayerTeam(player)?.name?.style?.color
                                                            ?: TextColor.fromFormatting(Formatting.WHITE)
                                                        )
                                                        .withHoverEvent(
                                                            HoverEvent(
                                                                HoverEvent.Action.SHOW_TEXT,
                                                                LifelineTeamManager.getPlayerTeam(player)?.name ?: Text.of("Not in any team")
                                                            )
                                                        )
                                                )
                                            )
                                            .append(
                                                Text.of(" - ")
                                            )
                                            .append(
                                                if (StreamIntegrationManager.integrations.contains(player) && StreamIntegrationManager.integrations[player]?.authorized == true)
                                                    Text.literal("LINKED").formatted(Formatting.GREEN)
                                                else if (StreamIntegrationManager.integrations[player]?.authorized == false)
                                                    Text.literal("UNAUTHORIZED").formatted(Formatting.YELLOW)
                                                else
                                                    Text.literal("UNLINKED").formatted(Formatting.RED)
                                            )
                                            .append(
                                                if (StreamIntegrationManager.integrations.contains(player))
                                                    Text.literal(" (${StreamIntegrationManager.integrations[player]!!.integrationType.integrationName})")
                                                else
                                                    Text.empty()
                                            ),
                                        false
                                    )
                                }

                                1
                            }
                    )
                    .then(
                        CommandManager.literal("team")
                            .then(
                                CommandManager.literal("create")
                                    .then(CommandManager.argument("id", StringArgumentType.string())
                                        .then(
                                            CommandManager.argument("name", TextArgumentType.text())
                                            .executes {
                                                val id = it.getArgument("id", String::class.java)
                                                val name = it.getArgument("name", Text::class.java)

                                                if (LifelineTeamManager.teams.contains(id)) {
                                                    it.source.sendError(Text.literal("Team $id already exists!").formatted(Formatting.RED))
                                                    return@executes 1
                                                }

                                                LifelineTeamManager.teams[id] = LifelineTeam(
                                                    config.defaultLives,
                                                    name,
                                                    mutableListOf()
                                                )
                                                LifelineTeamManager.save()

                                                val scoreboardTeam = it.source.server.scoreboard.addTeam("lf_$id")
                                                scoreboardTeam.displayName = name
                                                scoreboardTeam.color = Formatting.byName(name.style.color?.name ?: "WHITE")
                                                scoreboardTeam.prefix = name.copy().formatted(Formatting.BOLD).append(Text.literal(" "))
                                                scoreboardTeam.isFriendlyFireAllowed = false

                                                updateTeams(mapOf(id to LifelineTeamManager.teams[id]!!), it.source.server.playerManager.playerList)

                                                it.source.sendFeedback(Text.literal("Successfully created team ").append(name), false)

                                                1
                                            }
                                        )
                                    )
                            )
                            .then(
                                CommandManager.literal("delete")
                                    .then(
                                        CommandManager.argument("id", StringArgumentType.string())
                                            .suggests { _, builder ->
                                                builder.apply {
                                                    LifelineTeamManager.teams.forEach {
                                                        suggest(it.key) {
                                                            it.value.name.string
                                                        }
                                                    }
                                                }.buildFuture()
                                            }
                                            .executes {
                                                val id = it.getArgument("id", String::class.java)

                                                if (!LifelineTeamManager.teams.contains(id)) {
                                                    it.source.sendError(Text.literal("Team $id does not exist!").formatted(Formatting.RED))
                                                    return@executes 1
                                                }

                                                val team = LifelineTeamManager.teams[id]!!

                                                it.source.server.scoreboard.removeTeam(it.source.server.scoreboard.getTeam("lf_$id"))

                                                LifelineTeamManager.teams.remove(id)
                                                LifelineTeamManager.save()

                                                updateTeams(mapOf(id to LifelineTeam(0, Text.literal(""), mutableListOf())), it.source.server.playerManager.playerList)
                                                it.source.sendFeedback(Text.literal("Successfully deleted team ").append(team.name), false)

                                                1
                                            }
                                    )
                            )
                            .then(
                                CommandManager.literal("modify")
                                    .then(
                                        CommandManager.argument("id", StringArgumentType.string())
                                            .suggests { _, builder ->
                                                builder.apply {
                                                    LifelineTeamManager.teams.forEach {
                                                        suggest(it.key) {
                                                            it.value.name.string
                                                        }
                                                    }
                                                }.buildFuture()
                                            }.then(
                                                CommandManager.argument("name", TextArgumentType.text())
                                                    .executes {
                                                        val id = it.getArgument("id", String::class.java)
                                                        val text = it.getArgument("name", Text::class.java)

                                                        if (!LifelineTeamManager.teams.contains(id)) {
                                                            it.source.sendError(Text.literal("Team $id does not exist!").formatted(Formatting.RED))
                                                            return@executes 1
                                                        }

                                                        val team = LifelineTeamManager.teams[id]!!
                                                        val oldName = team.name

                                                        team.name = text
                                                        LifelineTeamManager.save()

                                                        val scoreboardTeam = it.source.server.scoreboard.getTeam("lf_$id") ?: it.source.server.scoreboard.addTeam("lf_$id")
                                                        scoreboardTeam.color = Formatting.byName(text.style.color?.name ?: "WHITE")
                                                        scoreboardTeam.displayName = text
                                                        scoreboardTeam.prefix = team.name.copy().formatted(Formatting.BOLD).append(Text.literal(" "))
                                                        scoreboardTeam.isFriendlyFireAllowed = false

                                                        updateTeams(mapOf(id to LifelineTeamManager.teams[id]!!), it.source.server.playerManager.playerList)

                                                        it.source.sendFeedback(
                                                            Text.literal("Successfully changed team ")
                                                                .append(oldName)
                                                                .append(Text.literal(" to "))
                                                                .append(text),
                                                            false
                                                        )

                                                        1
                                                    }
                                            )
                                    )
                            )
                            .then(
                                CommandManager.literal("player")
                                    .then(
                                        CommandManager.literal("add")
                                            .then(
                                                CommandManager.argument("id", StringArgumentType.string())
                                                    .suggests { _, builder ->
                                                        builder.apply {
                                                            LifelineTeamManager.teams.forEach {
                                                                suggest(it.key) {
                                                                    it.value.name.string
                                                                }
                                                            }
                                                        }.buildFuture()
                                                    }
                                                    .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                                        .executes {
                                                            val id = StringArgumentType.getString(it, "id")
                                                            val players = GameProfileArgumentType.getProfileArgument(it, "player")

                                                            if (players.isEmpty()) {
                                                                it.source.sendError(Text.literal("No players exist by that name!").formatted(Formatting.RED))
                                                                return@executes 1
                                                            }

                                                            val player = players.first()

                                                            if (!LifelineTeamManager.teams.contains(id)) {
                                                                it.source.sendError(Text.literal("Team $id does not exist!").formatted(Formatting.RED))
                                                                return@executes 1
                                                            }

                                                            val team = LifelineTeamManager.teams[id]!!
                                                            if (team.players.any { pl -> pl.uuid == player.id }) {
                                                                it.source.sendError(Text.literal("Player is already in team!").formatted(Formatting.RED))
                                                                return@executes 1
                                                            }

                                                            val originalTeam = LifelineTeamManager.getPlayerTeam(player)

                                                            if (originalTeam != null) {
                                                                originalTeam.players.removeIf { pl -> pl.uuid == player.id }
                                                                it.source.sendFeedback(Text.literal("Player was removed from their original team ").append(originalTeam.name).append(Text.literal(" to join this team.")), false)
                                                            }

                                                            team.players.add(LifelinePlayer(
                                                                player.name,
                                                                player.id
                                                            ))
                                                            LifelineTeamManager.save()

                                                            val scoreboardTeam = it.source.server.scoreboard.getTeam("lf_$id") ?: it.source.server.scoreboard.addTeam("lf_$id")
                                                            scoreboardTeam.color = Formatting.byName(team.name.style.color?.name ?: "WHITE")
                                                            scoreboardTeam.displayName = team.name
                                                            scoreboardTeam.prefix = team.name.copy().formatted(Formatting.BOLD).append(Text.literal(" "))
                                                            scoreboardTeam.isFriendlyFireAllowed = false

                                                            it.source.server.scoreboard.addPlayerToTeam(player.name, scoreboardTeam)
                                                            updateTeams(mapOf(id to LifelineTeamManager.teams[id]!!), it.source.server.playerManager.playerList)

                                                            it.source.sendFeedback(Text.literal("${player.name} has been successfully added to team ").append(team.name), false)

                                                            1
                                                        })
                                            )
                                    )
                                    .then(
                                        CommandManager.literal("remove")
                                            .then(
                                                CommandManager.argument("id", StringArgumentType.string())
                                                    .suggests { _, builder ->
                                                        builder.apply {
                                                            LifelineTeamManager.teams.forEach {
                                                                suggest(it.key) {
                                                                    it.value.name.string
                                                                }
                                                            }
                                                        }.buildFuture()
                                                    }
                                                    .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                                        .executes {
                                                            val id = StringArgumentType.getString(it, "id")
                                                            val players = GameProfileArgumentType.getProfileArgument(it, "player")

                                                            if (players.isEmpty()) {
                                                                it.source.sendError(Text.literal("No players exist by that name!").formatted(Formatting.RED))
                                                                return@executes 1
                                                            }

                                                            val player = players.first()

                                                            if (!LifelineTeamManager.teams.contains(id)) {
                                                                it.source.sendError(Text.literal("Team $id does not exist!").formatted(Formatting.RED))
                                                                return@executes 1
                                                            }

                                                            val team = LifelineTeamManager.teams[id]!!
                                                            if (!team.players.any { pl -> pl.uuid == player.id }) {
                                                                it.source.sendError(Text.literal("Player is not in the team!").formatted(Formatting.RED))
                                                                return@executes 1
                                                            }

                                                            it.source.server.scoreboard.clearPlayerTeam(player.name)
                                                            team.players.removeIf { pl -> pl.uuid == player.id }
                                                            LifelineTeamManager.save()

                                                            updateTeams(mapOf(id to LifelineTeamManager.teams[id]!!), it.source.server.playerManager.playerList)

                                                            it.source.sendFeedback(Text.literal("${player.name} has been removed from team ").append(team.name), false)

                                                            1
                                                        }
                                                    )
                                            )
                                    )
                            )
                    )
            )
        }

        ServerLifecycleEvents.SERVER_STARTING.register {
            LifelineTeamManager.load()

            liveManager = LiveManager(it)
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
            LifelineTeamManager.teams.forEach { (id, team) ->
                val scoreboardTeam = it.scoreboard.getTeam("lf_$id") ?: it.scoreboard.addTeam("lf_$id")

                scoreboardTeam.displayName = team.name
                scoreboardTeam.prefix = team.name.copy().formatted(Formatting.BOLD).append(Text.literal(" "))
                scoreboardTeam.color = Formatting.byName(team.name.style.color?.name ?: "WHITE")
                scoreboardTeam.isFriendlyFireAllowed = false
            }
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            LifelineTeamManager.stop()
        }
    }

    companion object {
        lateinit var config: LifelineServerConfig
        lateinit var liveManager: LiveManager

        val configFile = File(FabricLoader.getInstance().configDir.toFile(), "lifeline_server_config.yml")

        fun updateTeams(teams: Map<String, LifelineTeam>, players: List<ServerPlayerEntity>) {
            teams.forEach { (id, team) ->
                val buf = PacketByteBufs.create()
                buf.writeString(id)
                buf.writeText(team.name)
                buf.writeInt(team.lives)
                buf.writeCollection(team.players) { packetBuf, player ->
                    packetBuf.writeUuid(player.uuid)
                    packetBuf.writeString(player.name)
                }

                players.forEach { player ->
                    ServerPlayNetworking.send(player, Identifier("lifelinesmp", "team_update"), buf)
                }
            }
        }
    }
}