package xyz.bluspring.lifelinedeathhandler.client.gui

import dev.lambdaurora.spruceui.Position
import dev.lambdaurora.spruceui.SpruceTexts
import dev.lambdaurora.spruceui.background.SimpleColorBackground
import dev.lambdaurora.spruceui.border.SimpleBorder
import dev.lambdaurora.spruceui.screen.SpruceScreen
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget
import dev.lambdaurora.spruceui.widget.SpruceTexturedButtonWidget
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import xyz.bluspring.lifelinedeathhandler.client.LifelineDeathHandlerClient
import xyz.bluspring.lifelinedeathhandler.client.gui.spruceui.SpruceScrollableContainerWidget


class TeamLivesScreen(private val parent: Screen? = null) : SpruceScreen(Text.of("LifelineSMP Teams")) {
    override fun init() {
        super.init()

        val client = MinecraftClient.getInstance()!!

        addDrawableChild(
            SpruceScrollableContainerWidget(Position.of(0, 24), client.window.scaledWidth, height - 64, 0).apply {
                background = SimpleColorBackground(0, 0, 0, 127)
                var y = 35

                LifelineDeathHandlerClient.teams.forEach {
                    println("this should be team ${it.key}")

                    addChild(
                        SpruceContainerWidget(Position.of(0, y), client.window.scaledWidth - 14 - 6, 60).apply {
                            println("and this should be a container")

                            val label = SpruceLabelWidget(Position.of(width / 6, 0), it.value.name, client.textRenderer.getWidth(it.value.name) + 2)

                            addChild(label)

                            it.value.players.forEachIndexed { index, lifelinePlayer ->
                                println("this should be player ${lifelinePlayer.name}")

                                /*addChild(
                                    SpruceTexturedButtonWidget(
                                        Position.of(label.width + (4 * (index + 1)), 10),
                                        20, 20,
                                        Text.of(lifelinePlayer.name),
                                        true,
                                        {},
                                        8, 8, 4, lifelinePlayer.getSkinTexture()
                                    )
                                )*/

                                addChild(
                                    SpruceLabelWidget(
                                        Position.of(width / 2, 10 * index),
                                        Text.of(lifelinePlayer.name),
                                        client.textRenderer.getWidth(lifelinePlayer.name) + 2
                                    )
                                )
                            }
                        }
                    )

                    y += 70
                }
            }
        )

        addDrawableChild(SpruceButtonWidget(
            Position.of(this, width / 2 - 75, height - 29), 150, 20, SpruceTexts.GUI_DONE
        ) { this.client!!.setScreen(parent) })
    }

    override fun renderTitle(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        drawCenteredText(matrices, textRenderer, title, width / 2, 8, 16777215)
    }
}