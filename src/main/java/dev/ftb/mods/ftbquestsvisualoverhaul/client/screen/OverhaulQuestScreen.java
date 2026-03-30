package dev.ftb.mods.ftbquestsvisualoverhaul.client.screen;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.QuestActionRouter;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.QuestDataController;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModClientConfig;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.QuestDataSnapshot;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.RewardInteractionMode;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.TaskInteractionMode;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestOpenContext;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestViewState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OverhaulQuestScreen extends Screen {
    private static final ResourceLocation QUESTS_BACKGROUND_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/gui/quests_background.png");
    private static final ResourceLocation OAK_PLANKS_TEXTURE = new ResourceLocation("minecraft", "textures/block/oak_planks.png");
    private static final ResourceLocation ADVANCEMENT_WINDOW_TEXTURE = new ResourceLocation("minecraft", "textures/gui/advancements/window.png");
    private static final ResourceLocation ADVANCEMENT_WIDGETS_TEXTURE = new ResourceLocation("minecraft", "textures/gui/advancements/widgets.png");
    private static final int BACKGROUND_WIDTH = 294;
    private static final int BACKGROUND_HEIGHT = 163;
    private static final int BACKGROUND_TEXTURE_WIDTH = 512;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    private static final int TREE_X = 89;
    private static final int TREE_Y = 19;
    private static final int TREE_WIDTH = 186;
    private static final int TREE_HEIGHT = 125;

    private static final int OUTER_MARGIN = 18;
    private static final int HEADER_HEIGHT = 58;
    private static final int CHAPTER_WIDTH = 152;
    private static final int SECTION_GAP = 12;
    private static final int CHAPTER_ROW_HEIGHT = 42;
    private static final int NODE_SPACING_X = 28;
    private static final int NODE_SPACING_Y = 27;
    private static final int NODE_BASE_WIDTH = 26;
    private static final int NODE_BASE_HEIGHT = 26;
    private static final int ADVANCEMENT_WIDGET_SIZE = 26;
    private static final int ADVANCEMENT_WIDGET_U = 0;
    private static final int ADVANCEMENT_WIDGET_OBTAINED_V = 128;
    private static final int ADVANCEMENT_WIDGET_UNOBTAINED_V = 154;
    private static final int MODAL_MIN_WIDTH = 280;
    private static final int MODAL_MAX_WIDTH = 440;
    private static final int MODAL_HEADER_HEIGHT = 50;
    private static final int MODAL_MARGIN = 68;

    private final QuestOpenContext openContext;
    private final QuestActionRouter actionRouter = new QuestActionRouter();
    private final QuestViewState viewState;
    private final List<ClickTarget> clickTargets = new ArrayList<>();

    private EditBox searchBox;
    private Button claimAllButton;
    private Button focusButton;
    private Button vanillaButton;
    private Button pinButton;

    private final Map<Long, NodeLayout> visibleQuestNodes = new HashMap<>();
    private QuestDataSnapshot.QuestSnapshot hoveredQuest;
    private boolean draggingTree;
    private double dragOriginMouseX;
    private double dragOriginMouseY;
    private double dragOriginPanX;
    private double dragOriginPanY;
    private long focusedChapterId = Long.MIN_VALUE;

    public OverhaulQuestScreen(QuestOpenContext openContext) {
        super(Component.literal("Quest Log"));
        this.openContext = openContext;
        this.viewState = openContext.previousState().copy();

        if (openContext.requestedChapterId() != 0L) {
            viewState.setSelectedChapterId(openContext.requestedChapterId());
        }
        if (openContext.requestedQuestId() != 0L) {
            viewState.setViewedQuestId(openContext.requestedQuestId());
        }
    }

    @Override
    protected void init() {
        clearWidgets();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void removed() {
        QuestDataController.saveViewState(viewState);
        super.removed();
    }

    @Override
    public void onClose() {
        QuestDataController.saveViewState(viewState);
        super.onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (FTBQuestsClient.KEY_QUESTS != null && FTBQuestsClient.KEY_QUESTS.matches(keyCode, scanCode)) {
            if (viewState.getViewedQuestId() != 0L) {
                closeViewedQuest();
            } else {
                onClose();
            }
            return true;
        }
        if (keyCode == 256 && viewState.getViewedQuestId() != 0L) {
            closeViewedQuest();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        QuestDataSnapshot.QuestSnapshot selectedQuest = getSelectedQuestSnapshot(snapshot);
        if (selectedQuest != null) {
            if (button == 0 && handleClickTargets(mouseX, mouseY)) {
                return true;
            }

            Rect modalRect = buildDetailLayout(selectedQuest).rect();
            if (!modalRect.contains(mouseX, mouseY)) {
                closeViewedQuest();
            }
            return true;
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && handleClickTargets(mouseX, mouseY)) {
            return true;
        }
        if (button == 0 && treeCanvasRect(treeRect(frameRect())).contains(mouseX, mouseY)) {
            draggingTree = true;
            dragOriginMouseX = mouseX;
            dragOriginMouseY = mouseY;
            dragOriginPanX = viewState.getTreePanX();
            dragOriginPanY = viewState.getTreePanY();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingTree = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingTree && button == 0) {
            viewState.setTreePanX(dragOriginPanX + mouseX - dragOriginMouseX);
            viewState.setTreePanY(dragOriginPanY + mouseY - dragOriginMouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaY) {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        QuestDataSnapshot.QuestSnapshot selectedQuest = getSelectedQuestSnapshot(snapshot);
        if (selectedQuest != null) {
            DetailLayout layout = buildDetailLayout(selectedQuest);
            Rect bodyRect = layout.bodyRect();
            if (bodyRect.contains(mouseX, mouseY)) {
                viewState.setDetailScroll(clampScroll(viewState.getDetailScroll() - deltaY * 24D, layout.contentHeight(), bodyRect.height()));
            }
            return true;
        }

        Rect frame = frameRect();
        Rect chapterRail = chapterRailRect(frame);
        Rect treeCanvas = treeCanvasRect(treeRect(frame));

        if (chapterRail.contains(mouseX, mouseY)) {
            int contentHeight = QuestDataController.getSnapshot().chapters().size() * (CHAPTER_ROW_HEIGHT + 8);
            viewState.setChapterScroll(clampScroll(viewState.getChapterScroll() - deltaY * 20D, contentHeight, chapterRail.height() - 20));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        refreshState();
        hoveredQuest = null;
        visibleQuestNodes.clear();
        clickTargets.clear();
        renderBackdrop(graphics);
        renderTreePanel(graphics, snapshot, treeRect(frameRect()), mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (viewState.getViewedQuestId() == 0L && hoveredQuest != null) {
            renderHoverSummary(graphics, hoveredQuest, mouseX, mouseY);
        }

        QuestDataSnapshot.QuestSnapshot selectedQuest = getSelectedQuestSnapshot(snapshot);
        if (selectedQuest != null) {
            renderQuestDetailModal(graphics, selectedQuest, mouseX, mouseY);
        }
    }

    private void renderBackdrop(GuiGraphics graphics) {
        graphics.fill(0, 0, width, height, 0xC0100B08);
        Rect frame = frameRect();
        graphics.blit(QUESTS_BACKGROUND_TEXTURE, frame.x(), frame.y(), 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);
    }

    private void renderFrame(GuiGraphics graphics, Rect frame, Rect header) {
        drawPanel(graphics, frame, 0xFF201610, 0xFF362219, 0xFFB28752);
        drawPanel(graphics, header, 0xFF3A261A, 0xFF291B13, 0xFFD5A86A);

        graphics.blit(ADVANCEMENT_WINDOW_TEXTURE, header.x() + 8, header.y() + 6, 0, 0, 252, 140, 256, 256);
        graphics.fill(header.x() + 6, header.y() + 6, header.maxX() - 6, header.maxY() - 6, 0xCC24160F);

        graphics.drawString(font, title, frame.x() + 18, frame.y() + 10, 0xFFF8EACC, false);
        graphics.drawString(font, Component.literal("Adventure log with a chapter rail, tree map, and quest dossier"), frame.x() + 18, frame.y() + 24, 0xFFD7B990, false);
    }

    private void renderChapterRail(GuiGraphics graphics, QuestDataSnapshot snapshot, Rect rect, int mouseX, int mouseY) {
        drawPanel(graphics, rect, 0xFF231913, 0xFF18110D, 0xFF85603D);
        graphics.drawString(font, Component.literal("Chapters"), rect.x() + 12, rect.y() + 10, 0xFFF5E4C0, false);

        int rowY = rect.y() + 32 - (int) viewState.getChapterScroll();
        graphics.enableScissor(rect.x() + 4, rect.y() + 28, rect.maxX() - 4, rect.maxY() - 6);
        for (QuestDataSnapshot.ChapterSnapshot chapter : snapshot.chapters()) {
            int x = rect.x() + 8;
            int y = rowY;
            boolean selected = chapter.id() == viewState.getSelectedChapterId();
            boolean hovered = new Rect(x, y, rect.width() - 16, CHAPTER_ROW_HEIGHT).contains(mouseX, mouseY);
            int fill = selected ? 0xFF7E552F : hovered ? 0xFF553826 : 0xFF2E211A;

            graphics.fill(x, y, rect.maxX() - 8, y + CHAPTER_ROW_HEIGHT, fill);
            graphics.fill(x, y, rect.maxX() - 8, y + 4, selected ? 0xFFD5A86A : 0xFF5C3E29);
            chapter.icon().draw(graphics, x + 8, y + 12, 18, 18);
            graphics.drawString(font, trim(chapter.title(), rect.width() - 54), x + 32, y + 9, 0xFFF7EBD2, false);
            drawProgressBar(graphics, x + 32, y + 24, rect.width() - 56, 6, chapter.progress(), 0xFF4D3829, 0xFFE2B66F);
            graphics.drawString(font, Component.literal(chapter.progress() + "%"), rect.maxX() - 34, y + 8, 0xFFE4CAA1, false);

            clickTargets.add(new ClickTarget(new Rect(x, y, rect.width() - 16, CHAPTER_ROW_HEIGHT), () -> {
                viewState.setSelectedChapterId(chapter.id());
                closeViewedQuest();
                focusCurrentChapter(true);
            }));

            rowY += CHAPTER_ROW_HEIGHT + 8;
        }
        graphics.disableScissor();
    }

    private void renderTreePanel(GuiGraphics graphics, QuestDataSnapshot snapshot, Rect rect, int mouseX, int mouseY) {
        QuestDataSnapshot.ChapterSnapshot chapter = snapshot.findChapter(viewState.getSelectedChapterId());
        Rect canvas = treeCanvasRect(rect);
        graphics.enableScissor(canvas.x(), canvas.y(), canvas.maxX(), canvas.maxY());
        tileOakPlanksBackground(graphics, canvas);

        List<QuestDataSnapshot.QuestSnapshot> quests = chapter == null ? List.of() : chapter.quests();
        Map<Long, NodeLayout> nodes = buildNodeLayouts(quests, canvas);
        renderQuestConnections(graphics, quests, nodes);
        renderQuestNodes(graphics, quests, nodes, mouseX, mouseY);

        graphics.disableScissor();
        drawInsetBorder(graphics, canvas, 0x88876646, 0xCC110C09);
    }

    private void tileOakPlanksBackground(GuiGraphics graphics, Rect rect) {
        int offsetX = Mth.floor(viewState.getTreePanX()) % 16;
        int offsetY = Mth.floor(viewState.getTreePanY()) % 16;
        if (offsetX > 0) {
            offsetX -= 16;
        }
        if (offsetY > 0) {
            offsetY -= 16;
        }

        for (int y = rect.y() + offsetY; y < rect.maxY(); y += 16) {
            for (int x = rect.x() + offsetX; x < rect.maxX(); x += 16) {
                graphics.blit(OAK_PLANKS_TEXTURE, x, y, 0, 0, 16, 16, 16, 16);
            }
        }
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), 0x55140F0B);
    }

    private void renderQuestConnections(GuiGraphics graphics, List<QuestDataSnapshot.QuestSnapshot> quests, Map<Long, NodeLayout> nodes) {
        String query = normalizedQuery();
        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            NodeLayout node = nodes.get(quest.id());
            if (node == null) {
                continue;
            }

            boolean questMatch = query.isEmpty() || questMatchesQuery(quest, query);
            for (Long dependencyId : quest.dependencyQuestIds()) {
                NodeLayout dependencyNode = nodes.get(dependencyId);
                if (dependencyNode == null) {
                    continue;
                }

                boolean dependencyMatch = query.isEmpty() || questMatchesQuery(dependencyNode.quest(), query);
                int color = quest.completed() ? 0xFF79B06A : quest.canStart() ? 0xFFD6A25A : 0xFF5C4734;
                if (!query.isEmpty() && !questMatch && !dependencyMatch) {
                    color = 0x88443226;
                }

                int sx = dependencyNode.centerX();
                int sy = dependencyNode.centerY();
                int ex = node.centerX();
                int ey = node.centerY();
                int midX = sx + (ex - sx) / 2;

                drawHorizontalLine(graphics, sx, midX, sy, color);
                drawVerticalLine(graphics, midX, sy, ey, color);
                drawHorizontalLine(graphics, midX, ex, ey, color);
            }
        }
    }

    private void renderQuestNodes(GuiGraphics graphics, List<QuestDataSnapshot.QuestSnapshot> quests, Map<Long, NodeLayout> nodes, int mouseX, int mouseY) {
        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            NodeLayout node = nodes.get(quest.id());
            if (node == null) {
                continue;
            }

            Rect rect = node.rect();
            boolean hovered = rect.contains(mouseX, mouseY);
            renderAdvancementWidget(graphics, rect, quest.completed() || quest.hasUnclaimedRewards());

            int iconSize = Math.min(16, rect.height() - 10);
            int iconX = rect.x() + (rect.width() - iconSize) / 2;
            int iconY = rect.y() + (rect.height() - iconSize) / 2;
            quest.icon().draw(graphics, iconX, iconY, iconSize, iconSize);

            clickTargets.add(new ClickTarget(rect, () -> {
                viewState.setViewedQuestId(quest.id());
                viewState.setDetailScroll(0D);
            }));

            visibleQuestNodes.put(quest.id(), node);
            if (hovered) {
                hoveredQuest = quest;
            }
        }
    }

    private void renderAdvancementWidget(GuiGraphics graphics, Rect rect, boolean obtained) {
        int v = obtained ? ADVANCEMENT_WIDGET_OBTAINED_V : ADVANCEMENT_WIDGET_UNOBTAINED_V;
        graphics.blit(
                ADVANCEMENT_WIDGETS_TEXTURE,
                rect.x(),
                rect.y(),
                rect.width(),
                rect.height(),
                ADVANCEMENT_WIDGET_U,
                v,
                ADVANCEMENT_WIDGET_SIZE,
                ADVANCEMENT_WIDGET_SIZE,
                256,
                256
        );
    }

    private void renderHoverSummary(GuiGraphics graphics, QuestDataSnapshot.QuestSnapshot quest, int mouseX, int mouseY) {
        int width = Math.min(180, this.width - 24);
        List<FormattedCharSequence> summary = summarizeQuest(quest, width - 18, 4);
        int height = 22 + summary.size() * 10;
        int x = mouseX + 12;
        int y = mouseY - 8;
        if (x + width > this.width - 8) {
            x = mouseX - width - 12;
        }
        if (y + height > this.height - 8) {
            y = this.height - height - 8;
        }
        if (y < 8) {
            y = 8;
        }
        Rect rect = new Rect(x, y, width, height);

        drawPanel(graphics, rect, 0xF02C2116, 0xF018120C, 0xFFE2B66F);
        graphics.drawString(font, trim(quest.title(), width - 14), x + 7, y + 6, 0xFFF8EFD9, false);

        int lineY = y + 18;
        for (FormattedCharSequence line : summary) {
            graphics.drawString(font, line, x + 7, lineY, 0xFFEADAC0);
            lineY += 10;
        }
    }

    private void renderQuestDetailModal(GuiGraphics graphics, QuestDataSnapshot.QuestSnapshot quest, int mouseX, int mouseY) {
        graphics.fill(0, 0, width, height, 0xA0140E0A);

        DetailLayout layout = buildDetailLayout(quest);
        Rect rect = layout.rect();
        Rect body = layout.bodyRect();

        drawPanel(graphics, rect, 0xFF2B1D14, 0xFF18110D, 0xFFE0B46A);
        graphics.blit(ADVANCEMENT_WINDOW_TEXTURE, rect.x() + 4, rect.y() + 4, 0, 0, 252, 140, 256, 256);
        graphics.fill(rect.x() + 4, rect.y() + 4, rect.maxX() - 4, rect.maxY() - 4, 0xDD1D140E);

        quest.icon().draw(graphics, rect.x() + 12, rect.y() + 14, 22, 22);
        graphics.drawString(font, trim(quest.title(), rect.width() - 140), rect.x() + 42, rect.y() + 13, 0xFFF9EED9, false);
        graphics.drawString(font, Component.literal(statusLabel(quest)), rect.x() + 42, rect.y() + 26, 0xFFD8BB95, false);

        int chipY = rect.y() + 12;
        int closeX = rect.maxX() - 24;
        int vanillaX = closeX - 64;
        int pinX = vanillaX - 56;
        renderHeaderChip(graphics, pinX, chipY, 48, 16, quest.pinned() ? "Unpin" : "Pin", 0xFF5C3F28);
        renderHeaderChip(graphics, vanillaX, chipY, 56, 16, "Vanilla", 0xFF6B452A);
        renderHeaderChip(graphics, closeX, chipY, 14, 16, "x", 0xFF6E3426);

        clickTargets.add(new ClickTarget(new Rect(pinX, chipY, 48, 16), this::toggleCurrentQuestPin));
        clickTargets.add(new ClickTarget(new Rect(vanillaX, chipY, 56, 16), this::openVanillaSelected));
        clickTargets.add(new ClickTarget(new Rect(closeX, chipY, 14, 16), this::closeViewedQuest));

        graphics.enableScissor(body.x(), body.y(), body.maxX(), body.maxY());
        int contentY = body.y() + 4 - (int) viewState.getDetailScroll();

        for (FormattedCharSequence line : layout.subtitleLines()) {
            graphics.drawString(font, line, body.x(), contentY, 0xFFD6B791);
            contentY += 10;
        }
        if (!layout.subtitleLines().isEmpty()) {
            contentY += 6;
        }
        for (FormattedCharSequence line : layout.descriptionLines()) {
            graphics.drawString(font, line, body.x(), contentY, 0xFFF0E1C3);
            contentY += 10;
        }
        contentY += 10;

        graphics.drawString(font, Component.literal("Requirements"), body.x(), contentY, 0xFFF7E8CA, false);
        contentY += 16;
        for (QuestDataSnapshot.TaskSnapshot task : quest.tasks()) {
            contentY = renderTaskRow(graphics, body.x(), body.width(), task, quest, mouseX, mouseY, contentY);
        }

        contentY += 6;
        graphics.drawString(font, Component.literal("Rewards"), body.x(), contentY, 0xFFF7E8CA, false);
        contentY += 16;
        for (QuestDataSnapshot.RewardSnapshot reward : quest.rewards()) {
            contentY = renderRewardRow(graphics, body.x(), body.width(), reward, quest, mouseX, mouseY, contentY);
        }
        graphics.disableScissor();

        if (layout.contentHeight() > body.height()) {
            int step = Math.max(64, body.height() - 36);
            int arrowX = rect.maxX() - 20;
            int upY = rect.y() + MODAL_HEADER_HEIGHT + 4;
            int downY = rect.maxY() - 20;
            boolean canUp = viewState.getDetailScroll() > 0D;
            boolean canDown = viewState.getDetailScroll() < layout.contentHeight() - body.height();

            renderArrowButton(graphics, arrowX, upY, true, canUp);
            renderArrowButton(graphics, arrowX, downY, false, canDown);

            if (canUp) {
                clickTargets.add(new ClickTarget(new Rect(arrowX - 2, upY - 2, 16, 16),
                        () -> viewState.setDetailScroll(clampScroll(viewState.getDetailScroll() - step, layout.contentHeight(), body.height()))));
            }
            if (canDown) {
                clickTargets.add(new ClickTarget(new Rect(arrowX - 2, downY - 2, 16, 16),
                        () -> viewState.setDetailScroll(clampScroll(viewState.getDetailScroll() + step, layout.contentHeight(), body.height()))));
            }
        }
    }

    private int renderTaskRow(GuiGraphics graphics, int x, int width, QuestDataSnapshot.TaskSnapshot task, QuestDataSnapshot.QuestSnapshot quest, int mouseX, int mouseY, int y) {
        Rect rect = new Rect(x, y, width, 30);
        boolean hovered = rect.contains(mouseX, mouseY);
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), hovered ? 0xFF4C3424 : 0xFF33251C);
        drawInsetBorder(graphics, rect, hovered ? 0xFFB48752 : 0xFF5B4331, 0xAA140F0B);

        task.icon().draw(graphics, rect.x() + 6, rect.y() + 7, 16, 16);
        graphics.drawString(font, trim(task.title(), rect.width() - 100), rect.x() + 28, rect.y() + 6, 0xFFF7EBD5, false);
        graphics.drawString(font, task.progressText(), rect.x() + 28, rect.y() + 17, 0xFFD1B48C, false);

        String actionText = switch (task.interactionMode()) {
            case SUBMIT -> task.canInteract() ? "Submit" : "Ready";
            case VANILLA_FALLBACK -> "Vanilla";
            case READ_ONLY -> task.completed() ? "Done" : "Track";
        };
        int actionWidth = task.interactionMode() == TaskInteractionMode.VANILLA_FALLBACK ? 58 : 50;
        int actionX = rect.maxX() - actionWidth - 8;
        int actionColor = task.interactionMode() == TaskInteractionMode.SUBMIT && task.canInteract() ? 0xFFB97A34
                : task.interactionMode() == TaskInteractionMode.VANILLA_FALLBACK ? 0xFF8C5030
                : 0xFF5B4637;
        graphics.fill(actionX, rect.y() + 5, actionX + actionWidth, rect.y() + 24, actionColor);
        graphics.drawCenteredString(font, Component.literal(actionText), actionX + actionWidth / 2, rect.y() + 11, 0xFFF8ECD6);

        if (task.interactionMode() == TaskInteractionMode.SUBMIT && task.canInteract()) {
            clickTargets.add(new ClickTarget(new Rect(actionX, rect.y() + 5, actionWidth, 19), () -> {
                Task liveTask = resolveTask(task.id());
                if (liveTask != null) {
                    actionRouter.submitTask(liveTask);
                }
            }));
        } else if (task.interactionMode() == TaskInteractionMode.VANILLA_FALLBACK) {
            clickTargets.add(new ClickTarget(new Rect(actionX, rect.y() + 5, actionWidth, 19), () -> {
                Quest liveQuest = resolveQuest(quest.id());
                if (liveQuest != null) {
                    actionRouter.openVanillaForQuest(liveQuest);
                }
            }));
        }

        return y + 36;
    }

    private int renderRewardRow(GuiGraphics graphics, int x, int width, QuestDataSnapshot.RewardSnapshot rewardSnapshot, QuestDataSnapshot.QuestSnapshot quest, int mouseX, int mouseY, int y) {
        Rect rect = new Rect(x, y, width, 30);
        boolean hovered = rect.contains(mouseX, mouseY);
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), hovered ? 0xFF4B3524 : 0xFF33251C);
        drawInsetBorder(graphics, rect, hovered ? 0xFFB48752 : 0xFF5B4331, 0xAA140F0B);

        rewardSnapshot.icon().draw(graphics, rect.x() + 6, rect.y() + 7, 16, 16);
        graphics.drawString(font, trim(rewardSnapshot.title(), rect.width() - 104), rect.x() + 28, rect.y() + 6, 0xFFF7EBD5, false);
        graphics.drawString(font, rewardSnapshot.statusText(), rect.x() + 28, rect.y() + 17, 0xFFD1B48C, false);

        String actionText = switch (rewardSnapshot.interactionMode()) {
            case CLAIM -> rewardSnapshot.canClaim() ? "Claim" : rewardSnapshot.claimed() ? "Claimed" : "Locked";
            case CHOICE -> rewardSnapshot.canClaim() ? "Choose" : rewardSnapshot.claimed() ? "Claimed" : "Locked";
            case VANILLA_FALLBACK -> "Vanilla";
        };
        int actionWidth = rewardSnapshot.interactionMode() == RewardInteractionMode.VANILLA_FALLBACK ? 58 : 52;
        int actionX = rect.maxX() - actionWidth - 8;
        int actionColor = rewardSnapshot.canClaim() ? 0xFFB97A34
                : rewardSnapshot.interactionMode() == RewardInteractionMode.VANILLA_FALLBACK ? 0xFF8C5030
                : 0xFF5B4637;
        graphics.fill(actionX, rect.y() + 5, actionX + actionWidth, rect.y() + 24, actionColor);
        graphics.drawCenteredString(font, Component.literal(actionText), actionX + actionWidth / 2, rect.y() + 11, 0xFFF8ECD6);

        if (rewardSnapshot.interactionMode() == RewardInteractionMode.CLAIM && rewardSnapshot.canClaim()) {
            clickTargets.add(new ClickTarget(new Rect(actionX, rect.y() + 5, actionWidth, 19), () -> {
                Reward liveReward = resolveReward(rewardSnapshot.id());
                if (liveReward != null) {
                    actionRouter.claimReward(liveReward, true);
                }
            }));
        } else if (rewardSnapshot.interactionMode() == RewardInteractionMode.CHOICE && rewardSnapshot.canClaim()) {
            clickTargets.add(new ClickTarget(new Rect(actionX, rect.y() + 5, actionWidth, 19), () -> {
                Reward liveReward = resolveReward(rewardSnapshot.id());
                if (liveReward instanceof ChoiceReward choiceReward) {
                    minecraft.setScreen(new ChoiceRewardSelectScreen(this, choiceReward, actionRouter));
                }
            }));
        } else if (rewardSnapshot.interactionMode() == RewardInteractionMode.VANILLA_FALLBACK) {
            clickTargets.add(new ClickTarget(new Rect(actionX, rect.y() + 5, actionWidth, 19), () -> {
                Quest liveQuest = resolveQuest(quest.id());
                if (liveQuest != null) {
                    actionRouter.openVanillaForQuest(liveQuest);
                }
            }));
        }

        return y + 36;
    }

    private void renderHeaderChip(GuiGraphics graphics, int x, int y, int width, int height, String label, int fill) {
        graphics.fill(x, y, x + width, y + height, fill);
        graphics.drawCenteredString(font, Component.literal(label), x + width / 2, y + 4, 0xFFF8ECD6);
    }

    private void renderArrowButton(GuiGraphics graphics, int x, int y, boolean up, boolean active) {
        int fill = active ? 0xFF714826 : 0xFF3A291C;
        graphics.fill(x, y, x + 12, y + 12, fill);
        graphics.drawCenteredString(font, Component.literal(up ? "^" : "v"), x + 6, y + 2, active ? 0xFFF7EAD2 : 0xFF9D7C57);
    }

    private Map<Long, NodeLayout> buildNodeLayouts(List<QuestDataSnapshot.QuestSnapshot> quests, Rect canvas) {
        Map<Long, NodeLayout> nodes = new HashMap<>();
        if (quests.isEmpty()) {
            return nodes;
        }

        double spacingX = NODE_SPACING_X;
        double spacingY = NODE_SPACING_Y;
        int nodeWidth = NODE_BASE_WIDTH;
        int nodeHeight = NODE_BASE_HEIGHT;

        double minX = quests.stream().mapToDouble(QuestDataSnapshot.QuestSnapshot::x).min().orElse(0D);
        double maxX = quests.stream().mapToDouble(QuestDataSnapshot.QuestSnapshot::x).max().orElse(0D);
        double minY = quests.stream().mapToDouble(QuestDataSnapshot.QuestSnapshot::y).min().orElse(0D);
        double maxY = quests.stream().mapToDouble(QuestDataSnapshot.QuestSnapshot::y).max().orElse(0D);

        double centerX = (minX + maxX) / 2D;
        double centerY = (minY + maxY) / 2D;

        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            int x = Mth.floor(canvas.centerX() + (quest.x() - centerX) * spacingX + viewState.getTreePanX() - nodeWidth / 2D);
            int y = Mth.floor(canvas.centerY() + (quest.y() - centerY) * spacingY + viewState.getTreePanY() - nodeHeight / 2D);
            nodes.put(quest.id(), new NodeLayout(quest, new Rect(x, y, nodeWidth, nodeHeight)));
        }

        return nodes;
    }

    private DetailLayout buildDetailLayout(QuestDataSnapshot.QuestSnapshot quest) {
        int desiredWidth = Math.max(
                Math.max(font.width(quest.title()), font.width(quest.subtitle())),
                flattenDescription(quest.description(), MODAL_MAX_WIDTH - 56).stream().mapToInt(font::width).max().orElse(0)
        ) + 84;
        int modalWidth = Mth.clamp(desiredWidth, MODAL_MIN_WIDTH, Math.min(MODAL_MAX_WIDTH, width - 40));

        List<FormattedCharSequence> subtitleLines = quest.hiddenDetails()
                ? font.split(Component.literal("Details stay hidden until this quest becomes available."), modalWidth - 44)
                : font.split(quest.subtitle().getString().isEmpty() ? Component.empty() : quest.subtitle(), modalWidth - 44);
        List<FormattedCharSequence> descriptionLines = quest.hiddenDetails()
                ? List.of()
                : flattenDescription(quest.description(), modalWidth - 44);

        int contentHeight = subtitleLines.size() * 10;
        if (!subtitleLines.isEmpty()) {
            contentHeight += 6;
        }
        contentHeight += descriptionLines.size() * 10 + 10;
        contentHeight += 16 + quest.tasks().size() * 36;
        contentHeight += 22;
        contentHeight += 16 + quest.rewards().size() * 36;
        contentHeight += 12;

        int maxHeight = height - MODAL_MARGIN;
        int modalHeight = Math.min(MODAL_HEADER_HEIGHT + contentHeight + 18, maxHeight);
        Rect rect = new Rect((width - modalWidth) / 2, (height - modalHeight) / 2, modalWidth, modalHeight);
        Rect bodyRect = new Rect(rect.x() + 16, rect.y() + MODAL_HEADER_HEIGHT + 8, rect.width() - 36, rect.height() - MODAL_HEADER_HEIGHT - 20);

        viewState.setDetailScroll(clampScroll(viewState.getDetailScroll(), contentHeight, bodyRect.height()));
        return new DetailLayout(rect, bodyRect, subtitleLines, descriptionLines, contentHeight);
    }

    private List<FormattedCharSequence> summarizeQuest(QuestDataSnapshot.QuestSnapshot quest, int width, int maxLines) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        if (!quest.subtitle().getString().isEmpty()) {
            lines.addAll(font.split(quest.subtitle(), width));
        } else if (!quest.description().isEmpty()) {
            lines.addAll(font.split(quest.description().get(0), width));
        }

        if (lines.isEmpty()) {
            lines.addAll(font.split(Component.literal(quest.tasks().size() + " requirements, " + quest.rewards().size() + " rewards"), width));
        }
        return lines.subList(0, Math.min(maxLines, lines.size()));
    }

    private List<FormattedCharSequence> flattenDescription(List<Component> description, int maxWidth) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        for (Component component : description) {
            lines.addAll(font.split(component, maxWidth));
        }
        if (lines.isEmpty()) {
            lines.addAll(font.split(Component.literal("No description"), maxWidth));
        }
        return lines;
    }

    private boolean questMatchesQuery(QuestDataSnapshot.QuestSnapshot quest, String query) {
        String normalizedTitle = quest.title().getString().toLowerCase(Locale.ROOT);
        String normalizedSubtitle = quest.subtitle().getString().toLowerCase(Locale.ROOT);
        return normalizedTitle.contains(query) || normalizedSubtitle.contains(query);
    }

    private String normalizedQuery() {
        return viewState.getSearchText().trim().toLowerCase(Locale.ROOT);
    }

    private void focusCurrentChapter(boolean force) {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        QuestDataSnapshot.ChapterSnapshot chapter = snapshot.findChapter(viewState.getSelectedChapterId());
        Rect canvas = treeCanvasRect(treeRect(frameRect()));

        if (!force && chapter != null && chapter.id() == focusedChapterId) {
            return;
        }
        focusedChapterId = chapter == null ? Long.MIN_VALUE : chapter.id();
        if (chapter == null || chapter.quests().isEmpty()) {
            viewState.setTreePanX(0D);
            viewState.setTreePanY(0D);
            return;
        }
        viewState.setTreePanX(0D);
        viewState.setTreePanY(0D);
    }

    private void refreshState() {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        if (snapshot.chapters().isEmpty()) {
            return;
        }

        QuestDataSnapshot.ChapterSnapshot chapter = snapshot.findChapter(viewState.getSelectedChapterId());
        if (chapter == null) {
            chapter = snapshot.chapters().get(0);
            viewState.setSelectedChapterId(chapter.id());
        }

        if (viewState.getViewedQuestId() != 0L && snapshot.findQuest(viewState.getViewedQuestId()) == null) {
            closeViewedQuest();
        }

        if (focusedChapterId != chapter.id()) {
            focusCurrentChapter(true);
        }
    }

    private void refreshButtons() {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        QuestDataSnapshot.QuestSnapshot selectedQuest = getSelectedQuestSnapshot(snapshot);
        pinButton.active = selectedQuest != null;
        pinButton.setMessage(Component.literal(selectedQuest != null && selectedQuest.pinned() ? "Unpin Quest" : "Pin Quest"));
        vanillaButton.setMessage(Component.literal(ClientQuestFile.exists() && ClientQuestFile.INSTANCE.canEdit() ? "Vanilla Editor" : "Open Vanilla"));
        claimAllButton.active = snapshot.chapters().stream()
                .flatMap(chapter -> chapter.quests().stream())
                .flatMap(quest -> quest.rewards().stream())
                .anyMatch(QuestDataSnapshot.RewardSnapshot::canClaim);
    }

    private void syncWithVanillaRequest() {
        if (!openContext.fromVanillaScreen() || !ClientQuestFile.exists()) {
            return;
        }
        if (viewState.getViewedQuestId() != 0L) {
            return;
        }

        ClientQuestFile.INSTANCE.getQuestScreen()
                .map(QuestScreen::getViewedQuest)
                .filter(quest -> quest != null)
                .ifPresent(quest -> {
                    viewState.setViewedQuestId(quest.getId());
                    viewState.setSelectedChapterId(quest.getChapter().getId());
                    viewState.setDetailScroll(0D);
                });
    }

    private void openVanillaSelected() {
        Quest quest = resolveQuest(viewState.getViewedQuestId());
        if (quest != null) {
            actionRouter.openVanillaForQuest(quest);
        } else {
            actionRouter.openVanillaRoot();
        }
    }

    private void toggleCurrentQuestPin() {
        Quest quest = resolveQuest(viewState.getViewedQuestId());
        if (quest != null) {
            actionRouter.togglePin(quest);
        }
    }

    private void closeViewedQuest() {
        viewState.setViewedQuestId(0L);
        viewState.setDetailScroll(0D);
    }

    private Reward resolveReward(long id) {
        return ClientQuestFile.exists() ? ClientQuestFile.INSTANCE.getReward(id) : null;
    }

    private Task resolveTask(long id) {
        return ClientQuestFile.exists() ? ClientQuestFile.INSTANCE.getTask(id) : null;
    }

    private Quest resolveQuest(long id) {
        return ClientQuestFile.exists() ? ClientQuestFile.INSTANCE.getQuest(id) : null;
    }

    private QuestDataSnapshot.QuestSnapshot getSelectedQuestSnapshot(QuestDataSnapshot snapshot) {
        return snapshot.findQuest(viewState.getViewedQuestId());
    }

    private boolean handleClickTargets(double mouseX, double mouseY) {
        for (int i = clickTargets.size() - 1; i >= 0; i--) {
            ClickTarget target = clickTargets.get(i);
            if (target.contains(mouseX, mouseY)) {
                target.action().run();
                return true;
            }
        }
        return false;
    }

    private void drawPanel(GuiGraphics graphics, Rect rect, int topColor, int bodyColor, int borderColor) {
        graphics.fillGradient(rect.x(), rect.y(), rect.maxX(), rect.maxY(), topColor, bodyColor);
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.y() + 1, borderColor);
        graphics.fill(rect.x(), rect.maxY() - 1, rect.maxX(), rect.maxY(), borderColor);
        graphics.fill(rect.x(), rect.y(), rect.x() + 1, rect.maxY(), borderColor);
        graphics.fill(rect.maxX() - 1, rect.y(), rect.maxX(), rect.maxY(), borderColor);
    }

    private void drawInsetBorder(GuiGraphics graphics, Rect rect, int borderColor, int shadeColor) {
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.y() + 1, borderColor);
        graphics.fill(rect.x(), rect.maxY() - 1, rect.maxX(), rect.maxY(), shadeColor);
        graphics.fill(rect.x(), rect.y(), rect.x() + 1, rect.maxY(), borderColor);
        graphics.fill(rect.maxX() - 1, rect.y(), rect.maxX(), rect.maxY(), shadeColor);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int height, int progress, int backgroundColor, int fillColor) {
        graphics.fill(x, y, x + width, y + height, backgroundColor);
        graphics.fill(x, y, x + Math.max(1, width * progress / 100), y + height, fillColor);
    }

    private void drawHorizontalLine(GuiGraphics graphics, int x1, int x2, int y, int color) {
        int min = Math.min(x1, x2);
        int max = Math.max(x1, x2);
        graphics.fill(min, y - 1, max + 1, y + 1, color);
    }

    private void drawVerticalLine(GuiGraphics graphics, int x, int y1, int y2, int color) {
        int min = Math.min(y1, y2);
        int max = Math.max(y1, y2);
        graphics.fill(x - 1, min, x + 1, max + 1, color);
    }

    private double clampScroll(double scroll, int contentHeight, int visibleHeight) {
        return Mth.clamp(scroll, 0D, Math.max(0, contentHeight - visibleHeight));
    }

    private Component trim(Component component, int width) {
        return Component.literal(font.plainSubstrByWidth(component.getString(), Math.max(8, width)));
    }

    private String statusLabel(QuestDataSnapshot.QuestSnapshot quest) {
        if (quest.completed()) {
            return quest.hasUnclaimedRewards() ? "Completed - rewards waiting" : "Completed";
        }
        if (quest.canStart()) {
            return quest.started() ? "In Progress" : "Available";
        }
        return "Locked";
    }

    private Rect frameRect() {
        return new Rect((width - BACKGROUND_WIDTH) / 2, (height - BACKGROUND_HEIGHT) / 2, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    private Rect headerRect(Rect frame) {
        return new Rect(frame.x() + 1, frame.y() + 1, frame.width() - 2, HEADER_HEIGHT);
    }

    private Rect chapterRailRect(Rect frame) {
        return new Rect(frame.x() + 8, frame.y() + HEADER_HEIGHT + 10, CHAPTER_WIDTH, frame.height() - HEADER_HEIGHT - 18);
    }

    private Rect treeRect(Rect frame) {
        return new Rect(frame.x() + TREE_X, frame.y() + TREE_Y, TREE_WIDTH, TREE_HEIGHT);
    }

    private Rect treeCanvasRect(Rect rect) {
        return rect;
    }

    private record ClickTarget(Rect rect, Runnable action) {
        boolean contains(double mouseX, double mouseY) {
            return rect.contains(mouseX, mouseY);
        }
    }

    private record NodeLayout(QuestDataSnapshot.QuestSnapshot quest, Rect rect) {
        int centerX() {
            return rect.centerX();
        }

        int centerY() {
            return rect.centerY();
        }
    }

    private record DetailLayout(
            Rect rect,
            Rect bodyRect,
            List<FormattedCharSequence> subtitleLines,
            List<FormattedCharSequence> descriptionLines,
            int contentHeight
    ) {
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

        int centerY() {
            return y + height / 2;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= maxX() && mouseY >= y && mouseY <= maxY();
        }
    }
}
