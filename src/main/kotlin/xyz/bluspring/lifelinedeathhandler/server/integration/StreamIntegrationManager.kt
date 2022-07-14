package xyz.bluspring.lifelinedeathhandler.server.integration

import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import xyz.bluspring.lifelinedeathhandler.common.StreamIntegrationType
import xyz.bluspring.lifelinedeathhandler.server.LifelineDeathHandlerServer
import xyz.bluspring.lifelinedeathhandler.server.config.viewer.TwitchViewerAssistanceInfo
import xyz.bluspring.lifelinedeathhandler.server.config.viewer.ViewerAssistanceInfo
import xyz.bluspring.lifelinedeathhandler.server.team.LifelineTeamManager
import kotlin.math.floor

object StreamIntegrationManager {
    private val viewerAssistance = LifelineDeathHandlerServer.config.viewerAssistance
    val integrations = mutableMapOf<ServerPlayerEntity, StreamIntegration>()

    fun registerIntegration(player: ServerPlayerEntity, username: String, integrationType: StreamIntegrationType, apiKey: String) {
        integrations[player]?.stop()

        if (integrationType == StreamIntegrationType.StreamElements) {
            integrations[player] = StreamElementsStreamIntegration(player, apiKey, username)
        } else if (integrationType == StreamIntegrationType.Streamlabs) {
            integrations[player] = StreamlabsStreamIntegration(player, apiKey, username)
        }

        integrations[player]!!.start()
    }

    fun handleTwitchSubscription(player: ServerPlayerEntity, tier: TwitchSubscriptionTiers, actor: String) {
        if (!viewerAssistance.twitchSubscription.enabled)
            return

        val team = LifelineTeamManager.getPlayerTeam(player) ?: return

        viewerAssistance.twitchSubscription.types.forEach {
            if (!it.tiers.contains(tier))
                return@forEach

            when (it) {
                is TwitchViewerAssistanceInfo.ItemGiveAssistanceData -> {
                    val itemStack = ItemStack(
                        Registry.ITEM.get(Identifier.tryParse(it.id)), it.count
                    )

                    player.giveItemStack(itemStack)

                    player.sendMessage(
                        Text.of(actor)
                            .copy()
                            .formatted(Formatting.YELLOW)
                            .append(
                                Text.of(" has subscribed to you, and given you").copy().formatted(Formatting.DARK_AQUA)
                            ).append(
                                Text.of(" ${itemStack.count}x ").copy().append(Text.translatable(itemStack.item.translationKey)).formatted(Formatting.YELLOW)
                            ).append(
                                Text.of("!").copy().formatted(Formatting.DARK_AQUA)
                            )
                    )
                }

                is TwitchViewerAssistanceInfo.LifeAddEveryAssistanceData -> {
                    team.lives += it.add

                    player.sendMessage(
                        Text.of(actor)
                            .copy()
                            .formatted(Formatting.YELLOW)
                            .append(
                                Text.of(" has subscribed to you, and given you").copy().formatted(Formatting.DARK_AQUA)
                            ).append(
                                Text.of(" ${it.add} ${if (it.add == 1) "life" else "lives"}").copy().formatted(Formatting.YELLOW)
                            ).append(
                                Text.of("!").copy().formatted(Formatting.DARK_AQUA)
                            )
                    )
                }

                else -> {}
            }
        }
    }

    fun handleTwitchGiftedSubscription(player: ServerPlayerEntity, tier: TwitchSubscriptionTiers, actor: String, amount: Int) {
        if (!viewerAssistance.twitchSubscriptionGift.enabled)
            return

        val team = LifelineTeamManager.getPlayerTeam(player) ?: return

        viewerAssistance.twitchSubscriptionGift.types.forEach {
            if (!it.tiers.contains(tier))
                return@forEach

            when (it) {
                is TwitchViewerAssistanceInfo.ItemGiveAssistanceData -> {
                    if (amount < it.per)
                        return@forEach

                    val itemStack = ItemStack(
                        Registry.ITEM.get(Identifier.tryParse(it.id)), it.count * floor(amount.toDouble() / it.per.toDouble()).toInt()
                    )

                    player.giveItemStack(itemStack)

                    player.sendMessage(
                        Text.of(actor)
                            .copy()
                            .formatted(Formatting.YELLOW)
                            .append(
                                Text.of(" has gifted $amount subscription${if (amount == 1) "" else "s"} to you, and given you").copy().formatted(Formatting.DARK_AQUA)
                            ).append(
                                Text.of(" ${itemStack.count}x ").copy().append(Text.translatable(itemStack.item.translationKey)).formatted(Formatting.YELLOW)
                            ).append(
                                Text.of("!").copy().formatted(Formatting.DARK_AQUA)
                            )
                    )
                }

                is TwitchViewerAssistanceInfo.LifeAddEveryAssistanceData -> {
                    if (amount < it.per)
                        return@forEach

                    val add = it.add * floor(amount.toDouble() / it.per.toDouble()).toInt()
                    team.lives += add

                    player.sendMessage(
                        Text.of(actor)
                            .copy()
                            .formatted(Formatting.YELLOW)
                            .append(
                                Text.of(" has gifted $amount subscription${if (amount == 1) "" else "s"} to you, and given you").copy().formatted(Formatting.DARK_AQUA)
                            ).append(
                                Text.of(" $add ${if (add == 1) "life" else "lives"}").copy().formatted(Formatting.YELLOW)
                            ).append(
                                Text.of("!").copy().formatted(Formatting.DARK_AQUA)
                            )
                    )
                }

                else -> {}
            }
        }
    }

    fun handleTwitchCheer(player: ServerPlayerEntity, actor: String, amount: Int) {
        if (!viewerAssistance.twitchCheer.enabled)
            return

        val team = LifelineTeamManager.getPlayerTeam(player) ?: return

        viewerAssistance.twitchCheer.types.forEach {
            when (it) {
                is ViewerAssistanceInfo.ItemGiveAssistanceData -> {
                    if (amount < it.per)
                        return@forEach

                    val itemStack = ItemStack(
                        Registry.ITEM.get(Identifier.tryParse(it.id)), it.count
                    )

                    player.giveItemStack(itemStack)

                    player.sendMessage(
                        Text.of(actor)
                            .copy()
                            .formatted(Formatting.YELLOW)
                            .append(
                                Text.of(" has cheered $amount bits, and given you").copy().formatted(Formatting.DARK_AQUA)
                            ).append(
                                Text.of(" ${itemStack.count}x ").copy().append(Text.translatable(itemStack.item.translationKey)).formatted(Formatting.YELLOW)
                            ).append(
                                Text.of("!").copy().formatted(Formatting.DARK_AQUA)
                            )
                    )
                }

                is ViewerAssistanceInfo.LifeAddEveryAssistanceData -> {
                    if (amount < it.per)
                        return@forEach

                    team.lives += it.add

                    player.sendMessage(
                        Text.of(actor)
                            .copy()
                            .formatted(Formatting.YELLOW)
                            .append(
                                Text.of(" has cheered $amount bits, and given you").copy().formatted(Formatting.DARK_AQUA)
                            ).append(
                                Text.of(" ${it.add} ${if (it.add == 1) "life" else "lives"}").copy().formatted(Formatting.YELLOW)
                            ).append(
                                Text.of("!").copy().formatted(Formatting.DARK_AQUA)
                            )
                    )
                }

                else -> {}
            }
        }
    }
}