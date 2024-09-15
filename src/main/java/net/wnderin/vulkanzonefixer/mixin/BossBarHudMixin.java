package net.wnderin.vulkanzonefixer.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.boss.BossBar;
import net.wnderin.vulkanzonefixer.CustomHudFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {

    @Inject(
            method = "renderBossBar(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/entity/boss/BossBar;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void vulkanzonefixer$interceptCustomHudBossBar(DrawContext context, int x, int y, BossBar bossBar, CallbackInfo ci) {
        if (bossBar != null && CustomHudFixer.isBrokenHudText(bossBar.getName())) {
            ci.cancel();
        }
    }

    @Inject(
            method = "render(Lnet/minecraft/client/gui/DrawContext;)V",
            at = @At("RETURN")
    )
    private void vulkanzonefixer$renderCustomHud(DrawContext context, CallbackInfo ci) {
        CustomHudFixer.render(context);
    }
}
