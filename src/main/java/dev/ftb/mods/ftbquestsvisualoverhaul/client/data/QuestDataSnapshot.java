package dev.ftb.mods.ftbquestsvisualoverhaul.client.data;

import dev.ftb.mods.ftblibrary.icon.Icon;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class QuestDataSnapshot {
    private final List<ChapterSnapshot> chapters;
    private final Map<Long, ChapterSnapshot> chaptersById;
    private final Map<Long, QuestSnapshot> questsById;
    private final boolean hasFallbackEntries;

    public QuestDataSnapshot(List<ChapterSnapshot> chapters, boolean hasFallbackEntries) {
        this.chapters = List.copyOf(chapters);
        this.hasFallbackEntries = hasFallbackEntries;

        Map<Long, ChapterSnapshot> chapterMap = new LinkedHashMap<>();
        Map<Long, QuestSnapshot> questMap = new LinkedHashMap<>();
        for (ChapterSnapshot chapter : chapters) {
            chapterMap.put(chapter.id(), chapter);
            for (QuestSnapshot quest : chapter.quests()) {
                questMap.put(quest.id(), quest);
            }
        }

        this.chaptersById = Collections.unmodifiableMap(chapterMap);
        this.questsById = Collections.unmodifiableMap(questMap);
    }

    public List<ChapterSnapshot> chapters() {
        return chapters;
    }

    public ChapterSnapshot findChapter(long id) {
        return chaptersById.get(id);
    }

    public QuestSnapshot findQuest(long id) {
        return questsById.get(id);
    }

    public boolean hasFallbackEntries() {
        return hasFallbackEntries;
    }

    public record ChapterSnapshot(
            long id,
            long groupId,
            Component groupTitle,
            boolean firstInGroup,
            Component title,
            Component subtitle,
            Icon icon,
            int progress,
            List<QuestSnapshot> quests
    ) {
    }

    public record QuestSnapshot(
            long id,
            long chapterId,
            Component title,
            Component subtitle,
            List<Component> description,
            Icon icon,
            double x,
            double y,
            double size,
            String shape,
            int progress,
            boolean completed,
            boolean started,
            boolean pinned,
            boolean canStart,
            boolean hiddenDetails,
            boolean hasUnclaimedRewards,
            boolean checkmarkOnly,
            List<Long> dependencyQuestIds,
            List<Long> dependantQuestIds,
            List<TaskSnapshot> tasks,
            List<RewardSnapshot> rewards
    ) {
    }

    public record TaskSnapshot(
            long id,
            Component title,
            Component progressText,
            Component countLabel,
            Icon icon,
            boolean completed,
            boolean progressSatisfied,
            boolean checkmarkTask,
            boolean optional,
            boolean canInteract,
            boolean consumesResources,
            TaskInteractionMode interactionMode,
            Component fallbackReason
    ) {
    }

    public record RewardSnapshot(
            long id,
            Component title,
            Component statusText,
            Component countLabel,
            Icon icon,
            boolean claimed,
            boolean canClaim,
            RewardInteractionMode interactionMode,
            Component fallbackReason
    ) {
    }
}
