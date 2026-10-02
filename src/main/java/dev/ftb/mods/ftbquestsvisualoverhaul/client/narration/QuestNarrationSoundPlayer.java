package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Plays the addon's own dialogue_blip for newly revealed letters/digits.
 * Guards against overlap so a lag spike cannot sound like a burst of clicks.
 */
public final class QuestNarrationSoundPlayer {
    private static final float PITCH = 1.0F;
    private static final float VOLUME = 0.70F;
    private static final long TAIL_THRESHOLD_MILLIS = 40L;
    private static final int MAX_QUEUED_BLIPS = 8;

    private SoundInstance activeBlip;
    private long activeBlipStartedAtMillis;

    public void playNewlyRevealed(String characters) {
        if (characters == null || characters.isEmpty()) {
            return;
        }
        int start = Math.max(0, characters.length() - MAX_QUEUED_BLIPS);
        for (int i = start; i < characters.length(); i++) {
            playFor(characters.charAt(i));
        }
    }

    private void playFor(char character) {
        if (!Character.isLetterOrDigit(character)) {
            return;
        }
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        boolean blipActive = activeBlip != null && soundManager.isActive(activeBlip);
        long activeDurationMillis = Math.max(0L, Util.getMillis() - activeBlipStartedAtMillis);
        if (blipActive && activeDurationMillis < TAIL_THRESHOLD_MILLIS) {
            return;
        }
        SoundInstance blip = new ResolvedBlipSoundInstance(PITCH, VOLUME);
        soundManager.play(blip);
        activeBlip = blip;
        activeBlipStartedAtMillis = Util.getMillis();
    }

    /** Retains the same weighted sound variant through playback, matching UI-sound behavior. */
    private static final class ResolvedBlipSoundInstance extends SimpleSoundInstance {
        private WeighedSoundEvents resolvedEvents;

        ResolvedBlipSoundInstance(float pitch, float volume) {
            super(ModSounds.DIALOGUE_BLIP.get().getLocation(), SoundSource.MASTER, volume, pitch,
                    SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        }

        @Override
        public WeighedSoundEvents resolve(SoundManager manager) {
            if (resolvedEvents == null) {
                resolvedEvents = super.resolve(manager);
            }
            return resolvedEvents;
        }
    }
}
