package xyz.bluspring.lifelinedeathhandler.client.gui

import dev.lambdaurora.spruceui.Position
import dev.lambdaurora.spruceui.SpruceTexts
import dev.lambdaurora.spruceui.background.SimpleColorBackground
import dev.lambdaurora.spruceui.screen.SpruceScreen
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import xyz.bluspring.lifelinedeathhandler.client.LifelineDeathHandlerClient
import xyz.bluspring.lifelinedeathhandler.client.gui.spruceui.SpruceImageTextureWidget
import xyz.bluspring.lifelinedeathhandler.client.gui.spruceui.SpruceImageTextureWithTooltipWidget
import xyz.bluspring.lifelinedeathhandler.client.gui.spruceui.SpruceScrollableContainerWidget
import xyz.bluspring.lifelinedeathhandler.team.LifelineTeam


class TeamLivesScreen(private val parent: Screen? = null) : SpruceScreen(Text.of("RoyalSMP Teams")) {
    override fun init() {
        super.init()

        val client = MinecraftClient.getInstance()!!

        addDrawableChild(
            SpruceScrollableContainerWidget(Position.of(0, 24), client.window.scaledWidth, height - 64, 0).apply {
                background = SimpleColorBackground(0, 0, 0, 127)
                var y = 35

                LifelineDeathHandlerClient.teams.forEach {
                    addChild(
                        SpruceContainerWidget(Position.of(0, y), client.window.scaledWidth - 14 - 6, 60).apply {
                            val label = SpruceLabelWidget(Position.of(width / 6, 0), it.value.name, client.textRenderer.getWidth(it.value.name) + 2)

                            addChild(label)

                            it.value.players.forEachIndexed { index, lifelinePlayer ->
                                // Old way of figuring out how the texture thing worked
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

                                val faceX = width / 2 - 12 + ((12 * client.window.scaleFactor.toInt()) * index)
                                val size = 8 * client.window.scaleFactor.toInt()

                                // This is the face layer
                                addChild(
                                    SpruceImageTextureWidget(
                                        Position.of(faceX, height / 2),
                                        size, size,
                                        lifelinePlayer.getSkinTexture()!!,
                                        (8 * client.window.scaleFactor).toFloat(), (8 * client.window.scaleFactor).toFloat(),
                                        (8 * client.window.scaleFactor).toInt(), (8 * client.window.scaleFactor).toInt(),
                                        (64 * client.window.scaleFactor).toInt(), (64 * client.window.scaleFactor).toInt()
                                    )
                                )

                                // This is the hat layer
                                addChild(
                                    SpruceImageTextureWithTooltipWidget(
                                        Position.of(faceX, height / 2),
                                        size, size,
                                        lifelinePlayer.getSkinTexture()!!,
                                        (40 * client.window.scaleFactor).toFloat(), (8 * client.window.scaleFactor).toFloat(),
                                        (8 * client.window.scaleFactor).toInt(), (8 * client.window.scaleFactor).toInt(),
                                        (64 * client.window.scaleFactor).toInt(), (64 * client.window.scaleFactor).toInt(),
                                        Text.of(lifelinePlayer.name)
                                    )
                                )

                                // Username-based
                                /*addChild(
                                    SpruceLabelWidget(
                                        Position.of(width / 2, 10 * index),
                                        Text.of(lifelinePlayer.name),
                                        client.textRenderer.getWidth(lifelinePlayer.name) + 2
                                    )
                                )*/
                            }

                            val posX = 72

                            val heartSize = 9
                            val heartU = 52F
                            val heartV = 0F
                            val heartScale = (2 * client.window.scaleFactor).toInt()

                            if (it.value.lives <= 0) {
                                addChild(
                                    SpruceImageTextureWidget(
                                        Position.of(width - posX, height / 4),
                                        heartSize * heartScale, heartSize * heartScale,
                                        Identifier("minecraft", "textures/gui/icons.png"),
                                        heartU * heartScale, (heartV + 9) * heartScale,
                                        heartSize * heartScale, heartSize * heartScale,
                                        256 * heartScale, 256 * heartScale
                                    )
                                )
                            } else {
                                addChild(
                                    SpruceImageTextureWidget(
                                        Position.of(width - posX, height / 4),
                                        heartSize * heartScale, heartSize * heartScale,
                                        Identifier("minecraft", "textures/gui/icons.png"),
                                        heartU * heartScale, heartV * heartScale,
                                        heartSize * heartScale, heartSize * heartScale,
                                        256 * heartScale, 256 * heartScale
                                    )
                                )
                            }

                            addChild(
                                SpruceLabelWidget(
                                    Position.of(width - posX + 4 + (heartSize * heartScale), height / 2),
                                    Text.of("x ${it.value.lives}").copy().setStyle(Style.EMPTY.withColor(
                                        LifelineTeam.getColorFromLives(it.value.lives))),
                                    client.textRenderer.getWidth(Text.of("x ${it.value.lives}"))
                                )
                            )
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