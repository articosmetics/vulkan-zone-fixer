package net.wnderin.vulkanzonefixer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class CustomHudFixer {

    public static class TeammateCard {
        public String name = "";
        public int totems = -1;
        public float health = 1.0f;
        public boolean active = false;
        public long lastUpdated = 0L;
    }

    private static final TeammateCard[] cards = new TeammateCard[4];

    static {
        for (int i = 0; i < cards.length; i++) {
            cards[i] = new TeammateCard();
        }
    }

    private CustomHudFixer() {}

    public static boolean isBrokenHudText(Text text) {
        if (text == null) return false;
        String str = text.getString();
        if (str == null || str.isEmpty()) return false;

        boolean found = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c >= '\uf410' && c <= '\uf417')   // hud_panels (background black bars)
                    || (c >= '\uf210' && c <= '\uf40f') // hud_cells (player face blocks)
                    || (c >= '\uf418' && c <= '\uf41f') // hud_bars (health widgets)
                    || c == '\ue040') {                 // totem icon
                found = true;
                break;
            }
        }

        if (found) {
            parseHudText(text);
            return true;
        }

        return false;
    }

    public static synchronized void parseHudText(Text text) {
        if (text == null) return;
        long now = System.currentTimeMillis();

        StringBuilder[] names = new StringBuilder[4];
        int[] totems = new int[]{-1, -1, -1, -1};
        float[] health = new float[]{-1f, -1f, -1f, -1f};
        boolean[] active = new boolean[4];

        for (int i = 0; i < 4; i++) {
            names[i] = new StringBuilder();
        }

        try {
            text.visit((style, part) -> {
                if (style != null && style.getColor() != null) {
                    TextColor col = style.getColor();
                    int rgb = col.getRgb();
                    int r = (rgb >> 16) & 0xFF;
                    int g = (rgb >> 8) & 0xFF;
                    int b = rgb & 0xFF;
                    int slot = b & 7;

                    if (slot >= 0 && slot < 4) {
                        active[slot] = true;

                        if (r == 70) {
                            if (g == 70) {
                                // Teammate username
                                for (int i = 0; i < part.length(); i++) {
                                    char c = part.charAt(i);
                                    if (c < '\ue000' && c > ' ') {
                                        names[slot].append(c);
                                    }
                                }
                            } else if (g == 71) {
                                // Totem count text
                                String s = part.trim();
                                if (!s.isEmpty()) {
                                    try {
                                        totems[slot] = Integer.parseInt(s);
                                    } catch (Throwable ignored) {}
                                }
                            }
                        } else if (r == 75 && g == 0) {
                            active[slot] = true;
                        }
                    }

                    if (part != null && !part.isEmpty()) {
                        char c = part.charAt(0);
                        if (c >= '\uf418' && c <= '\uf41f') {
                            int barSlot = c - '\uf418';
                            if (barSlot >= 0 && barSlot < 4) {
                                health[barSlot] = ((float) (r >> 3)) / 31.0f;
                                active[barSlot] = true;
                            }
                        }
                    }
                }
                return Optional.empty();
            }, Style.EMPTY);
        } catch (Throwable ignored) {}

        for (int i = 0; i < 4; i++) {
            if (active[i]) {
                cards[i].active = true;
                cards[i].lastUpdated = now;
                String n = names[i].toString().trim();
                if (!n.isEmpty()) {
                    cards[i].name = n;
                }
                if (totems[i] >= 0) {
                    cards[i].totems = totems[i];
                }
                if (health[i] >= 0f) {
                    cards[i].health = health[i];
                }
            }
        }
    }

    public static final net.minecraft.util.Identifier TOTEM_TEXTURE =
            net.minecraft.util.Identifier.of("vulkanzonefixer", "textures/gui/totem.png");

    public static void render(DrawContext context) {
        if (context == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;
        if (client.options.hudHidden) return;

        long now = System.currentTimeMillis();
        int activeCount = 0;
        for (TeammateCard c : cards) {
            if (c.active && (now - c.lastUpdated < 6000L)) {
                activeCount++;
            }
        }

        if (activeCount == 0) return;

        TextRenderer textRenderer = client.textRenderer;
        if (textRenderer == null) return;

        int startX = 6;
        int startY = 6;

        int slotIdx = 0;
        for (int i = 0; i < 4; i++) {
            TeammateCard card = cards[i];
            if (!card.active || (now - card.lastUpdated >= 6000L)) continue;

            int cardX = startX;
            int cardY = startY + slotIdx * 40;

            // 1. Slate blue-gray semi-transparent card background (exact match to screenshot)
            context.fill(cardX, cardY, cardX + 152, cardY + 36, 0xD0384353);
            // 1px subtle dark outline
            context.fill(cardX, cardY, cardX + 152, cardY + 1, 0xFF1E242C);
            context.fill(cardX, cardY + 35, cardX + 152, cardY + 36, 0xFF1E242C);
            context.fill(cardX, cardY, cardX + 1, cardY + 36, 0xFF1E242C);
            context.fill(cardX + 151, cardY, cardX + 152, cardY + 36, 0xFF1E242C);

            // Resolve name if empty from scoreboard
            String name = card.name;
            if (name == null || name.isEmpty()) {
                try {
                    if (client.player.getScoreboardTeam() != null) {
                        Collection<String> members = client.player.getScoreboardTeam().getPlayerList();
                        int idx = 0;
                        for (String m : members) {
                            if (!m.equalsIgnoreCase(client.player.getNameForScoreboard())) {
                                if (idx == slotIdx) {
                                    name = m;
                                    card.name = m;
                                    break;
                                }
                                idx++;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }

            // 2. Player Skin Face (32x32)
            SkinTextures skin = null;
            if (name != null && !name.isEmpty() && client.getNetworkHandler() != null) {
                try {
                    PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(name);
                    if (entry != null) {
                        skin = entry.getSkinTextures();
                    }
                } catch (Throwable ignored) {}
            }

            if (skin == null) {
                try {
                    UUID id = name != null && !name.isEmpty()
                            ? UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8))
                            : UUID.randomUUID();
                    skin = DefaultSkinHelper.getSkinTextures(id);
                } catch (Throwable ignored) {}
            }

            if (skin != null) {
                try {
                    PlayerSkinDrawer.draw(context, skin, cardX + 2, cardY + 2, 32);
                } catch (Throwable ignored) {}
            }

            // 3. Player Name Text
            String displayName = (name != null && !name.isEmpty()) ? name : ("Teammate " + (i + 1));
            if (displayName.length() > 9) {
                displayName = displayName.substring(0, 8) + "…";
            }
            context.drawTextWithShadow(textRenderer, displayName, cardX + 38, cardY + 5, 0xFFFFFFFF);

            // 4. Health Bar (width = 64, height = 6)
            int barX = cardX + 38;
            int barY = cardY + 18;
            int barW = 64;
            int barH = 6;
            // Bar background
            context.fill(barX, barY, barX + barW, barY + barH, 0xFF222428);
            // Health fill
            float hp = Math.max(0.0f, Math.min(1.0f, card.health));
            int fillW = (int) (barW * hp);
            int barColor = hp <= 0.33f ? 0xFFE63333 : (hp <= 0.66f ? 0xFFFFB300 : 0xFF4ADE80);
            if (fillW > 0) {
                context.fill(barX, barY, barX + fillW, barY + barH, barColor);
            }

            // 5. Totem Icon
            int totemX = cardX + 114;
            int totemY = cardY + 12;
            boolean drewTotem = false;
            try {
                if (client.getResourceManager().getResource(TOTEM_TEXTURE).isPresent()) {
                    context.drawTexture(TOTEM_TEXTURE, totemX, totemY, 0.0f, 0.0f, 12, 14, 16, 16);
                    drewTotem = true;
                }
            } catch (Throwable ignored) {}

            if (!drewTotem) {
                try {
                    context.drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), totemX - 2, totemY - 2);
                } catch (Throwable ignored) {}
            }

            // Totem count text: "x9" in gold
            String totStr = "x" + Math.max(0, card.totems >= 0 ? card.totems : 0);
            context.drawTextWithShadow(textRenderer, totStr, cardX + 128, cardY + 15, 0xFFFFD233);

            slotIdx++;
        }
    }
}
