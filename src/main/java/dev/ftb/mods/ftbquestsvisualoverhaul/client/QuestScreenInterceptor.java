package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModClientConfig;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.screen.OverhaulQuestScreen;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestOpenContext;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestViewState;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class QuestScreenInterceptor {
    public static boolean openOverhaulForQuest(Quest quest) {
        if (!ModClientConfig.REPLACE_FTBQUESTS_SCREEN.get() || !ClientQuestFile.exists()) {
            return false;
        }

        QuestViewState state = QuestDataController.copyPersistedViewState();
        if (state.isDefaultFtbUiMode() && isCreativeOrEditorView()) {
            return false;
        }

        Minecraft.getInstance().setScreen(new OverhaulQuestScreen(QuestOpenContext.fromDirectRequest(quest, state)));
        return true;
    }

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

        QuestViewState state = QuestDataController.copyPersistedViewState();
        if (state.isDefaultFtbUiMode() && isCreativeOrEditorView()) {
            return;
        }

        QuestOpenContext context = QuestOpenContext.fromVanilla(questScreen, state);
        event.setNewScreen(new OverhaulQuestScreen(context));
    }

    private static boolean isCreativeOrEditorView() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() != GameType.SURVIVAL;
    }
}
