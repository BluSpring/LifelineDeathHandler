package xyz.bluspring.lifelinedeathhandler.server.team

import net.minecraft.server.network.ServerPlayerEntity
import xyz.bluspring.lifelinedeathhandler.team.LifelineTeam

object LifelineTeamManager {
    val teams = mutableMapOf<String, LifelineTeam>()

    fun getPlayerTeam(player: ServerPlayerEntity): LifelineTeam? {
        return teams.values.firstOrNull {
            it.players.any { lifelinePlayer ->
                lifelinePlayer.uuid == player.uuid
            }
        }
    }
}