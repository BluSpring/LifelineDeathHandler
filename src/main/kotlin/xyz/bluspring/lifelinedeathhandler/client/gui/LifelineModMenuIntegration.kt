package xyz.bluspring.lifelinedeathhandler.client.gui

import com.charleskorn.kaml.Yaml
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import me.shedaniel.clothconfig2.api.ConfigBuilder
import net.minecraft.text.Text
import xyz.bluspring.lifelinedeathhandler.client.LifelineDeathHandlerClient
import xyz.bluspring.lifelinedeathhandler.client.config.LifelineClientConfig
import xyz.bluspring.lifelinedeathhandler.client.config.StreamIntegrationType
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
                                }
                            }.build()
                        )
                    }.build()
                )
            }.build()
        }
    }
}