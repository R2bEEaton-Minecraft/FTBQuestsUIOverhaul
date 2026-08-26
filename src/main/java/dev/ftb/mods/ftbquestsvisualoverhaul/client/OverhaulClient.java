package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.events.ClearFileCacheEvent;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;

public class OverhaulClient {
    private static boolean initialized;

    private OverhaulClient() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, lastScreen) -> new ModConfigScreen(lastScreen)));

        FtbQuestTypeIconOverrides.apply();
        QuestHotkeys.init();
        MinecraftForge.EVENT_BUS.register(QuestScreenInterceptor.class);
        MinecraftForge.EVENT_BUS.register(QuestLauncherButtonInjector.class);
        MinecraftForge.EVENT_BUS.register(CleanUiModeOverlay.class);
        MinecraftForge.EVENT_BUS.register(QuestHotkeys.class);
        ClearFileCacheEvent.EVENT.register(file -> QuestDataController.markDirty());
    }
}
