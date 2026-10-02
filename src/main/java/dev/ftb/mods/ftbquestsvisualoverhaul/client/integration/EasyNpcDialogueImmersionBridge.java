package dev.ftb.mods.ftbquestsvisualoverhaul.client.integration;

import net.minecraftforge.fml.ModList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Keeps ENDI entirely behind reflection: a normal installation never links an
 * optional-mod class while it is absent.
 */
public final class EasyNpcDialogueImmersionBridge implements QuestNarrationBridge {
    private static final float QUEST_SPEED_MULTIPLIER = 1.25F;
    private final Method revealCount;
    private final Constructor<?> revealTiming;
    private final Object configClient;
    private final Field commaPause;
    private final Field periodPause;
    private final Field exclamationPause;
    private final Field questionPause;
    private final Method configValueGet;
    private final Method playFor;
    private final Object defaultVoice;
    private final Field typewriterCps;

    private EasyNpcDialogueImmersionBridge() throws ReflectiveOperationException {
        ClassLoader loader = EasyNpcDialogueImmersionBridge.class.getClassLoader();
        Class<?> reveal = Class.forName("com.r2beeaton.easynpcdialogueimmersion.dialogue.PunctuationPacedReveal", true, loader);
        Class<?> timing = Class.forName("com.r2beeaton.easynpcdialogueimmersion.dialogue.RevealTiming", true, loader);
        Class<?> config = Class.forName("com.r2beeaton.easynpcdialogueimmersion.config.EasyNpcDialogueImmersionConfig", true, loader);
        Class<?> speech = Class.forName("com.r2beeaton.easynpcdialogueimmersion.audio.SpeechSoundPlayer", true, loader);
        Class<?> voice = Class.forName("com.r2beeaton.easynpcdialogueimmersion.audio.DialogueVoiceSettings", true, loader);

        revealCount = reveal.getMethod("revealedCharacterCount", String.class, long.class, long.class, timing);
        revealTiming = timing.getConstructor(long.class, long.class, long.class, long.class);
        configClient = config.getField("CLIENT").get(null);
        Class<?> clientType = configClient.getClass();
        commaPause = clientType.getField("commaPauseMillis");
        periodPause = clientType.getField("periodPauseMillis");
        exclamationPause = clientType.getField("exclamationPauseMillis");
        questionPause = clientType.getField("questionPauseMillis");
        configValueGet = commaPause.get(configClient).getClass().getMethod("get");
        defaultVoice = voice.getField("DEFAULT").get(null);
        playFor = speech.getMethod("playFor", char.class, voice);
        Field configuredCps;
        try {
            configuredCps = Class.forName("de.markusbordihn.easynpc.config.ClientDialogConfig", true, loader)
                    .getField("TYPEWRITER_CHARS_PER_SECOND");
        } catch (ReflectiveOperationException ignored) {
            configuredCps = null;
        }
        typewriterCps = configuredCps;
    }

    public static QuestNarrationBridge create() {
        if (!ModList.get().isLoaded("easynpcdialogueimmersion")) {
            return QuestNarrationBridge.NONE;
        }
        try {
            return new EasyNpcDialogueImmersionBridge();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return QuestNarrationBridge.NONE;
        }
    }

    @Override
    public int revealedCharacterCount(String text, long elapsedMillis) {
        try {
            int cps = Math.max(1, typewriterCps == null ? 40 : typewriterCps.getInt(null));
            long delayMs = Math.max(1L, Math.round(1000D / (cps * QUEST_SPEED_MULTIPLIER)));
            Object timing = revealTiming.newInstance(
                    pause(commaPause), pause(periodPause), pause(exclamationPause), pause(questionPause));
            return (Integer) revealCount.invoke(null, text, elapsedMillis, delayMs, timing);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return text == null ? 0 : text.length();
        }
    }

    @Override
    public void playNewlyRevealed(String characters) {
        if (characters == null || characters.isEmpty()) return;
        // Do not turn a lagged render into a burst of dozens of dialogue blips.
        int start = Math.max(0, characters.length() - 8);
        try {
            for (int i = start; i < characters.length(); i++) playFor.invoke(null, characters.charAt(i), defaultVoice);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }

    private long pause(Field field) throws ReflectiveOperationException {
        Object value = configValueGet.invoke(field.get(configClient));
        return value instanceof Number number ? Math.max(0L, number.longValue()) : 0L;
    }
}
