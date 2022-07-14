package xyz.bluspring.lifelinedeathhandler.team

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.math.Vec3i
import xyz.bluspring.lifelinedeathhandler.util.ColorHelper

data class LifelineTeam(
    var lives: Int,
    var name: Text,
    val players: MutableList<LifelinePlayer>
) {
    fun serialize(): JsonObject {
        return JsonObject().apply {
            addProperty("lives", lives)
            addProperty("name", Text.Serializer.toJson(name))
            add("players", JsonArray().apply {
                players.forEach {
                    add(it.serialize())
                }
            })
        }
    }

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

        fun deserialize(data: JsonObject): LifelineTeam {
            return LifelineTeam(
                data.get("lives").asInt,
                Text.Serializer.fromJson(data.get("name").asString) ?: Text.literal("name broke pls fix"),
                mutableListOf<LifelinePlayer>().apply {
                    data.getAsJsonArray("players").forEach {
                        add(LifelinePlayer.deserialize(it.asJsonObject))
                    }
                }
            )
        }
    }
}