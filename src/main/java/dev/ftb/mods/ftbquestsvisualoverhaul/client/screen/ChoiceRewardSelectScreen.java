package dev.ftb.mods.ftbquestsvisualoverhaul.client.screen;

import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.QuestActionRouter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ChoiceRewardSelectScreen extends Screen {
    private final Screen parent;
    private final ChoiceReward reward;
    private final QuestActionRouter router;
    private final List<RowTarget> targets = new ArrayList<>();

    public ChoiceRewardSelectScreen(Screen parent, ChoiceReward reward, QuestActionRouter router) {
        super(Component.translatable("ftbquests.reward.ftbquests.choice"));
        this.parent = parent;
        this.reward = reward;
        this.router = router;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        targets.clear();

        int boxW = Math.min(320, width - 40);
        int boxX = (width - boxW) / 2;
        int y = 36;

        graphics.fill(boxX, 20, boxX + boxW, height - 20, 0xEE201711);
        graphics.fill(boxX + 1, 21, boxX + boxW - 1, height - 21, 0xEE322319);
        graphics.drawCenteredString(font, title, width / 2, 28, 0xF7E7C8);

        if (reward.getTable() != null) {
            int index = 0;
            for (WeightedReward weightedReward : reward.getTable().getWeightedRewards()) {
                int rowY = y + index * 28;
                boolean hovered = mouseX >= boxX + 10 && mouseX <= boxX + boxW - 10 && mouseY >= rowY && mouseY <= rowY + 22;
                int fill = hovered ? 0xFFB56E2B : 0xFF6F4B26;
                graphics.fill(boxX + 10, rowY, boxX + boxW - 10, rowY + 22, fill);
                weightedReward.getReward().getIcon().draw(graphics, boxX + 14, rowY + 3, 16, 16);
                graphics.drawString(font, weightedReward.getReward().getTitle(), boxX + 36, rowY + 7, 0xFFF6EDD7, false);
                targets.add(new RowTarget(boxX + 10, rowY, boxW - 20, 22, index));
                index++;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (RowTarget target : targets) {
                if (target.contains(mouseX, mouseY)) {
                    router.claimChoiceReward(reward, target.index());
                    onClose();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private record RowTarget(int x, int y, int width, int height, int index) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
}
