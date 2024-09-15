package net.wnderin.vulkanzonefixer.mixin;

import net.minecraft.client.gui.DrawContext;
import net.wnderin.vulkanzonefixer.BalmDrawHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.blay09.mods.balm.client.screen.click.dropdown.panel.CategoryPanel", remap = false)
public abstract class CategoryPanelMixin {

    @Inject(
            method = "render",
            at = @At("HEAD"),
            require = 0
    )
    private void vulkanzonefixer$onCategoryRenderHead(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        BalmDrawHelper.activeContext = context;
    }
}
