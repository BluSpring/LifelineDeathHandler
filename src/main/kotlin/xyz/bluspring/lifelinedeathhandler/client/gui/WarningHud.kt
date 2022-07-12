package xyz.bluspring.lifelinedeathhandler.client.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import xyz.bluspring.lifelinedeathhandler.client.LifelineDeathHandlerClient
import xyz.bluspring.lifelinedeathhandler.client.config.StreamIntegrationType

class WarningHud(private val client: MinecraftClient) : DrawableHelper() {
    fun render(matrices: MatrixStack) {
        val config = LifelineDeathHandlerClient.config
        if (config.apiKey.isBlank() && LifelineDeathHandlerClient.isEnabled) {
            matrices.push()
            matrices.translate(4.0, (client.window.scaledHeight / 2).toDouble() - 12.0, 0.0)
            matrices.scale(1F, 1F, 1F)

            this.client.textRenderer.drawWithShadow(matrices, "Your ${config.type.name} is not currently set up!", 0F, 0F, 16777215)
            this.client.textRenderer.drawWithShadow(matrices, "Please set it up in your mod menu immediately", 0F, 12F, 16777215)
            this.client.textRenderer.drawWithShadow(matrices, "under Mods -> LifelineDeathHandler", 0F, 24F, 16777215)

            matrices.pop()
        }
    }
}