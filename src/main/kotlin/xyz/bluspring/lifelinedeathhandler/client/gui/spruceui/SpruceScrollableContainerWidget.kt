package xyz.bluspring.lifelinedeathhandler.client.gui.spruceui

import com.mojang.blaze3d.systems.RenderSystem
import dev.lambdaurora.spruceui.Position
import dev.lambdaurora.spruceui.border.Border
import dev.lambdaurora.spruceui.border.EmptyBorder
import dev.lambdaurora.spruceui.util.ScissorManager
import dev.lambdaurora.spruceui.widget.SpruceWidget
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW

/*
For some reason, SpruceUI doesn't provide a scrollable container class by itself, so I ended up just
creating that widget myself. It's mostly just a modified version of SpruceEntryListWidget, and ported
to Kotlin.
 */
class SpruceScrollableContainerWidget(position: Position, width: Int, height: Int, val anchorYOffset: Int) : SpruceContainerWidget(position, width, height) {
    private var scrolling = false
    private val anchor = Position.of(0, 0)
    private var customBorder: Border = EmptyBorder.EMPTY_BORDER

    var scrollAmount = 0.0
        set(value) {
            val newValue = MathHelper.clamp(value, 0.0, maxScroll.toDouble())
            anchor.relativeY = (anchorYOffset + border!!.thickness - newValue).toInt()

            children().forEach {
                it.isVisible = !(it.y + it.height < y || it.y > y + height)
            }

            field = newValue
        }

    val maxScroll: Int
        get() {
            return 0.coerceAtLeast(maxPosition - height + 8)
        }

    val maxPosition: Int
        get() {
            var max = 0
            for (i in 0 until children().size) {
                max += this.children()[i].height + 8
            }
            return max
        }

    val scrollbarPositionX: Int
        get() {
            return x + width - 6 - border!!.thickness
        }

    init {
        anchor.relativeY = anchorYOffset
    }

    override fun getBorder(): Border? {
        return customBorder
    }

    override fun addChild(child: SpruceWidget) {
        super.addChild(child)

        child.position.anchor = anchor
    }

    override fun setBorder(border: Border?) {
        customBorder = border!!

        anchor.relativeY = border.thickness

        if (anchor.relativeY == anchorYOffset && hasBorder())
            anchor.relativeY = anchorYOffset + border.thickness
    }

    override fun onMouseClick(mouseX: Double, mouseY: Double, button: Int): Boolean {
        scrolling = button == GLFW.GLFW_MOUSE_BUTTON_1 && mouseX >= scrollbarPositionX && mouseX < (scrollbarPositionX + 6)
        return super.onMouseClick(mouseX, mouseY, button) || scrolling
    }

    override fun onMouseDrag(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (super.onMouseDrag(mouseX, mouseY, button, deltaX, deltaY))
            return true
        else if (button == GLFW.GLFW_MOUSE_BUTTON_1 && this.scrolling) {
            scrollAmount = if (mouseY < y) {
                0.0
            } else if (mouseY > (y + height)) {
                maxScroll.toDouble()
            } else {
                val d = 1.coerceAtLeast(maxScroll)
                val j = MathHelper.clamp(((height * height) / maxPosition), 32, height - 8)
                val e = 1.coerceAtLeast(d / (height - j))

                scrollAmount + deltaY * e
            }

            return true
        }

        return false
    }

    override fun onMouseScroll(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        if (super.onMouseScroll(mouseX, mouseY, amount))
            return true

        scrollAmount -= amount * (maxPosition / children().size) / 2
        return true
    }

    override fun renderWidget(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        val scrollbarEnd = scrollbarPositionX + 6

        ScissorManager.push(x, y, width, height)
        children().forEach {
            it.render(matrices, mouseX, mouseY, delta)
        }
        ScissorManager.pop()

        if (maxScroll > 0) {
            val tessellator = Tessellator.getInstance()
            val buffer = tessellator.buffer

            RenderSystem.disableTexture()
            val scrollbarHeight = MathHelper.clamp(((height * height) / maxPosition), 32, height - 8)
            var scrollbarY = (scrollAmount * (height - scrollbarHeight) / maxScroll + y).toInt()
            if (scrollbarY < y)
                scrollbarY = y

            renderScrollbar(tessellator, buffer, scrollbarPositionX, scrollbarEnd, scrollbarY, scrollbarHeight)
        }

        border!!.render(matrices, this, mouseX, mouseY, delta)

        RenderSystem.enableTexture()
        RenderSystem.disableBlend()
    }

    fun renderScrollbar(tessellator: Tessellator, buffer: BufferBuilder, scrollbarX: Int, scrollbarEndX: Int, scrollbarY: Int, scrollbarHeight: Int) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader)
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        buffer.vertex(scrollbarX.toDouble(), (y + height).toDouble(), 0.0).color(0, 0, 0, 255).next()
        buffer.vertex(scrollbarEndX.toDouble(), (y + height).toDouble(), 0.0).color(0, 0, 0, 255).next()
        buffer.vertex(scrollbarEndX.toDouble(), y.toDouble(), 0.0).color(0, 0, 0, 255).next()
        buffer.vertex(scrollbarX.toDouble(), y.toDouble(), 0.0).color(0, 0, 0, 255).next()
        buffer.vertex(scrollbarX.toDouble(), (scrollbarY + scrollbarHeight).toDouble(), 0.0).color(128, 128, 128, 255).next()
        buffer.vertex(scrollbarEndX.toDouble(), (scrollbarY + scrollbarHeight).toDouble(), 0.0).color(128, 128, 128, 255).next()
        buffer.vertex(scrollbarEndX.toDouble(), scrollbarY.toDouble(), 0.0).color(128, 128, 128, 255).next()
        buffer.vertex(scrollbarX.toDouble(), scrollbarY.toDouble(), 0.0).color(128, 128, 128, 255).next()
        buffer.vertex(scrollbarX.toDouble(), (scrollbarY + scrollbarHeight - 1).toDouble(), 0.0).color(192, 192, 192, 255).next()
        buffer.vertex(scrollbarEndX.toDouble() - 1, (scrollbarY + scrollbarHeight - 1).toDouble(), 0.0).color(192, 192, 192, 255).next()
        buffer.vertex(scrollbarEndX.toDouble() - 1, scrollbarY.toDouble(), 0.0).color(192, 192, 192, 255).next()
        buffer.vertex(scrollbarX.toDouble(), scrollbarY.toDouble(), 0.0).color(192, 192, 192, 255).next()
        tessellator.draw()
    }
}