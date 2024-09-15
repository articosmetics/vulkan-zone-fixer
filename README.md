# Vulkan Zone Fixer

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.x-brightgreen.svg?style=flat-square)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Mod_Loader-Fabric-blue.svg?style=flat-square)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg?style=flat-square)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square)](https://adoptium.net/)

> **Vulkan Zone Fixer** is a Fabric bytecode modification mod designed to restore server world boundaries, custom zone effects, and resource pack GUIs when playing with [VulkanMod](https://github.com/xCollateral/VulkanMod).

---

## Problem Statement
When running Minecraft with the VulkanMod graphics backend, several standard OpenGL rendering assumptions break:
1. **World Border and Zone Rendering**: Vulkan's depth testing and stencil pipeline conflicts with vanilla barrier particles, regional forcefields, and server-side zone tint shaders.
2. **Resource Pack GUIs**: Custom GUI texture overlays (such as No Dark Inventory, custom bossbar icons, themed container frames) fail to blend correctly against Vulkan's swapchain buffers.
3. **HUD Element Occlusion**: Multi-instance status indicators and custom server overlays either flicker or render with incorrect blending modes.

---

## Architecture and Features

- **16+ Optimized Bytecode Mixins**: Hooks into VulkanMod's render pipeline, `ScreenMixin`, `DrawContextMixin`, and `GpuTextureMixin`.
- **Restored Forcefield and Zone Boundaries**: Re-enables vanilla and server-customized box barrier overlays without sacrificing Vulkan frame rates.
- **Resource Pack Frame Compatibility**: Resolves GUI darkness anomalies and texture clipping in custom inventories and shops.
- **Custom HUD and Bossbar Fix**: Smooth alpha-blended rendering for custom server health bars and regional alerts.
- **Zero Overhead**: Native execution without additional draw call batch bottlenecks.

---

## Building from Source

Requires **JDK 21** or later.

```bash
# Clone the repository
git clone https://github.com/articosmetics/vulkan-zone-fixer.git
cd vulkan-zone-fixer

# Build the Fabric mod jar
./gradlew build
```

The compiled mod will be generated in `build/libs/vulkanzonefixer-1.0.0.jar`.

---

## Installation

1. Install **Fabric Loader** (version 0.16.x or newer) for Minecraft 1.21+.
2. Ensure **Fabric API** and **VulkanMod** are placed in your `.minecraft/mods` directory.
3. Drop `vulkanzonefixer-1.0.0.jar` into your `.minecraft/mods` folder.
4. Launch the game.

---

## License

Author: **Wnderin** ([@articosmetics](https://github.com/articosmetics))  
Licensed under the [MIT License](LICENSE).
