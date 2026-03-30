package dev.ftb.mods.ftbquestsvisualoverhaul.client.state;

public enum QuestSortMode {
    PROGRESSION,
    TITLE;

    public QuestSortMode next() {
        return this == PROGRESSION ? TITLE : PROGRESSION;
    }
}
