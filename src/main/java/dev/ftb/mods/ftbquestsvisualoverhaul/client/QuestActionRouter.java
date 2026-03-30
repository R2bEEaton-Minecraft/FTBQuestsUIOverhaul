package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.net.ClaimAllRewardsMessage;
import dev.ftb.mods.ftbquests.net.ClaimChoiceRewardMessage;
import dev.ftb.mods.ftbquests.net.ClaimRewardMessage;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.net.TogglePinnedMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;

public class QuestActionRouter {
    public void submitTask(Task task) {
        new SubmitTaskMessage(task.getId()).sendToServer();
    }

    public void claimReward(Reward reward, boolean notify) {
        new ClaimRewardMessage(reward.getId(), notify).sendToServer();
    }

    public void claimChoiceReward(ChoiceReward reward, int index) {
        new ClaimChoiceRewardMessage(reward.getId(), index).sendToServer();
    }

    public void claimAllRewards() {
        new ClaimAllRewardsMessage().sendToServer();
    }

    public void togglePin(Quest quest) {
        new TogglePinnedMessage(quest.getId()).sendToServer();
    }

    public void openVanillaForQuest(Quest quest) {
        VanillaFallbackBridge.openVanillaForQuest(quest);
    }

    public void openVanillaRoot() {
        VanillaFallbackBridge.openVanillaRoot();
    }
}
