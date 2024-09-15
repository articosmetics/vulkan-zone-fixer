package net.wnderin.vulkanzonefixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
    "com.mojang.blaze3d.textures.GpuTexture",
    "net.minecraft.class_10868"
}, remap = false)
public abstract class GpuTextureMixin {

    @Inject(method = "getLabel", at = @At("RETURN"), cancellable = true, require = 0)
    private void vulkanzonefixer$safeNumericLabel(CallbackInfoReturnable<String> cir) {
        String label = cir.getReturnValue();
        if (label != null && !label.isEmpty()) {
            boolean isNumber = true;
            for (int i = 0; i < label.length(); i++) {
                char c = label.charAt(i);
                if (i == 0 && (c == '-' || c == '+')) continue;
                if (!Character.isDigit(c)) {
                    isNumber = false;
                    break;
                }
            }
            if (!isNumber) {
                for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                    String cls = elem.getClassName();
                    if (cls.contains("balm") || cls.contains("ShaderFramebuffer")) {
                        cir.setReturnValue("0");
                        return;
                    }
                }
            }
        }
    }
}
