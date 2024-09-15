package net.wnderin.vulkanzonefixer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "net.blay09.mods.balm.client.renderer.shader.ShaderFramebuffer", remap = false)
public abstract class BalmCompatibilityMixin {

    /**
     * @author Wnderin
     * @reason Prevent Balm crash on VulkanMod when trying to parse non-numeric texture labels like 'Swapchain'
     */
    @Overwrite(remap = false)
    public static void onResized(int width, int height) {
        // Safely no-op on VulkanMod to prevent NumberFormatException
    }

    /**
     * @author Wnderin
     * @reason Prevent Balm blur crash on VulkanMod
     */
    @Overwrite(remap = false)
    public static void applyBlurToFullScreen() {
        // No-op
    }

    /**
     * @author Wnderin
     * @reason Prevent Balm glow crash on VulkanMod
     */
    @Overwrite(remap = false)
    public static void applyGlowToNVGObjects() {
        // No-op
    }
}
