package xyz.bluspring.lifelinedeathhandler.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.lifelinedeathhandler.client.gui.WarningHud;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    WarningHud warningHud;

    @Inject(at = @At("RETURN"), method = "renderStatusEffectOverlay")
    public void renderWarningHud(MatrixStack matrices, CallbackInfo ci) {
        if (warningHud == null)
            warningHud = new WarningHud(MinecraftClient.getInstance());

        warningHud.render(matrices);
    }
}
