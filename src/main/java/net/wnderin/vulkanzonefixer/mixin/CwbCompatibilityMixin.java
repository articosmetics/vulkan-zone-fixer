package net.wnderin.vulkanzonefixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.kir.cubeswithoutborders.client.ResizableGameRenderer", remap = false)
public abstract class CwbCompatibilityMixin {

    @Inject(method = "isEnabled", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanzonefixer$disableInVulkan(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "beginRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanzonefixer$cancelBeginRender(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "endRender", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanzonefixer$cancelEndRender(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "reload", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanzonefixer$cancelReload(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "resize", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanzonefixer$cancelResize(int width, int height, CallbackInfo ci) {
        ci.cancel();
    }
}
