package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.ui.misc.SimpleToast;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.ToastQuestObject;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestUiFeedback {
    private static final long MANUAL_COMPLETION_TOAST_SUPPRESSION_MS = 5000L;
    private static final Map<Long, Long> MANUALLY_HANDLED_COMPLETION_TOASTS = new LinkedHashMap<>();
    private static Field toastVisibleField;
    private static Field toastQueuedField;
    private static Method toastInstanceGetToastMethod;
    private static Field toastQuestObjectField;

    private QuestUiFeedback() {
    }

    public static void playRewardConfirmSound() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.15F));
        }
    }

    public static void playUiClickSound() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F));
        }
    }

    public static void playQuestCompletionToast(Quest quest) {
        if (quest == null) {
            return;
        }

        MANUALLY_HANDLED_COMPLETION_TOASTS.put(quest.getId(), System.currentTimeMillis() + MANUAL_COMPLETION_TOAST_SUPPRESSION_MS);
        Minecraft.getInstance().getToasts().addToast(new ManualQuestCompletionToast(quest));
    }

    public static void onClientTick() {
        long now = System.currentTimeMillis();
        MANUALLY_HANDLED_COMPLETION_TOASTS.entrySet().removeIf(entry -> entry.getValue() < now);
        suppressAutomaticQuestCompletionToasts();
    }

    @SuppressWarnings("unchecked")
    private static void suppressAutomaticQuestCompletionToasts() {
        if (!resolveToastReflection()) {
            return;
        }

        ToastComponent toastComponent = Minecraft.getInstance().getToasts();
        try {
            Deque<Toast> queued = (Deque<Toast>) toastQueuedField.get(toastComponent);
            queued.removeIf(QuestUiFeedback::shouldSuppressToast);

            List<Object> visible = (List<Object>) toastVisibleField.get(toastComponent);
            Iterator<Object> iterator = visible.iterator();
            while (iterator.hasNext()) {
                Toast toast = extractToast(iterator.next());
                if (toast != null && shouldSuppressToast(toast)) {
                    iterator.remove();
                }
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static boolean shouldSuppressToast(Toast toast) {
        if (!(toast instanceof ToastQuestObject)) {
            return false;
        }

        QuestObject object = extractQuestObject(toast);
        if (!(object instanceof Quest quest)) {
            return false;
        }

        long questId = quest.getId();
        if (MANUALLY_HANDLED_COMPLETION_TOASTS.containsKey(questId)) {
            return true;
        }

        Minecraft minecraft = Minecraft.getInstance();
        return ClientQuestFile.exists()
                && minecraft.player != null
                && ClientQuestFile.INSTANCE.selfTeamData.isQuestPinned(minecraft.player, questId);
    }

    private static Toast extractToast(Object toastInstance) {
        try {
            return (Toast) toastInstanceGetToastMethod.invoke(toastInstance);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static QuestObject extractQuestObject(Toast toast) {
        try {
            return (QuestObject) toastQuestObjectField.get(toast);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private static boolean resolveToastReflection() {
        if (toastVisibleField != null && toastQueuedField != null && toastInstanceGetToastMethod != null && toastQuestObjectField != null) {
            return true;
        }

        try {
            toastVisibleField = ToastComponent.class.getDeclaredField("visible");
            toastVisibleField.setAccessible(true);
            toastQueuedField = ToastComponent.class.getDeclaredField("queued");
            toastQueuedField.setAccessible(true);

            Class<?> toastInstanceClass = Class.forName("net.minecraft.client.gui.components.toasts.ToastComponent$ToastInstance");
            toastInstanceGetToastMethod = toastInstanceClass.getDeclaredMethod("getToast");
            toastInstanceGetToastMethod.setAccessible(true);

            toastQuestObjectField = ToastQuestObject.class.getDeclaredField("object");
            toastQuestObjectField.setAccessible(true);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static final class ManualQuestCompletionToast extends SimpleToast {
        private final Quest quest;

        private ManualQuestCompletionToast(Quest quest) {
            this.quest = quest;
        }

        @Override
        public Component getTitle() {
            return quest.getObjectType().getCompletedMessage();
        }

        @Override
        public Component getSubtitle() {
            return quest.getTitle();
        }

        @Override
        public Icon getIcon() {
            return quest.getIcon();
        }
    }
}
