package xyz.bluspring.lifelinedeathhandler.team

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.util.Identifier
import java.util.*

data class LifelinePlayer(
    val name: String,
    val uuid: UUID
) {
    private var cachedSkin: Identifier? = null

    @Environment(EnvType.CLIENT)
    fun getSkinTexture(): Identifier? {
        val networkHandler = MinecraftClient.getInstance().networkHandler

        return if (networkHandler != null && networkHandler.playerList.any { it.profile.id == uuid }) {
            val player = networkHandler.playerList.first { it.profile.id == uuid }

            player.skinTexture
        } else if (cachedSkin != null) {
            cachedSkin
        } else {
            MinecraftClient.getInstance().skinProvider.loadSkin(GameProfile(uuid, name), { type, id, _ ->
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    cachedSkin = id
                }
            }, true)

            cachedSkin ?: DefaultSkinHelper.getTexture(uuid)
        }
    }
}