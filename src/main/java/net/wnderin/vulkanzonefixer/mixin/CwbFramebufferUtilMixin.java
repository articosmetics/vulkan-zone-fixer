package net.wnderin.vulkanzonefixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.kir.cubeswithoutborders.client.util.FramebufferUtil", remap = false)
public abstract class CwbFramebufferUtilMixin {

    @Inject(method = "draw", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$cancelDraw(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "beginWrite", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$cancelBeginWrite(CallbackInfo ci) {
        ci.cancel();
    }
}
