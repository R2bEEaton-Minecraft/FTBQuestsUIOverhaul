package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

/**
 * Stateless re-derivation of the typewriter reveal count and the surrogate-pair-safe
 * prefix boundary. Carries no Minecraft dependency so it is directly unit-testable.
 */
public final class QuestNarrationPacing {
    private static final float CHARACTERS_PER_SECOND = 50F;
    private static final long BASE_DELAY_MILLIS = Math.round(1000D / CHARACTERS_PER_SECOND);
    private static final long COMMA_PAUSE_MILLIS = 300L;
    private static final long PERIOD_PAUSE_MILLIS = 600L;
    private static final long EXCLAMATION_PAUSE_MILLIS = 500L;
    private static final long QUESTION_PAUSE_MILLIS = 600L;

    private QuestNarrationPacing() {
    }

    public static int revealedCharacterCount(String text, long elapsedMillis) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        long remaining = Math.max(0L, elapsedMillis);
        int revealed = 0;
        while (revealed < text.length()) {
            long delay = BASE_DELAY_MILLIS + (revealed == 0 ? 0L : pauseAfter(text.charAt(revealed - 1)));
            if (remaining < delay) {
                break;
            }
            remaining -= delay;
            revealed++;
        }
        return revealed;
    }

    /** True if appending {@code codePoint} would not exceed the visible UTF-16 budget mid-surrogate-pair. */
    public static boolean codePointWithinPrefix(int usedUtf16CodeUnits, int codePoint, int visibleUtf16CodeUnits) {
        return usedUtf16CodeUnits + Character.charCount(codePoint) <= visibleUtf16CodeUnits;
    }

    private static long pauseAfter(char character) {
        return switch (character) {
            case ',' -> COMMA_PAUSE_MILLIS;
            case '.' -> PERIOD_PAUSE_MILLIS;
            case '!' -> EXCLAMATION_PAUSE_MILLIS;
            case '?' -> QUESTION_PAUSE_MILLIS;
            default -> 0L;
        };
    }
}
