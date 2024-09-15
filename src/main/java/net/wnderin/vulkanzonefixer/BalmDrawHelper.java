package net.wnderin.vulkanzonefixer;

import net.minecraft.client.gui.DrawContext;

public class BalmDrawHelper {
    public static DrawContext activeContext = null;
    public static float globalAlpha = 1.0f;

    public static int applyAlpha(int color) {
        if (globalAlpha >= 1.0f) return color;
        float aFactor = Math.max(0.0f, Math.min(1.0f, globalAlpha));
        int a = (int)(((color >> 24) & 0xFF) * aFactor);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    public static void fill(float x, float y, float width, float height, int color) {
        if (activeContext != null && width > 0 && height > 0) {
            int x1 = Math.round(x);
            int y1 = Math.round(y);
            int x2 = Math.round(x + width);
            int y2 = Math.round(y + height);
            if (x2 > x1 && y2 > y1) {
                activeContext.fill(x1, y1, x2, y2, applyAlpha(color));
            }
        }
    }

    public static void roundedRect(float x, float y, float width, float height, float radius, int color) {
        if (activeContext == null || width <= 0 || height <= 0) return;
        int c = applyAlpha(color);
        float maxR = Math.min(width, height) / 2.0f;
        float r = Math.min(radius, maxR);
        if (r < 1.0f) {
            fill(x, y, width, height, c);
            return;
        }

        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int ih = Math.round(height);
        int ir = Math.round(r);

        // Center body
        if (ih > 2 * ir) {
            activeContext.fill(ix, iy + ir, ix + iw, iy + ih - ir, c);
        }

        // Top and bottom horizontal scanlines with circular insets
        for (int dy = 0; dy < ir; dy++) {
            float distY = ir - dy - 0.5f;
            float dx = ir - (float) Math.sqrt(Math.max(0, ir * ir - distY * distY));
            int idx = Math.round(dx);

            // Top line
            activeContext.fill(ix + idx, iy + dy, ix + iw - idx, iy + dy + 1, c);
            // Bottom line
            activeContext.fill(ix + idx, iy + ih - dy - 1, ix + iw - idx, iy + ih - dy, c);
        }
    }

    public static void roundedRectVarying(float x, float y, float width, float height, float r1, float r2, float r3, float r4, int color) {
        if (activeContext == null || width <= 0 || height <= 0) return;
        int c = applyAlpha(color);

        float maxR = Math.min(width, height) / 2.0f;
        r1 = Math.min(r1, maxR);
        r2 = Math.min(r2, maxR);
        r3 = Math.min(r3, maxR);
        r4 = Math.min(r4, maxR);

        if (r1 < 1.0f && r2 < 1.0f && r3 < 1.0f && r4 < 1.0f) {
            fill(x, y, width, height, c);
            return;
        }

        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int ih = Math.round(height);

        int maxTop = (int) Math.ceil(Math.max(r1, r2));
        int maxBot = (int) Math.ceil(Math.max(r3, r4));

        // Top section
        for (int dy = 0; dy < maxTop; dy++) {
            int leftInset = 0;
            if (dy < r1) {
                float distY = r1 - dy - 0.5f;
                leftInset = Math.round(r1 - (float) Math.sqrt(Math.max(0, r1 * r1 - distY * distY)));
            }
            int rightInset = 0;
            if (dy < r2) {
                float distY = r2 - dy - 0.5f;
                rightInset = Math.round(r2 - (float) Math.sqrt(Math.max(0, r2 * r2 - distY * distY)));
            }
            activeContext.fill(ix + leftInset, iy + dy, ix + iw - rightInset, iy + dy + 1, c);
        }

        // Center section
        if (ih > maxTop + maxBot) {
            activeContext.fill(ix, iy + maxTop, ix + iw, iy + ih - maxBot, c);
        }

        // Bottom section
        for (int dy = 0; dy < maxBot; dy++) {
            int by = maxBot - 1 - dy;
            int leftInset = 0;
            if (by < r4) {
                float distY = r4 - by - 0.5f;
                leftInset = Math.round(r4 - (float) Math.sqrt(Math.max(0, r4 * r4 - distY * distY)));
            }
            int rightInset = 0;
            if (by < r3) {
                float distY = r3 - by - 0.5f;
                rightInset = Math.round(r3 - (float) Math.sqrt(Math.max(0, r3 * r3 - distY * distY)));
            }
            int lineY = iy + ih - maxBot + dy;
            activeContext.fill(ix + leftInset, lineY, ix + iw - rightInset, lineY + 1, c);
        }
    }

    public static void fillGradient(float x, float y, float width, float height, int colorStart, int colorEnd) {
        if (activeContext == null || width <= 0 || height <= 0) return;
        int c1 = applyAlpha(colorStart);
        int c2 = applyAlpha(colorEnd);
        int x1 = Math.round(x);
        int y1 = Math.round(y);
        int x2 = Math.round(x + width);
        int y2 = Math.round(y + height);
        if (x2 > x1 && y2 > y1) {
            activeContext.fillGradient(x1, y1, x2, y2, c1, c2);
        }
    }

    public static void rectGradient(float x, float y, float width, float height, int colorStart, int colorEnd, float radius) {
        if (activeContext == null || width <= 0 || height <= 0) return;
        int c1 = applyAlpha(colorStart);
        int c2 = applyAlpha(colorEnd);

        if (width <= 4) {
            fillGradient(x, y, width, height, c1, c2);
            return;
        }

        // Horizontal gradient: interpolate horizontally across strips
        int steps = Math.min(32, Math.max(8, Math.round(width / 3.0f)));
        float stepW = width / (float) steps;

        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;

        for (int i = 0; i < steps; i++) {
            float t = (i + 0.5f) / (float) steps;
            int a = (int)(a1 + t * (a2 - a1));
            int r = (int)(r1 + t * (r2 - r1));
            int g = (int)(g1 + t * (g2 - g1));
            int b = (int)(b1 + t * (b2 - b1));
            int col = (a << 24) | (r << 16) | (g << 8) | b;

            float sx = x + i * stepW;
            float sw = (i == steps - 1) ? (x + width - sx) : stepW;
            fill(sx, y, sw, height, col);
        }
    }

    public static void outline(float x, float y, float width, float height, float strokeWidth, int color) {
        if (activeContext == null) return;
        fill(x, y, width, strokeWidth, color);
        fill(x, y + height - strokeWidth, width, strokeWidth, color);
        fill(x, y, strokeWidth, height, color);
        fill(x + width - strokeWidth, y, strokeWidth, height, color);
    }

    public static void scissor(float x, float y, float width, float height, Runnable runnable) {
        if (runnable == null) return;
        if (activeContext != null && width > 0 && height > 0) {
            int x1 = Math.round(x);
            int y1 = Math.round(y);
            int x2 = Math.round(x + width);
            int y2 = Math.round(y + height);
            try {
                activeContext.enableScissor(x1, y1, x2, y2);
                runnable.run();
            } finally {
                activeContext.disableScissor();
            }
        } else {
            runnable.run();
        }
    }
}
