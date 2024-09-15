package net.wnderin.vulkanzonefixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@Pseudo
@Mixin(targets = "net.vulkanmod.vulkan.framebuffer.SwapChain", remap = false)
public abstract class VulkanSwapChainSafetyMixin {

    @Inject(method = "hasImages", at = @At("HEAD"), cancellable = true, require = 0)
    private void vulkanzonefixer$validateSwapChain(CallbackInfoReturnable<Boolean> cir) {
        try {
            Field hasImagesField = this.getClass().getDeclaredField("hasImages");
            hasImagesField.setAccessible(true);
            if (!hasImagesField.getBoolean(this)) {
                cir.setReturnValue(false);
                return;
            }

            Field imagesField = this.getClass().getDeclaredField("swapChainImages");
            imagesField.setAccessible(true);
            List<?> images = (List<?>) imagesField.get(this);
            if (images == null || images.isEmpty()) {
                cir.setReturnValue(false);
                return;
            }
            for (Object img : images) {
                if (img == null) {
                    cir.setReturnValue(false);
                    return;
                }
            }

            Field extentField = this.getClass().getDeclaredField("extent2D");
            extentField.setAccessible(true);
            Object extent = extentField.get(this);
            if (extent != null) {
                Method widthMethod = extent.getClass().getMethod("width");
                Method heightMethod = extent.getClass().getMethod("height");
                int w = (int) widthMethod.invoke(extent);
                int h = (int) heightMethod.invoke(extent);
                if (w <= 0 || h <= 0) {
                    cir.setReturnValue(false);
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
