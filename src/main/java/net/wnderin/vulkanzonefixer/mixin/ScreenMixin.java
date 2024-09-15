package net.wnderin.vulkanzonefixer.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.wnderin.vulkanzonefixer.BalmDrawHelper;
import net.wnderin.vulkanzonefixer.NoDarkInventoryTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(
            method = "renderInGameBackground(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void vulkanzonefixer$cancelInGameBackground(DrawContext context, CallbackInfo ci) {
        if (NoDarkInventoryTracker.isDarkBackgroundDisabled()) {
            ci.cancel();
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At("HEAD"),
            require = 0
    )
    private void vulkanzonefixer$captureDrawContext(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BalmDrawHelper.activeContext = context;
    }

    @Inject(
            method = "renderWithTooltip(Lnet/minecraft/client/gui/DrawContext;IIF)V",
            at = @At("HEAD"),
            require = 0
    )
    private void vulkanzonefixer$captureDrawContextTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BalmDrawHelper.activeContext = context;
    }
}
