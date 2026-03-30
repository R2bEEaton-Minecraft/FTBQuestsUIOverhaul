package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.QuestDataSnapshot;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.QuestDataSnapshotBuilder;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestViewState;

public class QuestDataController {
    private static final QuestDataSnapshotBuilder SNAPSHOT_BUILDER = new QuestDataSnapshotBuilder();

    private static QuestViewState persistedViewState = new QuestViewState();
    private static QuestDataSnapshot snapshot = new QuestDataSnapshot(java.util.List.of(), false);
    private static boolean dirty = true;

    private QuestDataController() {
    }

    public static void markDirty() {
        dirty = true;
    }

    public static QuestDataSnapshot getSnapshot() {
        if (dirty && ClientQuestFile.exists()) {
            snapshot = SNAPSHOT_BUILDER.build(ClientQuestFile.INSTANCE.selfTeamData);
            dirty = false;
        }
        return snapshot;
    }

    public static QuestViewState copyPersistedViewState() {
        return persistedViewState.copy();
    }

    public static void saveViewState(QuestViewState state) {
        persistedViewState = state.copy();
    }
}
