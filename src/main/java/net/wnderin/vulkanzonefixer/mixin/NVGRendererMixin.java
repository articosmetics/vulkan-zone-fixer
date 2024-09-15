package net.wnderin.vulkanzonefixer.mixin;

import net.wnderin.vulkanzonefixer.BalmDrawHelper;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.blay09.mods.balm.client.renderer.NVGRenderer", remap = false)
public abstract class NVGRendererMixin {

    @Redirect(
            method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/nanovg/NanoVGGL3;nvgCreate(I)J"),
            require = 0
    )
    private static long vulkanzonefixer$redirectNvgCreate(int flags) {
        return 0L;
    }

    @Inject(method = "getContext", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeGetContext(CallbackInfoReturnable<Long> cir) {
        cir.setReturnValue(0L);
    }

    @Inject(method = "beginFrame", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeBeginFrame(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "endFrame", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeEndFrame(boolean b, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "endFrameAndReset", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeEndFrameAndReset(boolean b, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "clearScissors", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeClearScissors(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "globalAlpha", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeGlobalAlpha(float alpha, CallbackInfo ci) {
        BalmDrawHelper.globalAlpha = alpha;
        ci.cancel();
    }

    @Inject(method = "rect(FFFFI)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRectColor(float x, float y, float w, float h, int color, CallbackInfo ci) {
        BalmDrawHelper.fill(x, y, w, h, color);
        ci.cancel();
    }

    @Inject(method = "rect(FFFFLorg/lwjgl/nanovg/NVGPaint;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRectPaint(float x, float y, float w, float h, NVGPaint paint, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "scale", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeScale(float originX, float originY, float targetX, float targetY, float scale, Runnable runnable, CallbackInfo ci) {
        if (runnable != null) {
            runnable.run();
        }
        ci.cancel();
    }

    @Inject(method = "rectStroke", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRectStroke(float x, float y, float w, float h, float stroke, int c1, int c2, CallbackInfo ci) {
        BalmDrawHelper.fill(x, y, w, h, c1);
        ci.cancel();
    }

    @Inject(method = "rotate", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRotate(double angle, float x, float y, float cx, float cy, Runnable runnable, CallbackInfo ci) {
        if (runnable != null) {
            runnable.run();
        }
        ci.cancel();
    }

    @Inject(method = "rectOutlineStroke", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRectOutlineStroke(float x, float y, float w, float h, float stroke, float radius, int c1, int c2, CallbackInfo ci) {
        BalmDrawHelper.outline(x, y, w, h, stroke, c1);
        ci.cancel();
    }

    @Inject(method = "rectOutline", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRectOutline(float x, float y, float w, float h, float stroke, int color, CallbackInfo ci) {
        BalmDrawHelper.outline(x, y, w, h, stroke, color);
        ci.cancel();
    }

    @Inject(method = "rainbowRect", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRainbowRect(float x, float y, float w, float h, CallbackInfo ci) {
        BalmDrawHelper.fill(x, y, w, h, 0x80808080);
        ci.cancel();
    }

    @Inject(method = "roundedRectOutline", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRoundedRectOutline(float x, float y, float w, float h, float radius, float stroke, int color, CallbackInfo ci) {
        BalmDrawHelper.outline(x, y, w, h, stroke, color);
        ci.cancel();
    }

    @Inject(method = "scissor", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeScissor(float x, float y, float w, float h, Runnable runnable, CallbackInfo ci) {
        BalmDrawHelper.scissor(x, y, w, h, runnable);
        ci.cancel();
    }

    @Inject(method = "useCurrentScissors", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeUseCurrentScissors(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "roundedRect(FFFFFI)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRoundedRectColor(float x, float y, float w, float h, float radius, int color, CallbackInfo ci) {
        BalmDrawHelper.roundedRect(x, y, w, h, radius, color);
        ci.cancel();
    }

    @Inject(method = "roundedRect(FFFFFLorg/lwjgl/nanovg/NVGPaint;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRoundedRectPaint(float x, float y, float w, float h, float radius, NVGPaint paint, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "roundedRectGradient", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRoundedRectGradient(float x, float y, float w, float h, float radius, int c1, int c2, float angle, CallbackInfo ci) {
        BalmDrawHelper.fillGradient(x, y, w, h, c1, c2);
        ci.cancel();
    }

    @Inject(method = "gradientSeparator", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeGradientSeparator(float x, float y, float w, float h, int color, CallbackInfo ci) {
        BalmDrawHelper.fill(x, y, w, h, color);
        ci.cancel();
    }

    @Inject(method = "rectGradient", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRectGradient(float x, float y, float w, float h, int c1, int c2, float radius, CallbackInfo ci) {
        BalmDrawHelper.rectGradient(x, y, w, h, c1, c2, radius);
        ci.cancel();
    }

    @Inject(method = "roundedRectVarying(FFFFFFFFI)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRoundedRectVaryingColor(float x, float y, float w, float h, float r1, float r2, float r3, float r4, int color, CallbackInfo ci) {
        BalmDrawHelper.roundedRectVarying(x, y, w, h, r1, r2, r3, r4, color);
        ci.cancel();
    }

    @Inject(method = "roundedRectVarying(FFFFFFFFLorg/lwjgl/nanovg/NVGPaint;)V", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRoundedRectVaryingPaint(float x, float y, float w, float h, float r1, float r2, float r3, float r4, NVGPaint paint, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "roundedRectVaryingGradient", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeRoundedRectVaryingGradient(float x, float y, float w, float h, float r1, float r2, float r3, float r4, int c1, int c2, float angle, CallbackInfo ci) {
        BalmDrawHelper.fillGradient(x, y, w, h, c1, c2);
        ci.cancel();
    }

    @Inject(method = "applyColor", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeApplyColor(int color, NVGColor nvgColor, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "createNVGPaintFromTex", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeCreateNVGPaintFromTex(int x, int y, int z, NVGPaint paint, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "ensureSystemFontLoaded", at = @At("HEAD"), cancellable = true, require = 0)
    private static void vulkanzonefixer$safeEnsureSystemFontLoaded(CallbackInfo ci) {
        ci.cancel();
    }
}
