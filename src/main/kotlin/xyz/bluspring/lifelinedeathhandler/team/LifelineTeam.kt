package xyz.bluspring.lifelinedeathhandler.team

import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.math.Vec3i
import xyz.bluspring.lifelinedeathhandler.util.ColorHelper

data class LifelineTeam(
    var lives: Int,
    var name: Text,
    val players: MutableList<LifelinePlayer>
) {
    companion object {
        private val dangerValue = Vec3i(0xFF, 0x30, 0x33)
        private val warningValue = Vec3i(0xFF, 0xB7, 0x32)
        private val safeValue = Vec3i(0x7F, 0xFF, 0x66)

        fun getColorFromLives(lives: Int): TextColor {
            return TextColor.fromRgb(ColorHelper.rgbToInt(ColorHelper.linearGradient(listOf(
                dangerValue,
                warningValue,
                safeValue
            ), lives.toDouble() / 50)))
        }
    }
}