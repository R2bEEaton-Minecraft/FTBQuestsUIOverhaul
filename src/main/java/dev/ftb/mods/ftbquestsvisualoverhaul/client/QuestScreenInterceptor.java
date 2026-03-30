package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModClientConfig;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.screen.OverhaulQuestScreen;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestOpenContext;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class QuestScreenInterceptor {
    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!ModClientConfig.REPLACE_FTBQUESTS_SCREEN.get()) {
            return;
        }
        if (!(event.getNewScreen() instanceof ScreenWrapper wrapper) || !(wrapper.getGui() instanceof QuestScreen questScreen) || !ClientQuestFile.exists()) {
            return;
        }
        if (VanillaFallbackBridge.consumeBypassFlag()) {
            return;
        }

        QuestOpenContext context = QuestOpenContext.fromVanilla(questScreen, QuestDataController.copyPersistedViewState());
        event.setNewScreen(new OverhaulQuestScreen(context));
    }
}
