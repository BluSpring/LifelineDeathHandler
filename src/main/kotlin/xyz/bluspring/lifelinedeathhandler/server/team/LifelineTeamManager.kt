package xyz.bluspring.lifelinedeathhandler.server.team

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.bluspring.lifelinedeathhandler.team.LifelineTeam
import java.io.File

object LifelineTeamManager {
    val teams = mutableMapOf<String, LifelineTeam>()
    private val dataFile = File(FabricLoader.getInstance().gameDir.toFile(), "lifeline_teams.json")
    private val logger: Logger = LoggerFactory.getLogger(LifelineTeamManager::class.java)

    fun stop() {
        logger.info("Saving teams data..")
        save()
    }

    fun load() {
        if (!dataFile.exists())
            return

        logger.info("Setting up LifelineSMP teams..")

        val json = JsonParser.parseString(dataFile.readText()).asJsonObject
        json.entrySet().forEach {
            try {
                teams[it.key] = LifelineTeam.deserialize(it.value.asJsonObject)
                logger.info("Loaded team ${teams[it.key]!!.name.string}")
            } catch (e: Exception) {
                logger.error("Failed to read team ID ${it.key}!")
                e.printStackTrace()
            }
        }
    }

    fun save() {
        if (!dataFile.exists())
            dataFile.createNewFile()

        val json = JsonObject()
        teams.forEach {
            json.add(it.key, it.value.serialize())
        }

        dataFile.writeText(json.toString())
    }

    fun getPlayerTeam(player: ServerPlayerEntity): LifelineTeam? {
        return teams.values.firstOrNull {
            it.players.any { lifelinePlayer ->
                lifelinePlayer.uuid == player.uuid
            }
        }
    }
}