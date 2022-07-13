package xyz.bluspring.lifelinedeathhandler.client.gui

import com.charleskorn.kaml.Yaml
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.text.ClickEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import xyz.bluspring.lifelinedeathhandler.client.LifelineDeathHandlerClient
import xyz.bluspring.lifelinedeathhandler.client.config.LifelineClientConfig
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType
import java.util.function.Supplier

class LifelineModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory {
            ConfigBuilder.create().apply {
                parentScreen = it
                title = Text.of("LifelineSMP Death Handler")

                val config = LifelineDeathHandlerClient.config

                savingRunnable = Runnable {
                    LifelineDeathHandlerClient.configFile.writeText(
                        Yaml.default.encodeToString(LifelineClientConfig.serializer(), config)
                    )
                }

                val integration = getOrCreateCategory(Text.of("Stream Integration"))
                val integrationEntryBuilder = entryBuilder()

                integration.addEntry(
                    integrationEntryBuilder.startStrField(
                        Text.of("Twitch Username"),
                        config.twitchUsername
                    ).apply {
                        defaultValue = Supplier { "" }

                        setSaveConsumer {
                            config.apiKey = it.lowercase()

                            LifelineDeathHandlerClient.sendApiKey()
                        }
                    }.build()
                )

                integration.addEntry(
                    integrationEntryBuilder.startEnumSelector(
                        Text.of("Stream Integration Type"),
                        StreamIntegrationType::class.java,
                        config.type
                    ).apply {
                        defaultValue = Supplier {
                            StreamIntegrationType.StreamElements
                        }

                        setSaveConsumer {
                            config.type = it
                        }
                    }.build()
                )

                integration.addEntry(
                    integrationEntryBuilder.startSubCategory(Text.of("Stream Integration Settings (DO NOT SHOW ON STREAM!)")).apply {
                        this.add(
                            integrationEntryBuilder.startStrField(
                                Text.of("Stream Integration API Key"),
                                config.apiKey
                            ).apply {
                                defaultValue = Supplier {
                                    ""
                                }

                                setSaveConsumer {
                                    config.apiKey = it

                                    LifelineDeathHandlerClient.sendApiKey()
                                }
                            }.build()
                        )

                        this.add(
                            integrationEntryBuilder.startTextDescription(
                                Text.of("You may acquire the stream integration API keys here:")
                            ).build()
                        )

                        this.add(
                            integrationEntryBuilder.startTextDescription(
                                Text.of("StreamElements: https://streamelements.com/dashboard/account/channels (under Show Secrets, paste the JWT token here)").copy().setStyle(
                                    Style.EMPTY.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://streamelements.com/dashboard/account/channels")))
                            ).build()
                        )

                        this.add(
                            integrationEntryBuilder.startTextDescription(
                                Text.of("Streamlabs: https://streamlabs.com/dashboard#/settings/api-settings (paste the Socket API token here)").copy().setStyle(
                                    Style.EMPTY.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, "https://streamlabs.com/dashboard#/settings/api-settings")))
                            ).build()
                        )
                    }.build()
                )
            }.build()
        }
    }
}