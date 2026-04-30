package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import net.minecraftforge.common.MinecraftForge;

public class OverhaulClient {
    private static boolean initialized;

    private OverhaulClient() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        QuestHotkeys.init();
        MinecraftForge.EVENT_BUS.register(QuestScreenInterceptor.class);
        MinecraftForge.EVENT_BUS.register(QuestLauncherButtonInjector.class);
        MinecraftForge.EVENT_BUS.register(CleanUiModeOverlay.class);
        MinecraftForge.EVENT_BUS.register(QuestHotkeys.class);
        ClearFileCacheEvent.EVENT.register(file -> QuestDataController.markDirty());
    }
}
