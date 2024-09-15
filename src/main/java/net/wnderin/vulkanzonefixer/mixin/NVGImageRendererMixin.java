package net.wnderin.vulkanzonefixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Pseudo
@Mixin(targets = "net.blay09.mods.balm.client.renderer.image.NVGImageRenderer", remap = false)
public abstract class NVGImageRendererMixin {

    @Redirect(
            method = "<init>(Ljava/io/InputStream;I)V",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/nanovg/NanoVG;nvgCreateImageMem(JILjava/nio/ByteBuffer;)I"),
            require = 0
    )
    private int vulkanzonefixer$redirectCreateImageMem(long ctx, int flags, ByteBuffer data) {
        return 0;
    }

    @Inject(method = "drawImage(FFFF)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanzonefixer$cancelDrawImage(float x, float y, float w, float h, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "drawImage(FFFFI)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanzonefixer$cancelDrawImageColor(float x, float y, float w, float h, int color, CallbackInfo ci) {
        ci.cancel();
    }
}
