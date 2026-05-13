package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.ftb.mods.ftbquests.quest.task.TaskTypes;
import dev.ftb.mods.ftbquestsvisualoverhaul.FTBQuestsVisualOverhaul;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public final class FtbQuestTypeIconOverrides {
    private static final Icon ADVANCEMENT_FALLBACK_ICON = Icon.getIcon("minecraft:item/filled_map");
    private static final String ICON_SUPPLIER_FIELD = "iconSupplier";

    private FtbQuestTypeIconOverrides() {
    }

    public static void apply() {
        overrideTaskIcon(TaskTypes.ADVANCEMENT, () -> ADVANCEMENT_FALLBACK_ICON);
        overrideRewardIcon(RewardTypes.ADVANCEMENT, () -> ADVANCEMENT_FALLBACK_ICON);
    }

    private static void overrideTaskIcon(TaskType type, Supplier<Icon> iconSupplier) {
        overrideIconSupplier(type, iconSupplier, "task");
    }

    private static void overrideRewardIcon(RewardType type, Supplier<Icon> iconSupplier) {
        overrideIconSupplier(type, iconSupplier, "reward");
    }

    private static void overrideIconSupplier(Object type, Supplier<Icon> iconSupplier, String label) {
        try {
            Field field = type.getClass().getDeclaredField(ICON_SUPPLIER_FIELD);
            field.setAccessible(true);
            field.set(type, iconSupplier);
        } catch (ReflectiveOperationException e) {
            FTBQuestsVisualOverhaul.LOGGER.warn("Failed to override FTB Quests {} advancement fallback icon", label, e);
        }
    }
}
