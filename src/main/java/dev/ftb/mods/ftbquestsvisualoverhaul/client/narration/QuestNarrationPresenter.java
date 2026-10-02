package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

/** The one native narration object OverhaulQuestScreen owns; no optional-mod awareness. */
public final class QuestNarrationPresenter {
    private final QuestNarrationSoundPlayer soundPlayer = new QuestNarrationSoundPlayer();

    public int revealedCharacterCount(String text, long elapsedMillis) {
        return QuestNarrationPacing.revealedCharacterCount(text, elapsedMillis);
    }

    public void playNewlyRevealed(String characters) {
        soundPlayer.playNewlyRevealed(characters);
    }
}
