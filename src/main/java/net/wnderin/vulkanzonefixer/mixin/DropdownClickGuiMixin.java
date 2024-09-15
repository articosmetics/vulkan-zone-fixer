package net.wnderin.vulkanzonefixer.mixin;

import net.minecraft.client.gui.DrawContext;
import net.wnderin.vulkanzonefixer.BalmDrawHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.blay09.mods.balm.client.screen.click.dropdown.DropdownClickGUI", remap = false)
public abstract class DropdownClickGuiMixin {

    @Inject(
            method = {"method_25394", "render"},
            at = @At("HEAD"),
            require = 0
    )
    private void vulkanzonefixer$onDropdownRenderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BalmDrawHelper.activeContext = context;
    }

    @Inject(
            method = {"method_25394", "render"},
            at = @At("RETURN"),
            require = 0
    )
    private void vulkanzonefixer$onDropdownRenderReturn(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (BalmDrawHelper.activeContext == context) {
            BalmDrawHelper.activeContext = null;
        }
    }
}
