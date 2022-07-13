package xyz.bluspring.lifelinedeathhandler.mixin;

import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.lifelinedeathhandler.client.gui.TeamLivesScreen;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "init")
    public void onInit(CallbackInfo ci) {
        this.addDrawableChild(new SpruceButtonWidget(Position.of(0, 12), 150, 20, Text.of("SpruceUI Test Menu"),
                btn -> this.client.setScreen(new TeamLivesScreen(this))).asVanilla());
    }
}
