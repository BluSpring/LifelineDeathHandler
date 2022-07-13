package xyz.bluspring.lifelinedeathhandler.client.gui.spruceui

import com.mojang.blaze3d.systems.RenderSystem
import dev.lambdaurora.spruceui.Position
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SpruceImageTextureWithTooltipWidget(
    widgetPosition: Position,
    widgetWidth: Int,
    widgetHeight: Int,
    identifier: Identifier,
    u: Float,
    v: Float,
    regionWidth: Int,
    regionHeight: Int,
    textureWidth: Int,
    textureHeight: Int,
    var tooltipText: Text
) : SpruceImageTextureWidget(
    widgetPosition, widgetWidth, widgetHeight, identifier, u, v, regionWidth, regionHeight, textureWidth, textureHeight
) {
    override fun renderWidget(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderWidget(matrices, mouseX, mouseY, delta)

        if (isMouseOver(mouseX.toDouble(), mouseY.toDouble())) {
            val x = mouseX.toDouble()
            val y = mouseY.toDouble() + 12

            val padding = 3

            val client = MinecraftClient.getInstance()!!

            // This is supposed to draw a background for the text.
            // However, it just fails to build a buffer, and I don't understand why.
            /*val tessellator = Tessellator.getInstance()
            val buffer = tessellator.buffer

            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
            buffer.vertex(x - padding, y - padding, 0.0).color(0, 0, 0, 32).next()
            buffer.vertex(x + client.textRenderer.getWidth(tooltipText) + padding, y - padding, 0.0)
                .color(0, 0, 0, 32).next()
            buffer.vertex(x - padding, y + client.textRenderer.fontHeight + padding, 0.0).color(0, 0, 0, 32).next()
            buffer.vertex(
                x + client.textRenderer.getWidth(tooltipText) + padding,
                y + client.textRenderer.fontHeight + padding,
                0.0
            ).color(0, 0, 0, 32).next()
            buffer.end()
            tessellator.draw()*/

            client.textRenderer.drawWithShadow(matrices, tooltipText, x.toFloat(), y.toFloat(), 16777215)
        }
    }
}