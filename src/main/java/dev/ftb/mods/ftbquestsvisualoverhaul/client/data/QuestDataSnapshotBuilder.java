package dev.ftb.mods.ftbquestsvisualoverhaul.client.data;

import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.ChapterGroup;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardClaimType;
import dev.ftb.mods.ftbquests.quest.reward.XPLevelsReward;
import dev.ftb.mods.ftbquests.quest.reward.XPReward;
import dev.ftb.mods.ftbquests.quest.task.CheckmarkTask;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.XPTask;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class QuestDataSnapshotBuilder {
    private static final String DATA_KEY = "data.ftbquestsvisualoverhaul.";

    public QuestDataSnapshot build(TeamData teamData) {
        List<QuestDataSnapshot.ChapterSnapshot> chapters = new ArrayList<>();
        Player player = FTBQuestsClient.getClientPlayer();
        UUID playerId = player == null ? null : player.getUUID();
        boolean hasFallbackEntries = false;
        long previousGroupId = Long.MIN_VALUE;

        for (Chapter chapter : teamData.getFile().getVisibleChapters(teamData)) {
            List<QuestDataSnapshot.QuestSnapshot> quests = new ArrayList<>();
            ChapterGroup group = chapter.getGroup();
            long groupId = group.getId();
            boolean firstInGroup = groupId != previousGroupId;
            previousGroupId = groupId;
            Component groupTitle = group.getTitle();
            if (groupTitle.getString().isBlank()) {
                groupTitle = group.isDefaultGroup() ? Component.translatable(DATA_KEY + "quest_chapters") : group.getAltTitle();
            }

            chapter.getQuests().stream()
                    .filter(quest -> quest.isVisible(teamData))
                    .sorted(Comparator.comparing(quest -> quest.getTitle().getString().toLowerCase(Locale.ROOT)))
                    .forEach(quest -> {
                        List<QuestDataSnapshot.TaskSnapshot> tasks = new ArrayList<>();
                        List<QuestDataSnapshot.RewardSnapshot> rewards = new ArrayList<>();
                        boolean accepted = player != null && teamData.isQuestPinned(player, quest.getId());

                        for (Task task : quest.getTasks()) {
                            TaskInteractionMode interactionMode = resolveTaskMode(task);
                            Component fallbackReason = interactionMode == TaskInteractionMode.VANILLA_FALLBACK
                                    ? Component.translatable(DATA_KEY + "fallback.task_interaction")
                                    : Component.empty();
                            long displayProgress = resolveDisplayProgress(task, teamData, player);
                            boolean completed = teamData.isCompleted(task);
                            boolean progressSatisfied = isTaskProgressSatisfied(task, teamData, player, displayProgress);

                            tasks.add(new QuestDataSnapshot.TaskSnapshot(
                                    task.getId(),
                                    task.getTitle(),
                                    Component.translatable(
                                            DATA_KEY + "progress_fraction",
                                            task.formatProgress(teamData, displayProgress),
                                            task.formatMaxProgress()
                                    ),
                                    taskCountLabel(task),
                                    task.getIcon(),
                                    completed,
                                    progressSatisfied,
                                    task instanceof CheckmarkTask,
                                    task.isOptionalForProgression(teamData),
                                    canTaskInteract(task, teamData, accepted, player, displayProgress),
                                    task.consumesResources(),
                                    interactionMode,
                                    fallbackReason
                            ));
                        }

                        for (Reward reward : quest.getRewards()) {
                            RewardInteractionMode interactionMode = resolveRewardMode(reward);
                            Component fallbackReason = interactionMode == RewardInteractionMode.VANILLA_FALLBACK
                                    ? Component.translatable(DATA_KEY + "fallback.reward_interaction")
                                    : Component.empty();
                            RewardClaimType claimType = playerId == null ? RewardClaimType.CANT_CLAIM : teamData.getClaimType(playerId, reward);
                            rewards.add(new QuestDataSnapshot.RewardSnapshot(
                                    reward.getId(),
                                    reward.getTitle(),
                                    rewardClaimTypeLabel(claimType),
                                    rewardCountLabel(reward),
                                    reward.getIcon(),
                                    claimType == RewardClaimType.CLAIMED,
                                    claimType == RewardClaimType.CAN_CLAIM,
                                    interactionMode,
                                    fallbackReason
                            ));
                        }

                        boolean checkmarkOnly = quest.getTasks().size() == 1 && quest.getTasks().stream().allMatch(CheckmarkTask.class::isInstance);
                        quests.add(new QuestDataSnapshot.QuestSnapshot(
                                quest.getId(),
                                chapter.getId(),
                                quest.getTitle(),
                                quest.getSubtitle(),
                                quest.getDescription(),
                                quest.getIcon(),
                                quest.getX(),
                                quest.getY(),
                                quest.getSize(),
                                quest.getShape(),
                                teamData.getRelativeProgress(quest),
                                teamData.isCompleted(quest),
                                teamData.isStarted(quest),
                                accepted,
                                teamData.canStartTasks(quest),
                                quest.hideDetailsUntilStartable() && !teamData.canStartTasks(quest) && !teamData.isCompleted(quest),
                                playerId != null && teamData.hasUnclaimedRewards(playerId, quest),
                                checkmarkOnly,
                                quest.streamDependencies()
                                        .filter(Quest.class::isInstance)
                                        .map(Quest.class::cast)
                                        .map(QuestObject::getId)
                                        .toList(),
                                quest.getDependants().stream()
                                        .filter(Quest.class::isInstance)
                                        .map(Quest.class::cast)
                                        .map(QuestObject::getId)
                                        .toList(),
                                List.copyOf(tasks),
                                List.copyOf(rewards)
                        ));
                    });

            hasFallbackEntries |= quests.stream().flatMap(q -> q.tasks().stream()).anyMatch(t -> t.interactionMode() == TaskInteractionMode.VANILLA_FALLBACK);
            hasFallbackEntries |= quests.stream().flatMap(q -> q.rewards().stream()).anyMatch(r -> r.interactionMode() == RewardInteractionMode.VANILLA_FALLBACK);

            chapters.add(new QuestDataSnapshot.ChapterSnapshot(
                    chapter.getId(),
                    groupId,
                    groupTitle,
                    firstInGroup,
                    chapter.getTitle(),
                    Component.empty(),
                    chapter.getIcon(),
                    teamData.getRelativeProgress(chapter),
                    List.copyOf(quests)
            ));
        }

        return new QuestDataSnapshot(chapters, hasFallbackEntries);
    }

    private static boolean canTaskInteract(Task task, TeamData teamData, boolean accepted, Player player, long displayProgress) {
        if (!accepted || teamData.isCompleted(task) || !teamData.canStartTasks(task.getQuest())) {
            return false;
        }
        if (task instanceof ItemTask itemTask && itemTask.consumesResources()) {
            return displayProgress >= itemTask.getMaxProgress();
        }
        return resolveTaskMode(task) == TaskInteractionMode.SUBMIT;
    }

    private static boolean isTaskProgressSatisfied(Task task, TeamData teamData, Player player, long displayProgress) {
        if (teamData.isCompleted(task)) {
            return true;
        }
        if (task instanceof ItemTask itemTask && itemTask.consumesResources() && player != null) {
            return displayProgress >= itemTask.getMaxProgress();
        }
        return false;
    }

    private static long resolveDisplayProgress(Task task, TeamData teamData, Player player) {
        if (task instanceof ItemTask itemTask && itemTask.consumesResources() && !teamData.isCompleted(task)) {
            return countMatchingInventoryItems(itemTask, player);
        }
        return teamData.getProgress(task);
    }

    private static long countMatchingInventoryItems(ItemTask itemTask, Player player) {
        if (player == null) {
            return 0L;
        }

        Collection<ItemStack> inventoryStacks = player.getInventory().items;
        long total = 0L;
        long required = itemTask.getMaxProgress();
        for (ItemStack stack : inventoryStacks) {
            if (itemTask.test(stack)) {
                total += stack.getCount();
                if (total >= required) {
                    return required;
                }
            }
        }
        return Math.min(total, required);
    }

    private static TaskInteractionMode resolveTaskMode(Task task) {
        if (!FTBQuestsAPI.MOD_ID.equals(task.getType().getTypeId().getNamespace())) {
            return TaskInteractionMode.VANILLA_FALLBACK;
        }
        if (task instanceof CheckmarkTask) {
            return TaskInteractionMode.SUBMIT;
        }
        if (task instanceof ItemTask itemTask) {
            return itemTask.consumesResources() ? TaskInteractionMode.SUBMIT : TaskInteractionMode.READ_ONLY;
        }
        return TaskInteractionMode.READ_ONLY;
    }

    private static RewardInteractionMode resolveRewardMode(Reward reward) {
        if (reward instanceof ChoiceReward) {
            return RewardInteractionMode.CHOICE;
        }
        if (!FTBQuestsAPI.MOD_ID.equals(reward.getType().getTypeId().getNamespace())) {
            return RewardInteractionMode.VANILLA_FALLBACK;
        }
        return RewardInteractionMode.CLAIM;
    }

    private static Component taskCountLabel(Task task) {
        if (task instanceof CheckmarkTask) {
            return Component.empty();
        }
        MutableComponent buttonText = task.getButtonText();
        String text = buttonText.getString().trim();
        if (task instanceof XPTask) {
            return text.isEmpty() ? Component.translatable(DATA_KEY + "count.plus_one") : buttonText;
        }
        return Component.translatable(DATA_KEY + "count.multiplier", text.isEmpty() ? "1" : text);
    }

    private static Component rewardCountLabel(Reward reward) {
        String text = reward.getButtonText().trim();
        if (reward instanceof XPReward || reward instanceof XPLevelsReward) {
            return text.isEmpty() ? Component.translatable(DATA_KEY + "count.plus_one") : Component.literal(text);
        }
        return Component.translatable(DATA_KEY + "count.multiplier", text.isEmpty() ? "1" : text);
    }

    private static Component rewardClaimTypeLabel(RewardClaimType claimType) {
        return switch (claimType) {
            case CANT_CLAIM -> Component.translatable(DATA_KEY + "reward_claim_type.cant_claim");
            case CAN_CLAIM -> Component.translatable(DATA_KEY + "reward_claim_type.can_claim");
            case CLAIMED -> Component.translatable(DATA_KEY + "reward_claim_type.claimed");
        };
    }
}
