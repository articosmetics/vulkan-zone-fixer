package net.wnderin.vulkanzonefixer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

public class VulkanZoneFixer implements ClientModInitializer {
    public static final String MOD_ID = "vulkanzonefixer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean vulkanModPresent = false;
    private static KeyBinding configKeyBinding;

    @Override
    public void onInitializeClient() {
        try {
            KeyBinding kb = null;
            for (java.lang.reflect.Constructor<?> c : KeyBinding.class.getConstructors()) {
                Class<?>[] params = c.getParameterTypes();
                if (params.length == 4 && params[0] == String.class && params[2] == int.class) {
                    if (params[3] == String.class) {
                        kb = (KeyBinding) c.newInstance("key.vulkanzonefixer.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.vulkanzonefixer");
                        break;
                    } else {
                        Object category = null;
                        try {
                            category = params[3].getField("field_62556").get(null);
                        } catch (Throwable t) {
                            try {
                                category = params[3].getField("MISC").get(null);
                            } catch (Throwable ignored) {}
                        }
                        if (category != null) {
                            kb = (KeyBinding) c.newInstance("key.vulkanzonefixer.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, category);
                            break;
                        }
                    }
                }
            }
            if (kb != null) {
                configKeyBinding = KeyBindingHelper.registerKeyBinding(kb);
                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                    while (configKeyBinding != null && configKeyBinding.wasPressed()) {
                        try {
                            Class<?> screenClass = Class.forName("bramcraft.net.mcroyale_essentials.client.gui.McroyaleEssentialsConfigScreen");
                            Screen screen = (Screen) screenClass.getConstructor(Screen.class).newInstance(client.currentScreen);
                            client.setScreen(screen);
                        } catch (Throwable t) {
                            LOGGER.error("[VulkanZoneFixer] Failed to open McroyaleEssentialsConfigScreen: ", t);
                        }
                    }
                });
            }
        } catch (Throwable t) {
            LOGGER.warn("[VulkanZoneFixer] Could not register config keybinding: {}", t.getMessage());
        }

        vulkanModPresent = FabricLoader.getInstance().isModLoaded("vulkanmod");
        if (vulkanModPresent) {
            LOGGER.info("[VulkanZoneFixer] VulkanMod active: universal zone and world border fallback renderer armed.");
            try {
                Class<?> initClass = Class.forName("net.vulkanmod.Initializer");
                java.lang.reflect.Field configField = initClass.getField("CONFIG");
                Object config = configField.get(null);
                if (config != null) {
                    java.lang.reflect.Field ecField = config.getClass().getField("entityCulling");
                    ecField.setBoolean(config, false);
                    LOGGER.info("[VulkanZoneFixer] Disabled VulkanMod chunk-based entity culling to ensure server border/storm display entities render properly.");
                }
            } catch (Throwable t) {
                LOGGER.debug("[VulkanZoneFixer] Failed to reflectively disable VulkanMod entity culling: {}", t.getMessage());
            }

            try {
                Class<?> shaderLoadUtil = Class.forName("net.vulkanmod.render.shader.ShaderLoadUtil");
                java.lang.reflect.Field remappedField = shaderLoadUtil.getField("REMAPPED_SHADERS");
                @SuppressWarnings("unchecked")
                Set<String> set = (Set<String>) remappedField.get(null);
                if (set != null) {
                    set.remove("core/rendertype_item_entity_translucent_cull.vsh");
                    set.remove("core/rendertype_entity_translucent_cull.vsh");
                    set.remove("core/rendertype_item_entity_translucent_cull.fsh");
                    set.remove("core/rendertype_entity_translucent_cull.fsh");
                    LOGGER.info("[VulkanZoneFixer] Unmapped translucent item/entity shaders from VulkanMod overrides.");
                }
            } catch (Throwable t) {
                LOGGER.warn("[VulkanZoneFixer] Could not unmap translucent shaders: {}", t.getMessage());
            }
        }

        if (FabricLoader.getInstance().isModLoaded("simpleblockoverlay")) {
            try {
                Class<?> configClass = Class.forName("dev.microcontrollers.simpleblockoverlay.config.SimpleBlockOverlayConfig");
                Object handler = configClass.getField("CONFIG").get(null);
                java.lang.reflect.Method instanceMethod = handler.getClass().getMethod("instance");
                Object configInstance = instanceMethod.invoke(handler);
                java.lang.reflect.Field customRendererField = configClass.getField("customRenderer");
                customRendererField.setBoolean(configInstance, false);
                LOGGER.info("[VulkanZoneFixer] Enforced SimpleBlockOverlay customRenderer = false for VulkanMod compatibility.");
            } catch (Throwable t) {
                LOGGER.debug("[VulkanZoneFixer] Could not adjust SimpleBlockOverlay config: {}", t.getMessage());
            }
        }
        LOGGER.info("[VulkanZoneFixer] Initialized.");
    }

    public static void clearVulkanPipelineCache() {
        if (!vulkanModPresent) return;
        try {
            Class<?> comRenderSystem = Class.forName("com.mojang.blaze3d.systems.RenderSystem");
            Object device = comRenderSystem.getMethod("getDevice").invoke(null);
            if (device != null && device.getClass().getName().contains("VkGpuDevice")) {
                device.getClass().getMethod("clearPipelineCache").invoke(device);
                LOGGER.info("[VulkanZoneFixer] Cleared VulkanMod pipeline cache to recompile shaders with updated resource packs.");
            }
        } catch (Throwable t) {
            LOGGER.debug("[VulkanZoneFixer] Could not clear VulkanMod pipeline cache: {}", t.getMessage());
        }
    }

    public static boolean isVulkanModPresent() {
        return vulkanModPresent;
    }
}
