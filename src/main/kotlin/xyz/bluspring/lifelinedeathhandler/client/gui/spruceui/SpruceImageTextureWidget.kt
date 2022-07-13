package xyz.bluspring.lifelinedeathhandler.client.gui.spruceui

import com.mojang.blaze3d.systems.RenderSystem
import dev.lambdaurora.spruceui.Position
import dev.lambdaurora.spruceui.widget.AbstractSpruceWidget
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

open class SpruceImageTextureWidget(
    widgetPosition: Position,
    widgetWidth: Int,
    widgetHeight: Int,
    var identifier: Identifier,
    var u: Float,
    var v: Float,
    var regionWidth: Int,
    var regionHeight: Int,
    var textureWidth: Int,
    var textureHeight: Int
) : AbstractSpruceWidget(widgetPosition) {
    init {
        this.width = widgetWidth
        this.height = widgetHeight
    }

    override fun renderWidget(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
        RenderSystem.enableTexture()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()

        RenderSystem.setShaderTexture(0, identifier)
        DrawableHelper.drawTexture(matrices, position.x, position.y, u, v, regionWidth, regionHeight, textureWidth, textureHeight)

        RenderSystem.disableBlend()
        RenderSystem.disableTexture()
    }
}