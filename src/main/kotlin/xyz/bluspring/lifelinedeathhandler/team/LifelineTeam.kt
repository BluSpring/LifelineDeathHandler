package xyz.bluspring.lifelinedeathhandler.team

import net.minecraft.text.Text

data class LifelineTeam(
    var lives: Int,
    val name: Text,
    val players: MutableList<LifelinePlayer>
)
