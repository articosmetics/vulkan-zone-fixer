package net.wnderin.vulkanzonefixer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class NoDarkInventoryTracker {
    private static volatile boolean darkBackgroundDisabled = false;
    private static volatile Set<String> lastKnownPacks = null;
    private static long lastCheckTime = 0L;

    public static boolean isDarkBackgroundDisabled() {
        long now = System.currentTimeMillis();
        // Periodically verify if active resource packs have changed
        if (now - lastCheckTime > 500L) {
            lastCheckTime = now;
            refreshState();
        }
        return darkBackgroundDisabled;
    }

    public static synchronized void refreshState() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return;
        }

        Collection<String> enabledPacks = null;
        try {
            if (client.getResourcePackManager() != null) {
                enabledPacks = client.getResourcePackManager().getEnabledIds();
            }
        } catch (Throwable ignored) {}

        if (enabledPacks != null && enabledPacks.equals(lastKnownPacks)) {
            return;
        }

        if (enabledPacks != null) {
            lastKnownPacks = new HashSet<>(enabledPacks);
        }

        darkBackgroundDisabled = detectNoDarkInventory(client, enabledPacks);
    }

    private static boolean detectNoDarkInventory(MinecraftClient client, Collection<String> enabledPacks) {
        // 1. Check enabled pack IDs and metadata
        try {
            if (client.getResourcePackManager() != null) {
                for (ResourcePackProfile profile : client.getResourcePackManager().getEnabledProfiles()) {
                    if (matchesPackText(profile.getId())
                            || (profile.getDisplayName() != null && matchesPackText(profile.getDisplayName().getString()))
                            || (profile.getDescription() != null && matchesPackDescription(profile.getDescription().getString()))) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // Fallback name check if getEnabledProfiles failed
        if (enabledPacks != null) {
            for (String packName : enabledPacks) {
                if (matchesPackText(packName)) {
                    return true;
                }
            }
        }

        // 2. Check active shader resources provided by resource packs
        try {
            ResourceManager resourceManager = client.getResourceManager();
            if (resourceManager != null) {
                if (checkShaderResource(resourceManager, "shaders/core/gui.vsh")
                        || checkShaderResource(resourceManager, "shaders/core/rendertype_gui.vsh")) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private static boolean matchesPackText(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return (lower.contains("dark") && lower.contains("inv"))
                || (lower.contains("dark") && lower.contains("gui"))
                || (lower.contains("no") && lower.contains("dark"))
                || lower.contains("clear gui")
                || lower.contains("cleargui");
    }

    private static boolean matchesPackDescription(String desc) {
        if (desc == null) return false;
        String lower = desc.toLowerCase();
        return lower.contains("clear") && (lower.contains("gui") || lower.contains("background") || lower.contains("inventory"));
    }

    private static boolean checkShaderResource(ResourceManager rm, String path) {
        try {
            Identifier id = Identifier.of("minecraft", path);
            Optional<Resource> res = rm.getResource(id);
            if (res.isPresent()) {
                try (BufferedReader reader = res.get().getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("vertexColor.a = 0")
                                || (line.contains("0.0627451") && line.contains("vertexColor.a"))) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean isDimmingColor(int colorStart, int colorEnd) {
        if (colorStart == -1072689136 && colorEnd == -804253680) {
            return true;
        }
        int rgb1 = colorStart & 0x00FFFFFF;
        int rgb2 = colorEnd & 0x00FFFFFF;
        return rgb1 == 0x00101010 && rgb2 == 0x00101010;
    }

    private NoDarkInventoryTracker() {}
}
