package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public final class QuestUiFeedback {
    private QuestUiFeedback() {
    }

    public static void playRewardConfirmSound() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.15F));
        }
    }
}
