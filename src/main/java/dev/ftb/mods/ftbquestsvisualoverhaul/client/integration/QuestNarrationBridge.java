package dev.ftb.mods.ftbquestsvisualoverhaul.client.integration;

/** Optional presentation hook for quest descriptions. */
public interface QuestNarrationBridge {
    QuestNarrationBridge NONE = new QuestNarrationBridge() {
        @Override
        public int revealedCharacterCount(String text, long elapsedMillis) {
            return text == null ? 0 : text.length();
        }

        @Override
        public void playNewlyRevealed(String characters) {
        }
    };

    int revealedCharacterCount(String text, long elapsedMillis);

    void playNewlyRevealed(String characters);
}
