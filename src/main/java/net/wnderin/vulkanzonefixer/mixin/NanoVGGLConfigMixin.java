package net.wnderin.vulkanzonefixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.lwjgl.nanovg.NanoVGGLConfig", remap = false)
public abstract class NanoVGGLConfigMixin {

    @Inject(method = "configGL", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$cancelConfigGL(long nvg, CallbackInfo ci) {
        ci.cancel();
    }
}
