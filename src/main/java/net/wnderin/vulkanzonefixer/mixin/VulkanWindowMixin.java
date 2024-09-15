package net.wnderin.vulkanzonefixer.mixin;

import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(value = Window.class, priority = 2500)
public abstract class VulkanWindowMixin {

    @Shadow @Final private long handle;
    @Shadow private boolean fullscreen;
    @Shadow private int windowedX;
    @Shadow private int windowedY;
    @Shadow private int windowedWidth;
    @Shadow private int windowedHeight;
    @Shadow private int x;
    @Shadow private int y;
    @Shadow private int width;
    @Shadow private int height;
    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;
    @Shadow @Final private MonitorTracker monitorTracker;
    @Shadow private boolean currentFullscreen;

    @Shadow public abstract Monitor getMonitor();

    @Inject(method = "updateWindowRegion", at = @At("HEAD"), cancellable = true)
    private void vulkanzonefixer$updateWindowRegion(CallbackInfo ci) {
        int targetMode = 1; // 0 = WINDOWED, 1 = WINDOWED_FULLSCREEN, 2 = EXCLUSIVE_FULLSCREEN
        Object vulkanConfig = null;

        try {
            Class<?> initClass = Class.forName("net.vulkanmod.Initializer");
            Field configField = initClass.getField("CONFIG");
            vulkanConfig = configField.get(null);
            if (vulkanConfig != null) {
                Field wmField = vulkanConfig.getClass().getField("windowMode");
                targetMode = wmField.getInt(vulkanConfig);
            }
        } catch (Throwable ignored) {}

        // Check if Cubes Without Borders has a preferred mode
        try {
            Class<?> cwbConfigClass = Class.forName("dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig");
            Method getInstance = cwbConfigClass.getMethod("getInstance");
            Object cwbConfig = getInstance.invoke(null);
            if (cwbConfig != null) {
                Method getMode = cwbConfigClass.getMethod("getFullscreenMode");
                Object mode = getMode.invoke(cwbConfig);
                if (mode != null) {
                    String name = mode.toString();
                    if ("BORDERLESS".equalsIgnoreCase(name)) {
                        targetMode = 1;
                    } else if ("ON".equalsIgnoreCase(name)) {
                        targetMode = 2;
                    } else if ("OFF".equalsIgnoreCase(name)) {
                        targetMode = 0;
                    }
                }
            }
        } catch (Throwable ignored) {}

        if (!this.fullscreen) {
            // Restore windowed mode
            GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

            int restoreW = this.windowedWidth > 0 ? this.windowedWidth : 1280;
            int restoreH = this.windowedHeight > 0 ? this.windowedHeight : 720;
            int restoreX = this.windowedX;
            int restoreY = this.windowedY;

            this.x = restoreX;
            this.y = restoreY;
            this.width = restoreW;
            this.height = restoreH;

            GLFW.glfwSetWindowMonitor(this.handle, 0L, this.x, this.y, this.width, this.height, GLFW.GLFW_DONT_CARE);

            int[] fbW = new int[1], fbH = new int[1];
            GLFW.glfwGetFramebufferSize(this.handle, fbW, fbH);
            this.framebufferWidth = fbW[0] > 0 ? fbW[0] : this.width;
            this.framebufferHeight = fbH[0] > 0 ? fbH[0] : this.height;

            this.currentFullscreen = false;

            if (vulkanConfig != null) {
                try {
                    vulkanConfig.getClass().getField("windowMode").setInt(vulkanConfig, 0);
                } catch (Throwable ignored) {}
            }

            vulkanzonefixer$scheduleSwapChainUpdate();
            ci.cancel();
            return;
        }

        // Entering Fullscreen (this.fullscreen == true)
        // If user explicitly chose Exclusive Fullscreen (2), let VulkanMod handle it
        if (targetMode == 2) {
            return;
        }

        // True Borderless Windowed Fullscreen (Mode 1)
        Monitor monitor = vulkanzonefixer$findBestMonitor();
        int monX = 0;
        int monY = 0;
        int monW = 1920;
        int monH = 1080;

        if (monitor != null) {
            monX = monitor.getViewportX();
            monY = monitor.getViewportY();
            VideoMode vm = monitor.getCurrentVideoMode();
            if (vm != null) {
                monW = vm.getWidth();
                monH = vm.getHeight();
            }
        } else {
            long primary = GLFW.glfwGetPrimaryMonitor();
            if (primary != 0L) {
                int[] mx = new int[1], my = new int[1];
                GLFW.glfwGetMonitorPos(primary, mx, my);
                monX = mx[0];
                monY = my[0];
                GLFWVidMode vm = GLFW.glfwGetVideoMode(primary);
                if (vm != null) {
                    monW = vm.width();
                    monH = vm.height();
                }
            }
        }

        // Save windowed bounds before going fullscreen
        if (!this.currentFullscreen) {
            if (this.width != monW || this.height != monH || this.x != monX || this.y != monY) {
                this.windowedX = this.x;
                this.windowedY = this.y;
                this.windowedWidth = this.width;
                this.windowedHeight = this.height;
            }
        }

        // Strip decorations and disable resize border
        GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
        GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        // Position window across the target monitor at native resolution (0L = Windowed mode)
        GLFW.glfwSetWindowMonitor(this.handle, 0L, monX, monY, monW, monH, GLFW.GLFW_DONT_CARE);

        this.x = monX;
        this.y = monY;
        this.width = monW;
        this.height = monH;

        int[] fbW = new int[1], fbH = new int[1];
        GLFW.glfwGetFramebufferSize(this.handle, fbW, fbH);
        this.framebufferWidth = fbW[0] > 0 ? fbW[0] : monW;
        this.framebufferHeight = fbH[0] > 0 ? fbH[0] : monH;

        this.currentFullscreen = true;

        // Preserve VulkanMod windowMode as WINDOWED_FULLSCREEN (1)
        if (vulkanConfig != null) {
            try {
                vulkanConfig.getClass().getField("windowMode").setInt(vulkanConfig, 1);
            } catch (Throwable ignored) {}
        }

        vulkanzonefixer$scheduleSwapChainUpdate();
        ci.cancel();
    }

