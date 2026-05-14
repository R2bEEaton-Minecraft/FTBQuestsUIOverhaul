package dev.ftb.mods.ftbquestsvisualoverhaul.client.screen;

import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.QuestActionRouter;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.QuestUiFeedback;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class ChoiceRewardSelectScreen extends Screen {
    private static final int MODAL_MIN_WIDTH = 188;
    private static final int MODAL_MAX_WIDTH = 232;
    private static final int MODAL_MARGIN = 28;
    private static final int HEADER_HEIGHT = 30;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 3;
    private static final int MAX_VISIBLE_ROWS = 5;
    private static final float TEXT_SCALE = 0.8F;

    private final Screen parent;
    private final ChoiceReward reward;
    private final QuestActionRouter router;
    private final List<RowTarget> targets = new ArrayList<>();
    private double scroll;

    public ChoiceRewardSelectScreen(Screen parent, ChoiceReward reward, QuestActionRouter router) {
        super(Component.literal("Choose Reward"));
        this.parent = parent;
        this.reward = reward;
        this.router = router;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (parent instanceof OverhaulQuestScreen overhaulQuestScreen) {
            overhaulQuestScreen.renderChoiceBackdrop(graphics, mouseX, mouseY, partialTick);
        } else {
            renderBackground(graphics);
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        graphics.fill(0, 0, width, height, 0xB015110D);
        targets.clear();

        List<WeightedReward> choices = getChoices();
        Layout layout = buildLayout(choices);
        Rect modal = layout.modal();
        Rect header = layout.header();
        Rect body = layout.body();

        graphics.fill(modal.x(), modal.y(), modal.maxX(), modal.maxY(), 0xD4211711);
        drawInsetBorder(graphics, modal, 0xFF8F6938, 0xCC160F0B);
        graphics.fill(modal.x() + 6, modal.y() + 6, modal.maxX() - 6, modal.maxY() - 6, 0x96241711);

        graphics.fill(header.x(), header.y(), header.maxX(), header.maxY(), 0xCC8F6A1D);
        drawInsetBorder(graphics, header, 0xFFD6A85C, 0xAA3E2B13);

        reward.getIcon().draw(graphics, header.x() + 7, header.y() + 6, 16, 16);
        drawCenteredScaledString(graphics, Component.literal("Choose Reward"), header.centerX(), header.y() + 6, 0xFFF6EDDB, TEXT_SCALE);
        drawCenteredScaledString(graphics, trim(reward.getQuest().getTitle(), header.width() - 52), header.centerX(), header.y() + 16, 0xFFD8C49D, TEXT_SCALE);

        Rect closeRect = new Rect(header.maxX() - 18, header.y() + 4, 12, 12);
        drawCenteredScaledString(graphics, Component.literal("x"), closeRect.centerX(), closeRect.y() + 4, 0xFFF6E8C9, TEXT_SCALE);
        targets.add(new RowTarget(closeRect, -1));

        graphics.enableScissor(body.x(), body.y(), body.maxX(), body.maxY());
        int contentY = body.y() + 1 - (int) scroll;
        for (int i = 0; i < choices.size(); i++) {
            WeightedReward choice = choices.get(i);
            Rect rowRect = new Rect(body.x(), contentY, body.width(), ROW_HEIGHT);
            boolean hovered = rowRect.contains(mouseX, mouseY);

            graphics.fill(rowRect.x(), rowRect.y(), rowRect.maxX(), rowRect.maxY(), hovered ? 0x7A5A3F25 : 0x5635261B);
            drawInsetBorder(graphics, rowRect, hovered ? 0xFFB8894D : 0x88634731, 0x55201510);

            choice.getReward().getIcon().draw(graphics, rowRect.x() + 3, rowRect.y() + 2, 14, 14);
            drawScaledString(graphics, trim(choice.getReward().getTitle(), rowRect.width() - 74), rowRect.x() + 22, rowRect.y() + 6, 0xFFF0E2C5, TEXT_SCALE);

            String action = "Choose";
            int actionWidth = Math.round(font.width(action) * TEXT_SCALE);
            int actionX = rowRect.maxX() - actionWidth - 6;
            drawScaledString(graphics, Component.literal(action), actionX, rowRect.y() + 6, hovered ? 0xFFF4DCB6 : 0xFFC8A97C, TEXT_SCALE);

            targets.add(new RowTarget(rowRect, i));
            contentY += ROW_HEIGHT + ROW_GAP;
        }
        graphics.disableScissor();

        if (layout.contentHeight() > body.height()) {
            int indicatorX = body.maxX() - 10;
            boolean canScrollUp = scroll > 0D;
            boolean canScrollDown = scroll < layout.contentHeight() - body.height();
            drawCenteredScaledString(graphics, Component.literal("^"), indicatorX, body.y() - 2, canScrollUp ? 0xFFE7D0A8 : 0xAA77644A, TEXT_SCALE);
            drawCenteredScaledString(graphics, Component.literal("v"), indicatorX, body.maxY() - 8, canScrollDown ? 0xFFE7D0A8 : 0xAA77644A, TEXT_SCALE);
        }
        graphics.pose().popPose();
        CursorType.set(isInteractiveTargetHovered(mouseX, mouseY) ? CursorType.HAND : null);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = targets.size() - 1; i >= 0; i--) {
                RowTarget target = targets.get(i);
                if (target.contains(mouseX, mouseY)) {
                    QuestUiFeedback.playUiClickSound();
                    if (target.index() >= 0) {
                        router.claimChoiceReward(reward, target.index());
                        QuestUiFeedback.playRewardConfirmSound();
                    }
                    onClose();
                    return true;
                }
            }

            Layout layout = buildLayout(getChoices());
            if (!layout.modal().contains(mouseX, mouseY)) {
                onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        Layout layout = buildLayout(getChoices());
        if (layout.body().contains(mouseX, mouseY)) {
            scroll = clampScroll(scroll - deltaY * 16D, layout.contentHeight(), layout.body().height());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaY);
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

    @Override
    public void removed() {
        CursorType.set(null);
        super.removed();
    }

    private List<WeightedReward> getChoices() {
        return reward.getTable() == null ? List.of() : reward.getTable().getWeightedRewards();
    }

    private Layout buildLayout(List<WeightedReward> choices) {
        int contentWidth = choices.stream()
                .map(WeightedReward::getReward)
                .mapToInt(r -> font.width(r.getTitle()))
                .max()
                .orElse(font.width(reward.getQuest().getTitle()));
        int modalWidth = Mth.clamp(contentWidth + 84, MODAL_MIN_WIDTH, Math.min(MODAL_MAX_WIDTH, width - 36));

        int visibleRows = Math.max(1, Math.min(MAX_VISIBLE_ROWS, choices.size()));
        int bodyHeight = visibleRows * ROW_HEIGHT + Math.max(0, visibleRows - 1) * ROW_GAP + 4;
        int modalHeight = HEADER_HEIGHT + bodyHeight + 22;
        int maxHeight = height - MODAL_MARGIN;
        if (modalHeight > maxHeight) {
            bodyHeight = Math.max(ROW_HEIGHT + 4, maxHeight - HEADER_HEIGHT - 22);
            modalHeight = HEADER_HEIGHT + bodyHeight + 22;
        }

        Rect modal = new Rect((width - modalWidth) / 2, (height - modalHeight) / 2, modalWidth, modalHeight);
        Rect header = new Rect(modal.x() + 10, modal.y() + 8, modal.width() - 20, HEADER_HEIGHT);
        Rect body = new Rect(modal.x() + 12, header.maxY() + 8, modal.width() - 24, modal.maxY() - header.maxY() - 18);
        int contentHeight = choices.isEmpty() ? ROW_HEIGHT : choices.size() * ROW_HEIGHT + Math.max(0, choices.size() - 1) * ROW_GAP;

        scroll = clampScroll(scroll, contentHeight, body.height());
        return new Layout(modal, header, body, contentHeight);
    }

    private void drawInsetBorder(GuiGraphics graphics, Rect rect, int borderColor, int shadeColor) {
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.y() + 1, borderColor);
        graphics.fill(rect.x(), rect.maxY() - 1, rect.maxX(), rect.maxY(), shadeColor);
        graphics.fill(rect.x(), rect.y(), rect.x() + 1, rect.maxY(), borderColor);
        graphics.fill(rect.maxX() - 1, rect.y(), rect.maxX(), rect.maxY(), shadeColor);
    }

    private void drawScaledString(GuiGraphics graphics, Component component, int x, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.drawString(font, component, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void drawCenteredScaledString(GuiGraphics graphics, Component component, int centerX, int y, int color, float scale) {
        int scaledWidth = Math.round(font.width(component) * scale);
        drawScaledString(graphics, component, centerX - scaledWidth / 2, y, color, scale);
    }

    private Component trim(Component component, int width) {
        return Component.literal(font.plainSubstrByWidth(component.getString(), Math.max(8, width)));
    }

    private double clampScroll(double currentScroll, int contentHeight, int visibleHeight) {
        return Mth.clamp(currentScroll, 0D, Math.max(0, contentHeight - visibleHeight));
    }

    private boolean isInteractiveTargetHovered(double mouseX, double mouseY) {
        for (int i = targets.size() - 1; i >= 0; i--) {
            if (targets.get(i).contains(mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    private record RowTarget(Rect rect, int index) {
        boolean contains(double mouseX, double mouseY) {
            return rect.contains(mouseX, mouseY);
        }
    }

    private record Layout(Rect modal, Rect header, Rect body, int contentHeight) {
    }

    private record Rect(int x, int y, int width, int height) {
        int maxX() {
            return x + width;
        }

        int maxY() {
            return y + height;
        }

        int centerX() {
            return x + width / 2;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= maxX() && mouseY >= y && mouseY <= maxY();
        }
    }
}
