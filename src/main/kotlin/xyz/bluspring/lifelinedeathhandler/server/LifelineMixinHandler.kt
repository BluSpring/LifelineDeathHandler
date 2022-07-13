package xyz.bluspring.lifelinedeathhandler.server

import net.minecraft.entity.EntityType
import net.minecraft.entity.damage.DamageTracker
import net.minecraft.text.Text
import net.minecraft.text.TranslatableTextContent
import net.minecraft.util.Formatting

object LifelineMixinHandler {
    fun handleDeathMessages(tracker: DamageTracker): Text {
        val deathMessage = tracker.deathMessage

        if (tracker.entity.type != EntityType.PLAYER)
            return deathMessage

        return if (deathMessage.content is TranslatableTextContent) {
            val translatable = (deathMessage.content as TranslatableTextContent)

            Text.translatable(
                "lifeline.death.mainMessage",
                translatable.args[0],
                Text.translatable(
                    "lifeline.${translatable.key}",
                    *(translatable.args.slice(1 until translatable.args.size).toTypedArray())
                )
            ).formatted(Formatting.RED)
        } else {
            deathMessage
        }
    }
}