    @Inject(method = "toggleFullscreen", at = @At("HEAD"), cancellable = true)
    private void vulkanzonefixer$toggleFullscreen(CallbackInfo ci) {
        this.fullscreen = !this.fullscreen;
        try {
            Class<?> optionsClass = Class.forName("net.vulkanmod.config.option.Options");
            optionsClass.getField("fullscreenDirty").setBoolean(null, true);

            Class<?> initClass = Class.forName("net.vulkanmod.Initializer");
            Field configField = initClass.getField("CONFIG");
            Object config = configField.get(null);
            if (config != null) {
                Field wmField = config.getClass().getField("windowMode");
                int mode = wmField.getInt(config);
                if (this.fullscreen) {
                    if (mode != 2) {
                        wmField.setInt(config, 1); // WINDOWED_FULLSCREEN
                    }
                } else {
                    wmField.setInt(config, 0); // WINDOWED
                }
            }
        } catch (Throwable ignored) {}
        ci.cancel();
    }

    private Monitor vulkanzonefixer$findBestMonitor() {
        // 1. Determine monitor based on window coordinates and overlap area
        try {
            PointerBuffer monitors = GLFW.glfwGetMonitors();
            if (monitors != null && monitors.hasRemaining()) {
                long bestMon = 0;
                long maxOverlap = -1;

                int winX = this.x;
                int winY = this.y;
                int winW = this.width > 0 ? this.width : 1280;
                int winH = this.height > 0 ? this.height : 720;

                for (int i = 0; i < monitors.limit(); i++) {
                    long mHandle = monitors.get(i);
                    int[] mx = new int[1], my = new int[1];
                    GLFW.glfwGetMonitorPos(mHandle, mx, my);
                    GLFWVidMode vm = GLFW.glfwGetVideoMode(mHandle);
                    if (vm != null) {
                        int ox = Math.max(0, Math.min(winX + winW, mx[0] + vm.width()) - Math.max(winX, mx[0]));
                        int oy = Math.max(0, Math.min(winY + winH, my[0] + vm.height()) - Math.max(winY, my[0]));
                        long area = (long) ox * oy;
                        if (area > maxOverlap) {
                            maxOverlap = area;
                            bestMon = mHandle;
                        }
                    }
                }
                if (bestMon != 0 && this.monitorTracker != null) {
                    Monitor m = this.monitorTracker.getMonitor(bestMon);
                    if (m != null) return m;
                }
            }
        } catch (Throwable ignored) {}

        // 2. Try Minecraft's getMonitor()
        try {
            Monitor m = this.getMonitor();
            if (m != null && m.getCurrentVideoMode() != null) {
                return m;
            }
        } catch (Throwable ignored) {}

        // 3. Fallback to primary monitor
        try {
            long primary = GLFW.glfwGetPrimaryMonitor();
            if (primary != 0 && this.monitorTracker != null) {
                Monitor m = this.monitorTracker.getMonitor(primary);
                if (m != null) return m;
            }
        } catch (Throwable ignored) {}

        return null;
    }

    private static void vulkanzonefixer$scheduleSwapChainUpdate() {
        try {
            Class<?> renderer = Class.forName("net.vulkanmod.vulkan.Renderer");
            renderer.getMethod("scheduleSwapChainUpdate").invoke(null);
        } catch (Throwable ignored) {}
    }
}
