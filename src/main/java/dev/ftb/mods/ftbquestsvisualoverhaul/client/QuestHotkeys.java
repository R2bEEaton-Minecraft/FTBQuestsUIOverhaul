package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashSet;
import java.util.Set;

public final class QuestHotkeys {
    private static final String KEY_CATEGORY = "key.categories.ftbquestsvisualoverhaul";
    private static final KeyMapping UNACCEPT_ALL_KEY = new KeyMapping(
            "key.ftbquestsvisualoverhaul.unaccept_all",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F7,
            KEY_CATEGORY
    );
    private static final KeyMapping TOGGLE_HIDE_ACCEPTED_KEY = new KeyMapping(
            "key.ftbquestsvisualoverhaul.toggle_hide_accepted",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            KEY_CATEGORY
    );
    private static final QuestActionRouter ACTION_ROUTER = new QuestActionRouter();

    private QuestHotkeys() {
    }

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(QuestHotkeys::registerKeyMappings);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(UNACCEPT_ALL_KEY);
        event.register(TOGGLE_HIDE_ACCEPTED_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !ClientQuestFile.exists()) {
            return;
        }

        while (UNACCEPT_ALL_KEY.consumeClick()) {
            unacceptAllAcceptedQuests();
        }

        while (TOGGLE_HIDE_ACCEPTED_KEY.consumeClick()) {
            toggleHideAcceptedQuests();
        }
    }

    private static void unacceptAllAcceptedQuests() {
        toggleQuestPins(explicitAcceptedQuestIds());
        QuestDataController.setLastAcceptedQuestId(0L);
        QuestDataController.setHiddenAcceptedQuestState(false, Set.of());
        QuestDataController.markDirty();
    }

    private static void toggleHideAcceptedQuests() {
        if (QuestDataController.isHideAcceptedQuests()) {
            restoreHiddenAcceptedQuests();
            return;
        }

        Set<Long> acceptedQuestIds = explicitAcceptedQuestIds();
        if (acceptedQuestIds.isEmpty()) {
            QuestDataController.setHiddenAcceptedQuestState(false, Set.of());
            return;
        }

        toggleQuestPins(acceptedQuestIds);
        QuestDataController.setHiddenAcceptedQuestState(true, acceptedQuestIds);
        QuestDataController.markDirty();
    }

    private static void restoreHiddenAcceptedQuests() {
        Set<Long> hiddenQuestIds = new LinkedHashSet<>(QuestDataController.getHiddenAcceptedQuestIds());
        if (hiddenQuestIds.isEmpty()) {
            QuestDataController.setHiddenAcceptedQuestState(false, Set.of());
            return;
        }

        Set<Long> currentlyAcceptedQuestIds = explicitAcceptedQuestIds();
        Set<Long> restorableQuestIds = new LinkedHashSet<>();
        for (Long questId : hiddenQuestIds) {
            Quest quest = ClientQuestFile.INSTANCE.getQuest(questId);
            if (quest != null && !currentlyAcceptedQuestIds.contains(questId)) {
                restorableQuestIds.add(questId);
            }
        }

        toggleQuestPins(restorableQuestIds);
        QuestDataController.setHiddenAcceptedQuestState(false, Set.of());
        QuestDataController.markDirty();
    }

    private static void toggleQuestPins(Set<Long> questIds) {
        for (Long questId : questIds) {
            Quest quest = ClientQuestFile.INSTANCE.getQuest(questId);
            if (quest != null) {
                ACTION_ROUTER.togglePin(quest);
            }
        }
    }

    private static Set<Long> explicitAcceptedQuestIds() {
        LongSet pinnedIds = ClientQuestFile.INSTANCE.selfTeamData.getPinnedQuestIds(Minecraft.getInstance().player);
        Set<Long> acceptedQuestIds = new LinkedHashSet<>();
        for (Long questId : pinnedIds) {
            if (questId != TeamData.AUTO_PIN_ID && ClientQuestFile.INSTANCE.getQuest(questId) != null) {
                acceptedQuestIds.add(questId);
            }
        }
        return acceptedQuestIds;
    }
}
