package xyz.bluspring.lifelinedeathhandler.mixin.server;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.lifelinedeathhandler.server.LifelineDeathHandlerServer;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow protected abstract MutableText addTellClickEvent(MutableText component);

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addTellClickEvent(Lnet/minecraft/text/MutableText;)Lnet/minecraft/text/MutableText;"), method = "getDisplayName")
    public MutableText replaceDisplayName(PlayerEntity instance, MutableText component) {
        var clickEvent = this.addTellClickEvent(component);

        // I'm not sure if this gets called on the client-side, but just in case.
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER)
            return clickEvent;

        if (LifelineDeathHandlerServer.Companion.getLiveManager().isLive((PlayerEntity) (Object) this))
            return Text.literal("⛆ ").append(clickEvent);
        else
            return clickEvent;
    }
}
