package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModClientConfig;

public class VanillaFallbackBridge {
    private static boolean bypassNextQuestScreen;

    private VanillaFallbackBridge() {
    }

    public static boolean consumeBypassFlag() {
        if (bypassNextQuestScreen) {
            bypassNextQuestScreen = false;
            return true;
        }
        return false;
    }

    public static boolean canFallback() {
        return ModClientConfig.ALLOW_VANILLA_FALLBACK.get();
    }

    public static void openVanillaRoot() {
        if (!canFallback() || !ClientQuestFile.exists()) {
            return;
        }
        bypassNextQuestScreen = true;
        ClientQuestFile.openGui();
    }

    public static void openVanillaForQuest(Quest quest) {
        if (!canFallback() || quest == null || !ClientQuestFile.exists()) {
            return;
        }
        bypassNextQuestScreen = true;
        ClientQuestFile.openGui(quest, true);
    }
}
