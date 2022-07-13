package xyz.bluspring.lifelinedeathhandler.mixin.server;

import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.lifelinedeathhandler.server.LifelineMixinHandler;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageTracker;getDeathMessage()Lnet/minecraft/text/Text;"), method = "onDeath")
    public Text replaceDeathMessageWithCustom(DamageTracker instance) {
        return LifelineMixinHandler.INSTANCE.handleDeathMessages(instance);
    }
}
