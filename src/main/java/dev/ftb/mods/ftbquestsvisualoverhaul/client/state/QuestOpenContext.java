package dev.ftb.mods.ftbquestsvisualoverhaul.client.state;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import org.jetbrains.annotations.Nullable;

public record QuestOpenContext(
        boolean fromVanillaScreen,
        long requestedQuestId,
        long requestedChapterId,
        QuestViewState previousState
) {
    public static QuestOpenContext fromVanilla(QuestScreen vanillaScreen, QuestViewState previousState) {
        long questId = 0L;
        long chapterId = previousState.getSelectedChapterId();

        Quest viewedQuest = vanillaScreen.getViewedQuest();
        if (viewedQuest != null) {
            questId = viewedQuest.getId();
            chapterId = viewedQuest.getChapter().getId();
        } else if (ClientQuestFile.exists()) {
            Chapter firstVisible = ClientQuestFile.INSTANCE.getFirstVisibleChapter(ClientQuestFile.INSTANCE.selfTeamData);
            if (chapterId == 0L && firstVisible != null) {
                chapterId = firstVisible.getId();
            }
        }

        return new QuestOpenContext(true, questId, chapterId, previousState.copy());
    }

    @Nullable
    public Long requestedQuestIdOrNull() {
        return requestedQuestId == 0L ? null : requestedQuestId;
    }
}
