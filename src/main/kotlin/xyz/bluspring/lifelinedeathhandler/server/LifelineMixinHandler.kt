package xyz.bluspring.lifelinedeathhandler.server

import net.minecraft.entity.EntityType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTracker
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Formatting
import xyz.bluspring.lifelinedeathhandler.server.team.LifelineTeamManager
import xyz.bluspring.lifelinedeathhandler.server.team.LifelineTeamManager.getPlayerTeam

object LifelineMixinHandler {
    fun handleDeathMessages(tracker: DamageTracker): Text {
        val deathMessage = tracker.deathMessage

        if (tracker.entity.type != EntityType.PLAYER)
            return deathMessage

        return if (deathMessage.content is TranslatableTextContent) {
            val translatable = (deathMessage.content as TranslatableTextContent)

            Text.translatable(
                "lifeline.death.mainMessage",
                translatable.args[0],
                Text.translatable(
                    "lifeline.${translatable.key}",
                    *(translatable.args.slice(1 until translatable.args.size).toTypedArray())
                )
            ).formatted(Formatting.RED)
        } else {
            deathMessage
        }
    }

    fun handlePostDeathMessages(player: ServerPlayerEntity, source: DamageSource) {
        val team = getPlayerTeam(player) ?: return
        team.lives -= 1
        player.server.playerManager.playerList.filter { getPlayerTeam(it) == team }.forEach {
            it.sendMessage(
                Text.literal("[!] Your team has lost a life!")
                    .formatted(Formatting.RED)
                    .append(Text.literal(" You now have ${team.lives} lives.").formatted(Formatting.YELLOW))
            )
        }

        if (source.attacker != null && source.attacker!!.type == EntityType.PLAYER) {
            val attackerTeam = getPlayerTeam((source.attacker as ServerPlayerEntity))

            if (attackerTeam != null) {
                attackerTeam.lives += 1

                player.server.playerManager.playerList.filter { getPlayerTeam(it) == attackerTeam }.forEach {
                    it.sendMessage(
                        Text.literal("[!] Your team has gained a life!")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal(" You now have ${attackerTeam.lives} lives.").formatted(Formatting.YELLOW))
                    )
                }

                val attackerId = LifelineTeamManager.teams.entries.first { it.value == attackerTeam }.key
                LifelineDeathHandlerServer.updateTeams(mapOf(attackerId to attackerTeam), player.server.playerManager.playerList)
            }
        }

        LifelineTeamManager.save()
        val id = LifelineTeamManager.teams.entries.first { it.value == team }.key
        LifelineDeathHandlerServer.updateTeams(mapOf(id to team), player.server.playerManager.playerList)
    }
}