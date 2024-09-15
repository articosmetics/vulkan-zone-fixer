package net.wnderin.vulkanzonefixer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.wnderin.vulkanzonefixer.BalmDrawHelper;
import net.wnderin.vulkanzonefixer.NoDarkInventoryTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;)V",
            at = @At("RETURN"),
            require = 0
    )
    private void vulkanzonefixer$captureDrawContext(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers, CallbackInfo ci) {
        BalmDrawHelper.activeContext = (DrawContext) (Object) this;
    }

    @Inject(
            method = "fillGradient(IIIIII)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void vulkanzonefixer$cancelDarkGradient(
            int startX, int startY, int endX, int endY, int colorStart, int colorEnd, CallbackInfo ci) {
        if (NoDarkInventoryTracker.isDarkBackgroundDisabled() && NoDarkInventoryTracker.isDimmingColor(colorStart, colorEnd)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void vulkanzonefixer$cancelBrokenHudText(
            net.minecraft.client.font.TextRenderer textRenderer, net.minecraft.text.Text text, int x, int y, int color, boolean shadow,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Integer> cir) {
        if (net.wnderin.vulkanzonefixer.CustomHudFixer.isBrokenHudText(text)) {
            cir.setReturnValue(0);
        }
    }
}
