package net.wnderin.vulkanzonefixer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.wnderin.vulkanzonefixer.VulkanZoneFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(value = MinecraftClient.class, priority = 500)
public abstract class MinecraftClientMixin {

    @Shadow public abstract void setScreen(Screen screen);

    @Unique
    private static boolean vulkanzonefixer$isRendering = false;

    @Inject(
            method = "setScreenAndRender(Lnet/minecraft/client/gui/screen/Screen;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void vulkanzonefixer$safeSetScreenAndRender(Screen screen, CallbackInfo ci) {
        if (vulkanzonefixer$isRendering) {
            this.setScreen(screen);
            ci.cancel();
        }
    }

    @Inject(
            method = "render(Z)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void vulkanzonefixer$preventRecursiveRender(boolean tick, CallbackInfo ci) {
        if (vulkanzonefixer$isRendering) {
            ci.cancel();
            return;
        }
        vulkanzonefixer$isRendering = true;
    }

    @Inject(
            method = "render(Z)V",
            at = @At("RETURN"),
            require = 0
    )
    private void vulkanzonefixer$postRender(boolean tick, CallbackInfo ci) {
        vulkanzonefixer$isRendering = false;
    }

    @Inject(
            method = "reloadResources()Ljava/util/concurrent/CompletableFuture;",
            at = @At("RETURN"),
            require = 0
    )
    private void vulkanzonefixer$onReloadResources(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        CompletableFuture<Void> future = cir.getReturnValue();
        if (future != null) {
            future.thenRun(VulkanZoneFixer::clearVulkanPipelineCache);
        } else {
            VulkanZoneFixer.clearVulkanPipelineCache();
        }
    }
}
