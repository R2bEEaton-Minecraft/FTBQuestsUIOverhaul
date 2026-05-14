package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClientEventHandler;
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

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
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
    private static FTBQuestsClientEventHandler pinnedQuestHudHandler;
    private static Field pinnedQuestTextField;

    private QuestHotkeys() {
    }

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(QuestHotkeys::registerKeyMappings);
        ClientTickEvent.CLIENT_POST.register(client -> hidePinnedQuestHudIfNeeded());
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

        QuestUiFeedback.onClientTick();
    }

    private static void unacceptAllAcceptedQuests() {
        toggleQuestPins(explicitAcceptedQuestIds());
        QuestDataController.setLastAcceptedQuestId(0L);
        QuestDataController.setHiddenAcceptedQuestState(false, Set.of());
        QuestDataController.markDirty();
    }

    private static void toggleHideAcceptedQuests() {
        boolean hideAccepted = !QuestDataController.isHideAcceptedQuests();
        QuestDataController.setHiddenAcceptedQuestState(hideAccepted, Set.of());
        QuestDataController.markDirty();
        if (hideAccepted) {
            clearPinnedQuestHudText();
        }
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

    private static void hidePinnedQuestHudIfNeeded() {
        if (QuestDataController.isHideAcceptedQuests()) {
            clearPinnedQuestHudText();
        }
    }

    @SuppressWarnings("unchecked")
    private static void clearPinnedQuestHudText() {
        if (!resolvePinnedQuestHudHandler()) {
            return;
        }

        try {
            List<Object> pinnedQuestText = (List<Object>) pinnedQuestTextField.get(pinnedQuestHudHandler);
            pinnedQuestText.clear();
        } catch (IllegalAccessException ignored) {
        }
    }

    private static boolean resolvePinnedQuestHudHandler() {
        if (pinnedQuestHudHandler != null && pinnedQuestTextField != null) {
            return true;
        }

        try {
            Field listenersField = ClientGuiEvent.RENDER_HUD.getClass().getDeclaredField("listeners");
            listenersField.setAccessible(true);
            Object listenersValue = listenersField.get(ClientGuiEvent.RENDER_HUD);
            if (!(listenersValue instanceof Iterable<?> listeners)) {
                return false;
            }

            for (Object listener : listeners) {
                FTBQuestsClientEventHandler handler = extractPinnedQuestHudHandler(listener);
                if (handler != null) {
                    Field field = FTBQuestsClientEventHandler.class.getDeclaredField("pinnedQuestText");
                    field.setAccessible(true);
                    pinnedQuestHudHandler = handler;
                    pinnedQuestTextField = field;
                    return true;
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }

        return false;
    }

    private static FTBQuestsClientEventHandler extractPinnedQuestHudHandler(Object listener) {
        if (listener instanceof FTBQuestsClientEventHandler handler) {
            return handler;
        }

        for (Field field : listener.getClass().getDeclaredFields()) {
            if (!FTBQuestsClientEventHandler.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(listener);
                if (value instanceof FTBQuestsClientEventHandler handler) {
                    return handler;
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        return null;
    }
}
