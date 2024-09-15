package net.wnderin.vulkanzonefixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.microcontrollers.simpleblockoverlay.util.RenderUtil", remap = false)
public abstract class SimpleBlockOverlayFixMixin {

    @Inject(
        method = "extractAndDrawWaypoint",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void vulkanzonefixer$stopBuggyRenderPass(CallbackInfo ci) {
        ci.cancel();
        try {
            java.lang.reflect.Field field = this.getClass().getDeclaredField("config");
            field.setAccessible(true);
            Object config = field.get(this);
            if (config != null) {
                java.lang.reflect.Field crField = config.getClass().getField("customRenderer");
                crField.setBoolean(config, false);
            }
        } catch (Throwable ignored) {
        }
    }
}
