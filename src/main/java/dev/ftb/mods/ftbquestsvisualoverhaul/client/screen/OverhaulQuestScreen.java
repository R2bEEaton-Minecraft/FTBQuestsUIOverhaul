package dev.ftb.mods.ftbquestsvisualoverhaul.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ui.SelectImageResourceScreen;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.ChoiceReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.QuestActionRouter;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.QuestDataController;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.QuestDataSnapshot;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.RewardInteractionMode;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.TaskInteractionMode;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestOpenContext;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestViewState;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.GameType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Quest screen that replicates the vanilla Minecraft advancements screen
 * as closely as possible for the quest tree area.
 *
 * References vanilla classes: AdvancementsScreen, AdvancementTab, AdvancementWidget
 */
public class OverhaulQuestScreen extends Screen {
    // --- Custom background texture ---
    private static final ResourceLocation QUESTS_BACKGROUND_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/gui/quests_background.png");
    private static final ResourceLocation CHAPTER_BUTTON_INACTIVE_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/gui/quest_line_button_inactive.png");
    private static final ResourceLocation CHAPTER_BUTTON_HOVER_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/gui/quest_line_button_hover.png");
    private static final ResourceLocation CHAPTER_BUTTON_ACTIVE_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/gui/quest_line_button_active.png");
    private static final int BACKGROUND_WIDTH = 392;
    private static final int BACKGROUND_HEIGHT = 217;
    private static final int BACKGROUND_TEXTURE_WIDTH = 512;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    private static final int CHAPTER_BUTTON_TEXTURE_WIDTH = 98;
    private static final int CHAPTER_BUTTON_TEXTURE_HEIGHT = 40;

    // --- Tree panel area within the background texture ---
    private static final int TREE_X = 119;
    private static final int TREE_Y = 19;
    private static final int TREE_WIDTH = 248;
    private static final int TREE_HEIGHT = 173;
    private static final int CHAPTER_SELECTOR_X = 15;
    private static final int CHAPTER_SELECTOR_Y = 13;
    private static final int CHAPTER_SELECTOR_BOTTOM_PADDING = 18;
    private static final int CHAPTER_BUTTON_ACTIVE_WIDTH = 98;
    private static final int CHAPTER_BUTTON_REGULAR_WIDTH = 86;
    private static final int CHAPTER_BUTTON_HEIGHT = 19;
    private static final int CHAPTER_BUTTON_TEXTURE_Y_OFFSET = 8;
    private static final int CHAPTER_SELECTOR_ENTRY_SPACING = 2;
    private static final int CHAPTER_SELECTOR_SCROLL_STEP = CHAPTER_BUTTON_HEIGHT + CHAPTER_SELECTOR_ENTRY_SPACING;
    private static final int CHAPTER_SELECTOR_ICON_SIZE = 13;
    private static final int CHAPTER_SELECTOR_ICON_X_OFFSET = 5;
    private static final int CHAPTER_SELECTOR_TEXT_X_OFFSET = 21;
    private static final int CHAPTER_SELECTOR_TEXT_RIGHT_PADDING = 5;
    private static final int CHAPTER_SCROLLBAR_WIDTH = 1;
    private static final int CHAPTER_SCROLLBAR_GAP = 2;
    private static final int CHAPTER_SCROLLBAR_HITBOX_WIDTH = 5;
    private static final int CHAPTER_SCROLLBAR_MIN_THUMB_HEIGHT = 11;
    private static final int CHAPTER_SELECTOR_BOTTOM_SHADOW_HEIGHT = 13;
    private static final float CHAPTER_SELECTOR_TEXT_SCALE = 0.6F;
    private static final float CHAPTER_GROUP_TEXT_SCALE = 0.65F;
    private static final int CHAPTER_GROUP_HEADER_HEIGHT = 15;
    private static final int ACTIVE_CHAPTER_TITLE_TOP_PADDING = 12;
    private static final int ACTIVE_CHAPTER_TITLE_BOTTOM_PADDING = 4;
    private static final int ACTIVE_CHAPTER_TITLE_SIDE_PADDING = 5;
    private static final int ACTIVE_CHAPTER_TITLE_ICON_SIZE = 11;
    private static final int ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING = 4;
    private static final float ACTIVE_CHAPTER_TITLE_ICON_Y_OFFSET = 2.0F;

    // --- Vanilla advancement textures ---
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    private static final ResourceLocation BUTTONS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation OAK_PLANKS_TEXTURE = new ResourceLocation("minecraft", "textures/block/oak_planks.png");
    private static final ResourceLocation FTB_QUEST_LOCKED_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/quest_locked.png");

    // --- Node spacing (based on AdvancementWidget, with wider horizontal gaps) ---
    // Vanilla uses displayX * 28 and displayY * 27, where displayX/Y are 0,1,2,...
    // FTB quest coordinates use a different scale, so we normalize to vanilla units
    // first (via xStepSize/yStepSize) before multiplying by these constants.
    private static final int NODE_SPACING_X = 42;
    private static final int NODE_SPACING_Y = 27;
    private static final int WIDGET_WIDTH = 26;
    private static final int WIDGET_HEIGHT = 26;
    private static final int WIDGET_ICON_X = 8;
    private static final int WIDGET_ICON_Y = 5;

    // --- Vanilla advancement tab dimensions (from AdvancementTabType.ABOVE) ---
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 32;

    // --- Vanilla tooltip constants (from AdvancementWidget) ---
    private static final int TITLE_PADDING_LEFT = 3;
    private static final int TITLE_PADDING_RIGHT = 5;
    private static final int TITLE_X = 32;
    private static final int TITLE_Y = 9;
    private static final int TITLE_MAX_WIDTH = 163;
    private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};

    // --- Detail modal constants ---
    private static final int MODAL_MIN_WIDTH = 168;
    private static final int MODAL_MAX_WIDTH = 188;
    private static final int MODAL_HEADER_HEIGHT = 34;
    private static final int MODAL_FOOTER_HEIGHT = 24;
    private static final int MODAL_MARGIN = 28;
    private static final int FOOTER_BUTTON_HEIGHT = 15;
    private static final float MODAL_TEXT_SCALE = 0.8F;

    private final QuestOpenContext openContext;
    private final QuestActionRouter actionRouter = new QuestActionRouter();
    private final QuestViewState viewState;
    private final List<ClickTarget> clickTargets = new ArrayList<>();

    private final Map<Long, NodeLayout> visibleQuestNodes = new HashMap<>();
    private QuestDataSnapshot.QuestSnapshot hoveredQuest;
    private boolean isScrolling;
    private boolean chapterScrollbarDragging;
    private long focusedChapterId = Long.MIN_VALUE;
    private float fade;
    private boolean centered;
    private int tabPage;
    private int maxPages;
    private double chapterScroll;

    // Scroll bounds tracking (from AdvancementTab)
    private int minNodeX = Integer.MAX_VALUE;
    private int minNodeY = Integer.MAX_VALUE;
    private int maxNodeX = Integer.MIN_VALUE;
    private int maxNodeY = Integer.MIN_VALUE;

    // Normalized step sizes: minimum non-zero gap between quest positions,
    // so we scale FTB coords to vanilla's 0,1,2,... units before multiplying by 28/27.
    private double xStepSize = 1.0;
    private double yStepSize = 1.0;

    public OverhaulQuestScreen(QuestOpenContext openContext) {
        super(Component.literal("Quests"));
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
        centered = false;
        updateTabPages();
    }

    @Override
    public void tick() {
        super.tick();
        // FTB Quests pushes client progress updates into TeamData, but it only refreshes
        // the vanilla QuestScreen widgets. Rebuild our cached snapshot while this screen
        // is open so inventory-detected task completion and reward claimability stay in sync.
        QuestDataController.markDirty();
    }

    @Override
    public void removed() {
        QuestDataController.saveViewState(viewState);
        CursorType.set(null);
        super.removed();
    }

    @Override
    public void onClose() {
        QuestDataController.saveViewState(viewState);
        super.onClose();
    }

    // ---- Input handling (matching vanilla AdvancementsScreen) ----

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

        // If a detail modal is open, handle clicks for it first
        if (selectedQuest != null) {
            if (button == 0 && handleClickTargets(mouseX, mouseY)) {
                return true;
            }
            DetailLayout layout = buildDetailLayout(selectedQuest);
            if (!layout.rect().contains(mouseX, mouseY)) {
                closeViewedQuest();
            }
            return true;
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 0 && handleChapterScrollbarClick(snapshot, mouseX, mouseY)) {
            return true;
        }

        if (button == 0 && handleClickTargets(mouseX, mouseY)) {
            return true;
        }

        if (!frameRect().contains(mouseX, mouseY)) {
            onClose();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isScrolling = false;
            chapterScrollbarDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /**
     * Matches vanilla AdvancementsScreen.mouseDragged exactly:
     * - Only button 0 (left click)
     * - First drag frame sets isScrolling=true but doesn't scroll
     * - Subsequent frames apply delta-based scrolling
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0) {
            isScrolling = false;
            chapterScrollbarDragging = false;
            return false;
        }

        if (viewState.getViewedQuestId() != 0L) {
            return false;
        }

        if (chapterScrollbarDragging) {
            updateChapterScrollFromMouse(mouseY, QuestDataController.getSnapshot());
            return true;
        }

        if (!isScrolling) {
            isScrolling = true;
        } else {
            scroll(dragX, dragY);
        }
        return true;
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
        Rect selectorViewport = chapterSelectorViewportRect();
        Rect scrollbarHitbox = chapterScrollbarHitbox(snapshot);
        if (selectorViewport.contains(mouseX, mouseY) || scrollbarHitbox != null && scrollbarHitbox.contains(mouseX, mouseY)) {
            scrollChapterSelector(-deltaY * CHAPTER_SELECTOR_SCROLL_STEP, snapshot);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, deltaY);
    }

    // ---- Scroll/Pan (matching vanilla AdvancementTab.scroll) ----

    private void scroll(double deltaX, double deltaY) {
        if (isCreativeControlsVisible() && viewState.isFreePan()) {
            viewState.setTreePanX(viewState.getTreePanX() + deltaX);
            viewState.setTreePanY(viewState.getTreePanY() + deltaY);
            return;
        }
        if (maxNodeX - minNodeX > TREE_WIDTH) {
            viewState.setTreePanX(Mth.clamp(viewState.getTreePanX() + deltaX, -(maxNodeX - TREE_WIDTH), 0.0D));
        }
        if (maxNodeY - minNodeY > TREE_HEIGHT) {
            viewState.setTreePanY(Mth.clamp(viewState.getTreePanY() + deltaY, -(maxNodeY - TREE_HEIGHT), 0.0D));
        }
    }

    // ---- Rendering ----

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderScreen(graphics, mouseX, mouseY, partialTick, true, true, true);
    }

    public void renderChoiceBackdrop(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderScreen(graphics, mouseX, mouseY, partialTick, false, false, false);
    }

    private void renderScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
                              boolean renderDetailModal, boolean renderDefaultButton, boolean updateCursor) {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        refreshState(snapshot);
        hoveredQuest = null;
        visibleQuestNodes.clear();
        clickTargets.clear();

        Rect frame = frameRect();
        int treeLeft = frame.x() + TREE_X;
        int treeTop = frame.y() + TREE_Y;

        // Dark backdrop + custom background texture
        this.renderBackground(graphics);
        graphics.fill(0, 0, width, height, 0xC0100B08);
        graphics.blit(QUESTS_BACKGROUND_TEXTURE, frame.x(), frame.y(), 0, 0,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT);

        QuestDataSnapshot.QuestSnapshot selectedQuest = getSelectedQuestSnapshot(snapshot);

        renderChapterSelector(graphics, snapshot, mouseX, mouseY, selectedQuest == null && updateCursor);

        // Render tree content inside the tree panel area (vanilla advancement style)
        renderInside(graphics, snapshot, mouseX, mouseY, treeLeft, treeTop);
        renderActiveChapterTitle(graphics, snapshot, treeLeft, treeTop);
        renderCreativeTreeControls(graphics, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Tooltips (rendered after everything else so they appear on top)
        renderTooltips(graphics, snapshot, mouseX, mouseY, treeLeft, treeTop);

        // Detail modal overlay (quest-specific, not in vanilla advancements)
        if (renderDetailModal && selectedQuest != null) {
            renderQuestDetailModal(graphics, selectedQuest, mouseX, mouseY);
        }

        if (renderDefaultButton && shouldShowDefaultViewButton()) {
            renderDefaultViewButton(graphics, mouseX, mouseY);
        }

        if (updateCursor) {
            updateCursor(snapshot, mouseX, mouseY, treeLeft, treeTop);
        }
    }

    /**
     * Renders the tree content area using vanilla AdvancementTab.drawContents approach:
     * Scissor clips to tree panel, tiles background, draws connections + widgets.
     */
    private void renderInside(GuiGraphics graphics, QuestDataSnapshot snapshot, int mouseX, int mouseY, int treeLeft, int treeTop) {
        QuestDataSnapshot.ChapterSnapshot chapter = snapshot.findChapter(viewState.getSelectedChapterId());
        if (chapter == null) {
            // Match vanilla: black fill + empty label
            graphics.fill(treeLeft, treeTop, treeLeft + TREE_WIDTH, treeTop + TREE_HEIGHT, -16777216);
            graphics.drawCenteredString(this.font, Component.translatable("advancements.empty"),
                    treeLeft + TREE_WIDTH / 2, treeTop + TREE_HEIGHT / 2 - 4, -1);
            return;
        }

        List<QuestDataSnapshot.QuestSnapshot> quests = chapter.quests();

        // Compute node bounds and auto-center (matching AdvancementTab logic)
        computeNodeBounds(quests);
        if (!centered && !quests.isEmpty()) {
            // Matching AdvancementTab: center = halfWidth - (max+min)/2
            viewState.setTreePanX((TREE_WIDTH / 2.0) - (maxNodeX + minNodeX) / 2.0);
            viewState.setTreePanY((TREE_HEIGHT / 2.0) - (maxNodeY + minNodeY) / 2.0);
            centered = true;
        }

        // Scissor to tree panel area (matching AdvancementTab.drawContents)
        graphics.enableScissor(treeLeft, treeTop, treeLeft + TREE_WIDTH, treeTop + TREE_HEIGHT);
        graphics.pose().pushPose();
        graphics.pose().translate((float) treeLeft, (float) treeTop, 0.0F);

        // Tile background (matching AdvancementTab: 16x16 tiles with scroll offset)
        ResourceLocation tileTexture = resolveTileTexture(viewState.getChapterTitleTexture(chapter.id()));
        if (tileTexture == null) {
            tileTexture = OAK_PLANKS_TEXTURE;
        }
        int scrollXInt = Mth.floor(viewState.getTreePanX());
        int scrollYInt = Mth.floor(viewState.getTreePanY());
        int tileOffsetX = scrollXInt % 16;
        int tileOffsetY = scrollYInt % 16;

        int tilesX = (TREE_WIDTH / 16) + 2;
        int tilesY = (TREE_HEIGHT / 16) + 2;
        for (int tx = -1; tx <= tilesX; ++tx) {
            for (int ty = -1; ty <= tilesY; ++ty) {
                graphics.blit(tileTexture, tileOffsetX + 16 * tx, tileOffsetY + 16 * ty, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        // Draw connections (matching AdvancementWidget.drawConnectivity)
        // First pass: black outline, second pass: white center
        drawAllConnections(graphics, quests, scrollXInt, scrollYInt, true);
        drawAllConnections(graphics, quests, scrollXInt, scrollYInt, false);

        // Draw widgets (matching AdvancementWidget.draw)
        drawAllWidgets(graphics, quests, scrollXInt, scrollYInt, mouseX - treeLeft, mouseY - treeTop);

        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private void renderChapterSelector(GuiGraphics graphics, QuestDataSnapshot snapshot, int mouseX, int mouseY, boolean interactive) {
        Rect viewportRect = chapterSelectorViewportRect();
        clampChapterScroll(snapshot);
        int y = viewportRect.y() - Mth.floor(chapterScroll);

        graphics.enableScissor(viewportRect.x(), viewportRect.y(), viewportRect.maxX(), viewportRect.maxY());

        for (QuestDataSnapshot.ChapterSnapshot chapter : snapshot.chapters()) {
            if (chapter.firstInGroup()) {
                Rect headerRect = new Rect(viewportRect.x(), y, viewportRect.width(), CHAPTER_GROUP_HEADER_HEIGHT);
                if (headerRect.intersects(viewportRect)) {
                    graphics.fill(headerRect.x(), headerRect.y() + 1, headerRect.maxX() - 4, headerRect.maxY() - 1, 0x77140E0C);
                    drawScaledString(graphics, trim(chapter.groupTitle(), headerRect.width() - 6, CHAPTER_GROUP_TEXT_SCALE),
                            headerRect.x() + 4, headerRect.y() + 3, 0xFFF0E4CB, CHAPTER_GROUP_TEXT_SCALE);
                }
                y += CHAPTER_GROUP_HEADER_HEIGHT;
            }

            boolean selected = chapter.id() == viewState.getSelectedChapterId();
            int buttonWidth = selected ? CHAPTER_BUTTON_ACTIVE_WIDTH : CHAPTER_BUTTON_REGULAR_WIDTH;
            int textureWidth = CHAPTER_BUTTON_TEXTURE_WIDTH;
            Rect buttonRect = new Rect(viewportRect.x(), y, buttonWidth, CHAPTER_BUTTON_HEIGHT);
            Rect textureRect = new Rect(viewportRect.x(), y - CHAPTER_BUTTON_TEXTURE_Y_OFFSET, textureWidth, CHAPTER_BUTTON_TEXTURE_HEIGHT);
            boolean visible = textureRect.intersects(viewportRect);
            boolean hovered = visible && buttonRect.contains(mouseX, mouseY);
            ResourceLocation texture = selected ? CHAPTER_BUTTON_ACTIVE_TEXTURE : hovered ? CHAPTER_BUTTON_HOVER_TEXTURE : CHAPTER_BUTTON_INACTIVE_TEXTURE;

            if (visible) {
                RenderSystem.enableBlend();
                graphics.blit(texture, textureRect.x(), textureRect.y(), 0, 0, textureRect.width(), textureRect.height(), textureWidth, CHAPTER_BUTTON_TEXTURE_HEIGHT);
                RenderSystem.disableBlend();

                int iconX = buttonRect.x() + CHAPTER_SELECTOR_ICON_X_OFFSET;
                int iconY = buttonRect.y() + Math.max(0, (buttonRect.height() - CHAPTER_SELECTOR_ICON_SIZE) / 2);
                chapter.icon().draw(graphics, iconX, iconY, CHAPTER_SELECTOR_ICON_SIZE, CHAPTER_SELECTOR_ICON_SIZE);

                float scaledTextHeight = font.lineHeight * CHAPTER_SELECTOR_TEXT_SCALE;
                int textLeft = buttonRect.x() + CHAPTER_SELECTOR_TEXT_X_OFFSET;
                int textY = Mth.floor(buttonRect.y() + (buttonRect.height() - scaledTextHeight) * 0.5F) + 2;
                int availableWidth = buttonRect.maxX() - textLeft - CHAPTER_SELECTOR_TEXT_RIGHT_PADDING;
                int textColor = selected ? 0xFFF2E8D6 : hovered ? 0xFFF6EAD0 : 0xFFE3D5BE;
                renderScrollingChapterLabel(graphics, chapter.title(), buttonRect, textLeft, textY, availableWidth, textColor, hovered || selected);
            }

            if (interactive && buttonRect.intersects(viewportRect)) {
                int clippedY = Math.max(buttonRect.y(), viewportRect.y());
                int clippedHeight = Math.min(buttonRect.maxY(), viewportRect.maxY()) - clippedY;
                if (clippedHeight > 0) {
                    clickTargets.add(new ClickTarget(new Rect(buttonRect.x(), clippedY, buttonRect.width(), clippedHeight),
                            () -> selectChapter(chapter.id())));
                }
            }

            y += CHAPTER_SELECTOR_SCROLL_STEP;
        }

        renderChapterSelectorBottomShadow(graphics, viewportRect, snapshot);
        graphics.disableScissor();
        renderChapterScrollbar(graphics, snapshot);
    }

    private void renderChapterSelectorBottomShadow(GuiGraphics graphics, Rect viewportRect, QuestDataSnapshot snapshot) {
        if (chapterScroll >= maxChapterScroll(snapshot)) {
            return;
        }

        int shadowTop = Math.max(viewportRect.y(), viewportRect.maxY() - CHAPTER_SELECTOR_BOTTOM_SHADOW_HEIGHT);
        RenderSystem.enableBlend();
        graphics.fillGradient(viewportRect.x(), shadowTop, viewportRect.maxX() - 10, viewportRect.maxY(), 0x00120D09, 0xD0120D09);
        RenderSystem.disableBlend();
    }

    private void renderActiveChapterTitle(GuiGraphics graphics, QuestDataSnapshot snapshot, int treeLeft, int treeTop) {
        QuestDataSnapshot.ChapterSnapshot chapter = snapshot.findChapter(viewState.getSelectedChapterId());
        if (chapter == null) {
            return;
        }
        Component title = chapter.title().copy();
        Component separator = Component.literal("-");

        Rect frame = frameRect();
        Rect titleBandRect = new Rect(
                treeLeft + ACTIVE_CHAPTER_TITLE_SIDE_PADDING,
                frame.y(),
                TREE_WIDTH - ACTIVE_CHAPTER_TITLE_SIDE_PADDING * 2,
                treeTop - frame.y()
        );
        if (titleBandRect.height() <= 0) {
            return;
        }

        int scaledTextHeight = Math.max(1, Math.round(font.lineHeight * CHAPTER_SELECTOR_TEXT_SCALE));
        int rowTextOffset = Mth.floor((CHAPTER_BUTTON_HEIGHT - scaledTextHeight) * 0.5F) + 2;
        int rowY = frame.y() + ACTIVE_CHAPTER_TITLE_TOP_PADDING - rowTextOffset;
        int textY = rowY + rowTextOffset;
        float iconY = rowY + Math.max(0, (CHAPTER_BUTTON_HEIGHT - ACTIVE_CHAPTER_TITLE_ICON_SIZE) / 2) + ACTIVE_CHAPTER_TITLE_ICON_Y_OFFSET;
        int separatorWidth = Math.round(font.width(separator) * CHAPTER_SELECTOR_TEXT_SCALE);
        int titleWidth = Math.round(font.width(title) * CHAPTER_SELECTOR_TEXT_SCALE);
        int totalWidth = ACTIVE_CHAPTER_TITLE_ICON_SIZE
                + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING
                + separatorWidth
                + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING
                + titleWidth
                + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING
                + separatorWidth
                + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING
                + ACTIVE_CHAPTER_TITLE_ICON_SIZE;
        int color = 0xFFF2E8D6;
        int drawX = titleBandRect.x() + Math.max(0, (titleBandRect.width() - totalWidth) / 2);

        if (totalWidth > titleBandRect.width()) {
            int overflow = totalWidth - titleBandRect.width();
            drawX -= Mth.floor(getMarqueeOffset(overflow));
        }
        graphics.enableScissor(titleBandRect.x(), titleBandRect.y(), titleBandRect.maxX(), titleBandRect.maxY());
        int cursorX = drawX;
        graphics.pose().pushPose();
        graphics.pose().translate(cursorX, iconY, 0F);
        chapter.icon().draw(graphics, 0, 0, ACTIVE_CHAPTER_TITLE_ICON_SIZE, ACTIVE_CHAPTER_TITLE_ICON_SIZE);
        graphics.pose().popPose();
        cursorX += ACTIVE_CHAPTER_TITLE_ICON_SIZE + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING;
        drawScaledString(graphics, separator, cursorX, textY, color, CHAPTER_SELECTOR_TEXT_SCALE);
        cursorX += separatorWidth + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING;
        drawScaledString(graphics, title, cursorX, textY, color, CHAPTER_SELECTOR_TEXT_SCALE);
        cursorX += titleWidth + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING;
        drawScaledString(graphics, separator, cursorX, textY, color, CHAPTER_SELECTOR_TEXT_SCALE);
        cursorX += separatorWidth + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING;
        graphics.pose().pushPose();
        graphics.pose().translate(cursorX, iconY, 0F);
        chapter.icon().draw(graphics, 0, 0, ACTIVE_CHAPTER_TITLE_ICON_SIZE, ACTIVE_CHAPTER_TITLE_ICON_SIZE);
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private void renderCreativeTreeControls(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isCreativeControlsVisible()) {
            return;
        }

        Rect frame = frameRect();
        Rect freePanButton = new Rect(frame.x() + TREE_X + 1, frame.y() + TREE_Y + 2, 72, 11);
        Rect titleButton = new Rect(frame.x() + TREE_X + TREE_WIDTH - 72, frame.y() + TREE_Y + 2, 68, 11);
        renderSmallOverlayButton(graphics, freePanButton, viewState.isFreePan() ? "Free Pan: On" : "Free Pan: Off",
                freePanButton.contains(mouseX, mouseY), viewState.isFreePan() ? 0xFFE6D176 : 0xFFE68E7C);
        renderSmallOverlayButton(graphics, titleButton, "Change Tile", titleButton.contains(mouseX, mouseY), 0xFFF2E8D6);

        clickTargets.add(new ClickTarget(freePanButton, () -> viewState.setFreePan(!viewState.isFreePan())));
        clickTargets.add(new ClickTarget(titleButton, this::openChapterTitleTextureSelector));
    }

    private void renderSmallOverlayButton(GuiGraphics graphics, Rect rect, String label, boolean hovered, int textColor) {
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), hovered ? 0xCC3A2B1C : 0x99201510);
        drawInsetBorder(graphics, rect, hovered ? 0xE0C8A15C : 0xAA6F5535, 0xAA120D09);
        drawCenteredScaledString(graphics, Component.literal(label), rect.centerX(), rect.y() + 3, textColor, 0.45F);
    }

    private void openChapterTitleTextureSelector() {
        long chapterId = viewState.getSelectedChapterId();
        ImageResourceConfig imageConfig = new ImageResourceConfig();
        ResourceLocation currentTexture = viewState.getChapterTitleTexture(chapterId);
        if (currentTexture != null) {
            imageConfig.setValue(currentTexture);
        }
        new SelectImageResourceScreen(imageConfig, accepted -> {
            if (accepted) {
                viewState.setChapterTitleTexture(chapterId, imageConfig.isEmpty() ? null : imageConfig.getValue());
                QuestDataController.saveViewState(viewState);
            }
            minecraft.setScreen(this);
        }).openGui();
    }

    private ResourceLocation resolveTileTexture(ResourceLocation selectedTexture) {
        if (selectedTexture == null) {
            return null;
        }

        String path = selectedTexture.getPath();
        if (path.startsWith("textures/") && path.endsWith(".png")) {
            return selectedTexture;
        }
        if (!path.endsWith(".png")) {
            return new ResourceLocation(selectedTexture.getNamespace(), "textures/" + path + ".png");
        }
        return new ResourceLocation(selectedTexture.getNamespace(), "textures/" + path);
    }

    /**
     * Renders chapter tabs along the top of the tree panel area,
     * using the vanilla advancement tab style (tabs.png texture).
     */
    private void renderTabs(GuiGraphics graphics, QuestDataSnapshot snapshot, int treeLeft, int treeTop, int mouseX, int mouseY) {
        List<QuestDataSnapshot.ChapterSnapshot> chapters = snapshot.chapters();
        if (chapters.size() <= 1) return;

        RenderSystem.enableBlend();

        // Draw tab backgrounds
        for (int i = 0; i < chapters.size(); i++) {
            if (getTabPage(i) == tabPage) {
                boolean selected = chapters.get(i).id() == viewState.getSelectedChapterId();
                drawTab(graphics, treeLeft, treeTop, i, selected);
            }
        }

        // Draw tab icons
        for (int i = 0; i < chapters.size(); i++) {
            if (getTabPage(i) == tabPage) {
                drawTabIcon(graphics, treeLeft, treeTop, i, chapters.get(i));
            }
        }
    }

    /**
     * Renders tooltips for hovered widgets and tabs.
     * Matches vanilla AdvancementTab.drawTooltips approach with fade overlay.
     */
    private void renderTooltips(GuiGraphics graphics, QuestDataSnapshot snapshot, int mouseX, int mouseY, int treeLeft, int treeTop) {
        if (viewState.getViewedQuestId() != 0L) {
            return;
        }

        QuestDataSnapshot.ChapterSnapshot chapter = snapshot.findChapter(viewState.getSelectedChapterId());
        if (chapter != null) {
            // Matching AdvancementsScreen.renderTooltips: translate to content origin at z=400
            graphics.pose().pushPose();
            graphics.pose().translate((float) treeLeft, (float) treeTop, 400.0F);
            RenderSystem.enableDepthTest();

            int relMouseX = mouseX - treeLeft;
            int relMouseY = mouseY - treeTop;

            // Fade overlay (matching AdvancementTab.drawTooltips)
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, -200.0F);
            graphics.fill(0, 0, TREE_WIDTH, TREE_HEIGHT, Mth.floor(this.fade * 255.0F) << 24);
            boolean foundHovered = false;

            if (relMouseX > 0 && relMouseX < TREE_WIDTH && relMouseY > 0 && relMouseY < TREE_HEIGHT) {
                int scrollXInt = Mth.floor(viewState.getTreePanX());
                int scrollYInt = Mth.floor(viewState.getTreePanY());

                for (QuestDataSnapshot.QuestSnapshot quest : chapter.quests()) {
                    int nodeX = getNodeX(quest);
                    int nodeY = getNodeY(quest);
                    int widgetX = scrollXInt + nodeX;
                    int widgetY = scrollYInt + nodeY;

                    if (relMouseX >= widgetX && relMouseX <= widgetX + WIDGET_WIDTH
                            && relMouseY >= widgetY && relMouseY <= widgetY + WIDGET_HEIGHT) {
                        foundHovered = true;
                        drawWidgetHover(graphics, quest, scrollXInt, scrollYInt, this.fade, treeLeft, treeTop);
                        break;
                    }
                }
            }

            graphics.pose().popPose();

            // Matching vanilla fade behavior
            if (foundHovered) {
                this.fade = Mth.clamp(this.fade + 0.02F, 0.0F, 0.3F);
            } else {
                this.fade = Mth.clamp(this.fade - 0.04F, 0.0F, 1.0F);
            }

            RenderSystem.disableDepthTest();
            graphics.pose().popPose();
        }

    }

    // ---- Node position calculations (matching AdvancementWidget) ----

    /**
     * Converts a quest's raw FTB x coordinate to a pixel position.
     * Normalizes by xStepSize so the minimum inter-node gap matches vanilla's
     * 0→1→2 integer unit scale, then multiplies by NODE_SPACING_X.
     */
    private int getNodeX(QuestDataSnapshot.QuestSnapshot quest) {
        return Mth.floor((quest.x() / xStepSize) * NODE_SPACING_X);
    }

    private int getNodeY(QuestDataSnapshot.QuestSnapshot quest) {
        return Mth.floor((quest.y() / yStepSize) * NODE_SPACING_Y);
    }

    /**
     * Compute min/max node bounds (matching AdvancementTab.addWidget bounds tracking)
     * and derive xStepSize/yStepSize by finding the minimum non-zero gap between
     * quest coordinates. This normalizes FTB's coordinate scale to vanilla units.
     */
    private void computeNodeBounds(List<QuestDataSnapshot.QuestSnapshot> quests) {
        minNodeX = Integer.MAX_VALUE;
        minNodeY = Integer.MAX_VALUE;
        maxNodeX = Integer.MIN_VALUE;
        maxNodeY = Integer.MIN_VALUE;

        // Derive step sizes from the data before computing pixel positions
        xStepSize = computeStepSize(quests.stream().mapToDouble(QuestDataSnapshot.QuestSnapshot::x).sorted().distinct().toArray());
        yStepSize = computeStepSize(quests.stream().mapToDouble(QuestDataSnapshot.QuestSnapshot::y).sorted().distinct().toArray());

        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            int x = getNodeX(quest);
            int y = getNodeY(quest);
            minNodeX = Math.min(minNodeX, x);
            maxNodeX = Math.max(maxNodeX, x + NODE_SPACING_X);
            minNodeY = Math.min(minNodeY, y);
            maxNodeY = Math.max(maxNodeY, y + NODE_SPACING_Y);
        }
    }

    /**
     * Finds the minimum non-zero absolute difference between consecutive values
     * in a sorted array. Returns 1.0 if there are fewer than 2 distinct values.
     */
    private static double computeStepSize(double[] sortedValues) {
        if (sortedValues.length < 2) return 1.0;
        double minStep = Double.MAX_VALUE;
        for (int i = 1; i < sortedValues.length; i++) {
            double gap = sortedValues[i] - sortedValues[i - 1];
            if (gap > 1e-6) {
                minStep = Math.min(minStep, gap);
            }
        }
        return minStep == Double.MAX_VALUE ? 1.0 : minStep;
    }

    // ---- Connection line drawing ----

    /**
     * Draws stable advancement-style elbow connections between quest nodes.
     */
    private void drawAllConnections(GuiGraphics graphics, List<QuestDataSnapshot.QuestSnapshot> quests, int scrollX, int scrollY, boolean outline) {
        Map<Long, QuestDataSnapshot.QuestSnapshot> questMap = new HashMap<>();
        for (QuestDataSnapshot.QuestSnapshot q : quests) {
            questMap.put(q.id(), q);
        }

        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            for (Long depId : quest.dependencyQuestIds()) {
                QuestDataSnapshot.QuestSnapshot parent = questMap.get(depId);
                if (parent == null) continue;

                int parentX = getNodeX(parent);
                int parentY = getNodeY(parent);
                int childX = getNodeX(quest);
                int childY = getNodeY(quest);

                int lineColor = outline ? 0xFF000000 : parent.completed() && isQuestAvailable(quest) ? 0xFF35E041 : 0xFFFFFFFF;

                int startX = scrollX + parentX + WIDGET_WIDTH / 2;
                int startY = scrollY + parentY + WIDGET_HEIGHT / 2;
                int endX = scrollX + childX + WIDGET_WIDTH / 2;
                int endY = scrollY + childY + WIDGET_HEIGHT / 2;
                int bendX = (startX + endX) / 2;

                if (outline) {
                    drawElbowLine(graphics, startX, bendX, startY, endX, endY, 3, lineColor);
                } else {
                    drawElbowLine(graphics, startX, bendX, startY, endX, endY, 1, lineColor);
                }
            }
        }
    }

    private void drawElbowLine(GuiGraphics graphics, int startX, int bendX, int startY, int endX, int endY, int thickness, int color) {
        drawLineSegment(graphics, startX, startY, bendX, startY, thickness, color);
        drawLineSegment(graphics, bendX, startY, bendX, endY, thickness, color);
        drawLineSegment(graphics, bendX, endY, endX, endY, thickness, color);
    }

    private void drawLineSegment(GuiGraphics graphics, int x1, int y1, int x2, int y2, int thickness, int color) {
        if (y1 == y2) {
            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int half = thickness / 2;
            for (int y = y1 - half; y <= y1 + half; y++) {
                graphics.hLine(minX, maxX, y, color);
            }
        } else if (x1 == x2) {
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            int half = thickness / 2;
            for (int x = x1 - half; x <= x1 + half; x++) {
                graphics.vLine(x, minY, maxY, color);
            }
        }
    }

    // ---- Widget drawing (matching AdvancementWidget.draw) ----

    /**
     * Draws all quest widgets.
     * Matches AdvancementWidget.draw:
     * - blit from widgets.png at (scrollX + x + 3, scrollY + y)
     * - U = frame texture offset (we use 0 = TASK frame for all quests)
     * - V = 128 + widgetType * 26 (128=obtained, 154=unobtained)
     * - Then render item icon at (scrollX + x + 8, scrollY + y + 5)
     */
    private void drawAllWidgets(GuiGraphics graphics, List<QuestDataSnapshot.QuestSnapshot> quests, int scrollX, int scrollY, int relMouseX, int relMouseY) {
        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            int nodeX = getNodeX(quest);
            int nodeY = getNodeY(quest);

            boolean obtained = quest.completed() || quest.hasUnclaimedRewards();
            int widgetTypeIndex = obtained ? 0 : 1;

            // Frame texture U: 0=TASK, 26=CHALLENGE, 52=GOAL
            // We use TASK (0) as the default frame for all quests
            int frameU = 0;

            // Matching vanilla: blit at (scrollX + x + 3, scrollY + y)
            graphics.blit(WIDGETS_LOCATION,
                    scrollX + nodeX + 3, scrollY + nodeY,
                    frameU, 128 + widgetTypeIndex * 26,
                    WIDGET_WIDTH, WIDGET_HEIGHT);

            // Matching vanilla: render icon at (scrollX + x + 8, scrollY + y + 5)
            quest.icon().draw(graphics,
                    scrollX + nodeX + WIDGET_ICON_X,
                    scrollY + nodeY + WIDGET_ICON_Y,
                    16, 16);

            if (!obtained && !isQuestAvailable(quest)) {
                renderLockOverlay(graphics, scrollX + nodeX, scrollY + nodeY);
            }

            // Hit detection for click targets (in screen coordinates)
            Rect frame = frameRect();
            int screenX = scrollX + nodeX + frame.x() + TREE_X;
            int screenY = scrollY + nodeY + frame.y() + TREE_Y;
            if (viewState.getViewedQuestId() == 0L) {
                clickTargets.add(new ClickTarget(new Rect(screenX, screenY, WIDGET_WIDTH, WIDGET_HEIGHT), () -> {
                    viewState.setViewedQuestId(quest.id());
                    viewState.setDetailScroll(0D);
                }));
            }

            visibleQuestNodes.put(quest.id(), new NodeLayout(quest, new Rect(screenX, screenY, WIDGET_WIDTH, WIDGET_HEIGHT)));

            // Check hover (in content-relative coordinates)
            int widgetScreenX = scrollX + nodeX;
            int widgetScreenY = scrollY + nodeY;
            if (relMouseX >= widgetScreenX && relMouseX <= widgetScreenX + WIDGET_WIDTH
                    && relMouseY >= widgetScreenY && relMouseY <= widgetScreenY + WIDGET_HEIGHT) {
                hoveredQuest = quest;
            }
        }
    }

    private void renderLockOverlay(GuiGraphics graphics, int nodeX, int nodeY) {
        int size = (int) (WIDGET_WIDTH / 8F * 3F);
        float scale = size / 16F;
        graphics.pose().pushPose();
        graphics.pose().translate(nodeX + WIDGET_WIDTH - size, nodeY + WIDGET_HEIGHT - 1 - size, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.blit(FTB_QUEST_LOCKED_TEXTURE, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
        graphics.pose().popPose();
    }

    // ---- Hover tooltip (matching AdvancementWidget.drawHover) ----

    /**
     * Draws the hover tooltip for a quest widget.
     * Closely matches AdvancementWidget.drawHover:
     * - Determines tooltip width from title + description
     * - Flips left if tooltip would go off-screen right
     * - Flips up if tooltip would go off-screen bottom
     * - Draws nine-sliced description background
     * - Draws title bar with progress-based split (obtained/unobtained halves)
     * - Draws frame icon overlay
     * - Draws title text and description text
     */
    private void drawWidgetHover(GuiGraphics graphics, QuestDataSnapshot.QuestSnapshot quest, int scrollX, int scrollY, float currentFade, int treeLeft, int treeTop) {
        int nodeX = getNodeX(quest);
        int nodeY = getNodeY(quest);
        boolean obtained = quest.completed() || quest.hasUnclaimedRewards();
        boolean locked = !obtained && !isQuestAvailable(quest);
        int widgetState = getQuestWidgetState(quest);

        // Build tooltip text (matching AdvancementWidget constructor logic)
        FormattedCharSequence titleSeq = Language.getInstance().getVisualOrder(font.substrByWidth(quest.title(), TITLE_MAX_WIDTH));
        int titleWidth = font.width(titleSeq);

        // Progress text: only shown when partially complete (not 0% or 100%)
        int progress = quest.progress();
        boolean showProgress = progress > 0 && progress < 100;
        String progressText = progress + "%";
        int progressTextWidth = showProgress ? font.width(progressText) : 0;
        int spaceWidth = showProgress ? font.width(" ") : 0;

        int tooltipWidth = 29 + titleWidth + progressTextWidth + spaceWidth;

        // Build description lines (matching AdvancementWidget.findOptimalLines)
        Component descComponent;
        if (!quest.subtitle().getString().isEmpty()) {
            descComponent = quest.subtitle().copy().withStyle(Style.EMPTY.withColor(obtained ? 0x55FF55 : locked ? 0xAAAAAA : 0xFFFF55));
        } else if (!quest.description().isEmpty()) {
            descComponent = quest.description().get(0).copy().withStyle(Style.EMPTY.withColor(obtained ? 0x55FF55 : locked ? 0xAAAAAA : 0xFFFF55));
        } else {
            descComponent = Component.literal(quest.tasks().size() + " requirements").withStyle(Style.EMPTY.withColor(0xAAAAAA));
        }

        List<FormattedCharSequence> description = findOptimalLines(descComponent, tooltipWidth);
        for (FormattedCharSequence line : description) {
            tooltipWidth = Math.max(tooltipWidth, font.width(line));
        }
        tooltipWidth += TITLE_PADDING_LEFT + TITLE_PADDING_RIGHT;

        // Determine tooltip position (matching vanilla drawHover logic)
        // flag = tooltip goes off right edge (vanilla uses: windowX + scrollX + x + width + 26 >= screen.width)
        boolean flipLeft = treeLeft + scrollX + nodeX + tooltipWidth + 26 >= this.width;
        // flag1 = tooltip goes off bottom edge (vanilla uses: contentHeight - scrollY - y - 26 <= 6 + desc.size * 9)
        boolean flipUp = TREE_HEIGHT - scrollY - nodeY - 26 <= 6 + description.size() * 9;

        // Progress-based title bar split (matching vanilla exactly)
        float percent = quest.progress() / 100.0F;
        int splitJ = Mth.floor(percent * (float) tooltipWidth);

        int obtainedIdx; // left half type
        int unobtainedIdx; // right half type
        int frameIdx; // frame overlay type

        if (widgetState == 2) {
            splitJ = tooltipWidth / 2;
            obtainedIdx = 2; // LOCKED
            unobtainedIdx = 2; // LOCKED
            frameIdx = 2; // LOCKED
        } else if (percent >= 1.0F) {
            splitJ = tooltipWidth / 2;
            obtainedIdx = 0; // OBTAINED
            unobtainedIdx = 0; // OBTAINED
            frameIdx = 0; // OBTAINED
        } else if (splitJ < 2) {
            splitJ = tooltipWidth / 2;
            obtainedIdx = 1; // UNOBTAINED
            unobtainedIdx = 1; // UNOBTAINED
            frameIdx = 1; // UNOBTAINED
        } else if (splitJ > tooltipWidth - 2) {
            splitJ = tooltipWidth / 2;
            obtainedIdx = 0; // OBTAINED
            unobtainedIdx = 0; // OBTAINED
            frameIdx = 1; // UNOBTAINED
        } else {
            obtainedIdx = 0; // OBTAINED
            unobtainedIdx = 1; // UNOBTAINED
            frameIdx = 1; // UNOBTAINED
        }

        int splitK = tooltipWidth - splitJ;

        RenderSystem.enableBlend();
        int drawY = scrollY + nodeY;
        int drawX;
        if (flipLeft) {
            drawX = scrollX + nodeX - tooltipWidth + 26 + 6;
        } else {
            drawX = scrollX + nodeX;
        }

        // Description background - nine-sliced (matching vanilla)
        int descHeight = 32 + description.size() * 9;
        if (!description.isEmpty()) {
            if (flipUp) {
                graphics.blitNineSliced(WIDGETS_LOCATION, drawX, drawY + 26 - descHeight, tooltipWidth, descHeight, 10, 200, 26, 0, 52);
            } else {
                graphics.blitNineSliced(WIDGETS_LOCATION, drawX, drawY, tooltipWidth, descHeight, 10, 200, 26, 0, 52);
            }
        }

        // Title bar - left half (obtained portion)
        graphics.blit(WIDGETS_LOCATION, drawX, drawY, 0, obtainedIdx * 26, splitJ, 26);
        // Title bar - right half (unobtained portion)
        graphics.blit(WIDGETS_LOCATION, drawX + splitJ, drawY, 200 - splitK, unobtainedIdx * 26, splitK, 26);

        // Frame icon overlay (matching vanilla: at x+3, y with frame texture)
        graphics.blit(WIDGETS_LOCATION, scrollX + nodeX + 3, scrollY + nodeY, 0, 128 + frameIdx * 26, WIDGET_WIDTH, WIDGET_HEIGHT);

        // Title text with space before progress (progress omitted at 0% and 100%)
        if (flipLeft) {
            graphics.drawString(this.font, titleSeq, drawX + 5, scrollY + nodeY + TITLE_Y, -1);
            if (showProgress) {
                graphics.drawString(this.font, " " + progressText, scrollX + nodeX - progressTextWidth - font.width(" "), scrollY + nodeY + TITLE_Y, -1);
            }
        } else {
            graphics.drawString(this.font, titleSeq, scrollX + nodeX + TITLE_X, scrollY + nodeY + TITLE_Y, -1);
            if (showProgress) {
                graphics.drawString(this.font, " " + progressText, scrollX + nodeX + tooltipWidth - progressTextWidth - font.width(" ") - 5, scrollY + nodeY + TITLE_Y, -1);
            }
        }

        // Description lines (matching vanilla text color: -5592406 = 0xFFAAAAAA)
        if (flipUp) {
            for (int idx = 0; idx < description.size(); idx++) {
                graphics.drawString(this.font, description.get(idx), drawX + 5, drawY + 26 - descHeight + 7 + idx * 9, -5592406, false);
            }
        } else {
            for (int idx = 0; idx < description.size(); idx++) {
                graphics.drawString(this.font, description.get(idx), drawX + 5, scrollY + nodeY + 9 + 17 + idx * 9, -5592406, false);
            }
        }

        // Re-render icon on top of tooltip (matching vanilla)
        quest.icon().draw(graphics,
                scrollX + nodeX + WIDGET_ICON_X,
                scrollY + nodeY + WIDGET_ICON_Y,
                16, 16);
    }

    /**
     * Matches AdvancementWidget.findOptimalLines:
     * Tries different split widths to find the best text wrapping.
     */
    private List<FormattedCharSequence> findOptimalLines(Component text, int maxWidth) {
        var splitter = this.font.getSplitter();
        List<FormattedCharSequence> best = null;
        float bestDiff = Float.MAX_VALUE;

        for (int offset : TEST_SPLIT_OFFSETS) {
            var lines = splitter.splitLines(text, maxWidth - offset, Style.EMPTY);
            float maxLineWidth = (float) lines.stream().mapToDouble(splitter::stringWidth).max().orElse(0.0D);
            float diff = Math.abs(maxLineWidth - (float) maxWidth);
            if (diff <= 10.0F) {
                return lines.stream()
                        .map(ft -> Language.getInstance().getVisualOrder(ft))
                        .toList();
            }
            if (diff < bestDiff) {
                bestDiff = diff;
                best = lines.stream()
                        .map(ft -> Language.getInstance().getVisualOrder(ft))
                        .toList();
            }
        }
        return best != null ? best : List.of();
    }

    // ---- Tab rendering (matching AdvancementTabType.ABOVE) ----

    /**
     * Draws a chapter tab along the top of the window.
     * Matches AdvancementTabType.ABOVE.draw:
     * - textureX=0 (first tab), +28 (middle tabs), +56 (last tab)
     * - textureY=0 (unselected), +32 (selected)
     * - Position: (tabWidth+4)*index, y=-tabHeight+4
     */
    private void drawTab(GuiGraphics graphics, int windowX, int windowY, int tabIndex, boolean selected) {
        int localIndex = tabIndex % AdvancementTabTypeAbove.MAX_TABS;

        int textureX = 0;
        if (localIndex > 0) textureX += TAB_WIDTH;
        if (localIndex == AdvancementTabTypeAbove.MAX_TABS - 1) textureX += TAB_WIDTH;

        int textureY = selected ? TAB_HEIGHT : 0;

        int tabX = windowX + (TAB_WIDTH + 4) * localIndex;
        int tabY = windowY + (-TAB_HEIGHT + 4);

        graphics.blit(TABS_LOCATION, tabX, tabY, textureX, textureY, TAB_WIDTH, TAB_HEIGHT);
    }

    /**
     * Draws the chapter icon on its tab.
     * Matches AdvancementTabType.ABOVE.drawIcon:
     * - Icon offset: x+6, y+9
     */
    private void drawTabIcon(GuiGraphics graphics, int windowX, int windowY, int tabIndex, QuestDataSnapshot.ChapterSnapshot chapter) {
        int localIndex = tabIndex % AdvancementTabTypeAbove.MAX_TABS;
        int tabX = windowX + (TAB_WIDTH + 4) * localIndex + 6;
        int tabY = windowY + (-TAB_HEIGHT + 4) + 9;
        chapter.icon().draw(graphics, tabX, tabY, 16, 16);
    }

    private boolean isTabMouseOver(int windowX, int windowY, int tabIndex, double mouseX, double mouseY) {
        int localIndex = tabIndex % AdvancementTabTypeAbove.MAX_TABS;
        int tabX = windowX + (TAB_WIDTH + 4) * localIndex;
        int tabY = windowY + (-TAB_HEIGHT + 4);
        return mouseX > tabX && mouseX < tabX + TAB_WIDTH && mouseY > tabY && mouseY < tabY + TAB_HEIGHT;
    }

    private int getTabPage(int tabIndex) {
        return tabIndex / AdvancementTabTypeAbove.MAX_TABS;
    }

    private void updateTabPages() {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        int tabCount = snapshot.chapters().size();
        if (tabCount > AdvancementTabTypeAbove.MAX_TABS) {
            maxPages = (tabCount - 1) / AdvancementTabTypeAbove.MAX_TABS;
        } else {
            maxPages = 0;
        }
        tabPage = Mth.clamp(tabPage, 0, maxPages);
    }

    private void updateCursor(QuestDataSnapshot snapshot, int mouseX, int mouseY, int treeLeft, int treeTop) {
        CursorType.set(isInteractiveTargetHovered(snapshot, mouseX, mouseY, treeLeft, treeTop) ? CursorType.HAND : null);
    }

    private boolean isInteractiveTargetHovered(QuestDataSnapshot snapshot, double mouseX, double mouseY, int treeLeft, int treeTop) {
        for (int i = clickTargets.size() - 1; i >= 0; i--) {
            if (clickTargets.get(i).contains(mouseX, mouseY)) {
                return true;
            }
        }

        if (viewState.getViewedQuestId() != 0L) {
            return false;
        }

        Rect scrollbarHitbox = chapterScrollbarHitbox(snapshot);
        if (scrollbarHitbox != null && scrollbarHitbox.contains(mouseX, mouseY)) {
            return true;
        }

        return false;
    }

    // Tab type constants - max tabs per page is limited by tree panel width
    // Tree panel is 186px wide, each tab is 32px (28+4), so ~5 tabs fit
    private static final class AdvancementTabTypeAbove {
        static final int MAX_TABS = 5;
    }

    // ---- Quest Detail Modal ----

    private void renderQuestDetailModal(GuiGraphics graphics, QuestDataSnapshot.QuestSnapshot quest, int mouseX, int mouseY) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 250.0F);
        graphics.fill(0, 0, width, height, 0xB015110D);

        DetailLayout layout = buildDetailLayout(quest);
        Rect rect = layout.rect();
        Rect header = layout.headerRect();
        Rect body = layout.bodyRect();
        Rect footer = layout.footerRect();

        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), 0xD4211711);
        drawInsetBorder(graphics, rect, 0xFF8F6938, 0xCC160F0B);
        graphics.fill(rect.x() + 6, rect.y() + 6, rect.maxX() - 6, rect.maxY() - 6, 0x96241711);

        graphics.fill(header.x(), header.y(), header.maxX(), header.maxY(), 0xCC8F6A1D);
        drawInsetBorder(graphics, header, 0xFFD6A85C, 0xAA3E2B13);

        quest.icon().draw(graphics, header.x() + 7, header.y() + 6, 16, 16);
        drawCenteredScaledString(graphics, trim(quest.title(), (int) ((header.width() - 58) / MODAL_TEXT_SCALE)), header.centerX(), header.y() + 6, 0xFFF6EDDB, MODAL_TEXT_SCALE);
        drawCenteredScaledString(graphics, Component.literal(statusLabel(quest)), header.centerX(), header.y() + 16, 0xFFD8C49D, MODAL_TEXT_SCALE);

        int closeX = header.maxX() - 18;
        drawCenteredScaledString(graphics, Component.literal("x"), closeX + 5, header.y() + 8, 0xFFF6E8C9, MODAL_TEXT_SCALE);
        clickTargets.add(new ClickTarget(new Rect(closeX, header.y() + 4, 12, 12), this::closeViewedQuest));

        graphics.enableScissor(body.x(), body.y(), body.maxX(), body.maxY());
        int contentY = body.y() + 2 - (int) viewState.getDetailScroll();

        contentY = renderSectionLabel(graphics, "Objective", body.x(), contentY);
        for (FormattedCharSequence line : layout.objectiveLines()) {
            drawScaledString(graphics, line, body.x(), contentY, 0xFF6FE142, MODAL_TEXT_SCALE);
            contentY += 8;
        }
        contentY += 4;
        drawSectionDivider(graphics, body.x(), body.width(), contentY);
        contentY += 8;

        contentY = renderSectionLabel(graphics, "Details", body.x(), contentY);
        for (FormattedCharSequence line : layout.descriptionLines()) {
            drawScaledString(graphics, line, body.x(), contentY, 0xFFF0E2C5, MODAL_TEXT_SCALE);
            contentY += 8;
        }
        contentY += 4;
        drawSectionDivider(graphics, body.x(), body.width(), contentY);
        contentY += 8;

        int columnGap = 8;
        int columnWidth = (body.width() - columnGap) / 2;
        int leftX = body.x();
        int rightX = body.x() + columnWidth + columnGap;
        int columnStartY = contentY;

        List<QuestDataSnapshot.TaskSnapshot> visibleTasks = visibleRequirementTasks(quest);
        int requirementsY = renderSectionLabel(graphics, "Requirements", leftX, columnStartY);
        if (visibleTasks.isEmpty()) {
            drawScaledString(graphics, Component.literal("No explicit requirements"), leftX, requirementsY, 0xFFC8B08C, MODAL_TEXT_SCALE);
            requirementsY += 10;
        } else {
            for (QuestDataSnapshot.TaskSnapshot task : visibleTasks) {
                requirementsY = renderTaskSummaryRow(graphics, leftX, columnWidth, task, requirementsY);
            }
        }

        int rewardsY = renderSectionLabel(graphics, "Reward", rightX, columnStartY);
        if (quest.rewards().isEmpty()) {
            drawScaledString(graphics, Component.literal("No reward configured"), rightX, rewardsY, 0xFFC8B08C, MODAL_TEXT_SCALE);
            rewardsY += 10;
        } else {
            for (QuestDataSnapshot.RewardSnapshot reward : quest.rewards()) {
                rewardsY = renderRewardSummaryRow(graphics, rightX, columnWidth, reward, rewardsY);
            }
        }
        contentY = Math.max(requirementsY, rewardsY);
        graphics.disableScissor();

        if (layout.contentHeight() > body.height()) {
            int step = Math.max(48, body.height() - 28);
            int arrowX = rect.maxX() - 20;
            int upY = body.y() + 2;
            int downY = footer.y() - 14;
            boolean canUp = viewState.getDetailScroll() > 0D;
            boolean canDown = viewState.getDetailScroll() < layout.contentHeight() - body.height();

            renderScrollArrow(graphics, arrowX, upY, true, canUp);
            renderScrollArrow(graphics, arrowX, downY, false, canDown);

            if (canUp) {
                clickTargets.add(new ClickTarget(new Rect(arrowX - 2, upY - 2, 16, 16),
                        () -> viewState.setDetailScroll(clampScroll(viewState.getDetailScroll() - step, layout.contentHeight(), body.height()))));
            }
            if (canDown) {
                clickTargets.add(new ClickTarget(new Rect(arrowX - 2, downY - 2, 16, 16),
                        () -> viewState.setDetailScroll(clampScroll(viewState.getDetailScroll() + step, layout.contentHeight(), body.height()))));
            }
        }

        graphics.fill(footer.x(), footer.y(), footer.maxX(), footer.maxY(), 0x7819100C);
        drawInsetBorder(graphics, footer, 0xFF805C32, 0xCC1C1410);

        int buttonGap = 8;
        int buttonSlotWidth = (footer.width() - buttonGap) / 2;
        int buttonWidth = Math.round(buttonSlotWidth * 0.75F);
        Rect acceptButton = footerButtonRect(footer.x() + Math.max(0, (buttonSlotWidth - buttonWidth) / 2), footer.y(), buttonWidth, footer.height());
        Rect completeButton = footerButtonRect(footer.x() + buttonSlotWidth + buttonGap + Math.max(0, (buttonSlotWidth - buttonWidth) / 2), footer.y(), buttonWidth, footer.height());

        renderFooterButton(graphics, acceptButton, quest.pinned() ? "Unaccept" : "Accept", true, acceptButton.contains(mouseX, mouseY), quest.pinned());
        boolean claimEnabled = canClaimAnyReward(quest);
        renderFooterButton(graphics, completeButton, "Complete", claimEnabled, completeButton.contains(mouseX, mouseY), false);
        if (quest.hasUnclaimedRewards() || claimEnabled) {
            renderNotificationMarker(graphics, completeButton.maxX() - 4, completeButton.y() - 3, quest.completed() && !claimEnabled);
        }

        clickTargets.add(new ClickTarget(acceptButton, () -> acceptOrUnacceptQuest(quest)));
        if (claimEnabled) {
            clickTargets.add(new ClickTarget(completeButton, this::claimCurrentQuestRewards));
        }
        graphics.pose().popPose();
    }

    private void renderDefaultViewButton(GuiGraphics graphics, int mouseX, int mouseY) {
        Rect rect = new Rect(width - 92, height - 18, 84, 12);
        boolean hovered = rect.contains(mouseX, mouseY);

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 260.0F);
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), hovered ? 0xAA2A2118 : 0x88201812);
        drawInsetBorder(graphics, rect, hovered ? 0xAA8C734E : 0x885B4630, 0xAA120D09);
        drawCenteredScaledString(graphics, Component.literal("Editing Mode"), rect.centerX(), rect.y() + 3, 0xFFD9C6A4, MODAL_TEXT_SCALE);
        graphics.pose().popPose();

        clickTargets.add(new ClickTarget(rect, this::openEditingMode));
    }

    private boolean shouldShowDefaultViewButton() {
        return minecraft == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() != GameType.SURVIVAL;
    }

    private int renderSectionLabel(GuiGraphics graphics, String label, int x, int y) {
        drawScaledString(graphics, Component.literal(label + ":"), x, y, 0xFFF6ECD6, MODAL_TEXT_SCALE);
        return y + 10;
    }

    private int renderTaskSummaryRow(GuiGraphics graphics, int x, int width, QuestDataSnapshot.TaskSnapshot task, int y) {
        Rect rect = new Rect(x, y, width, 18);
        Rect iconRect = new Rect(rect.x() + 2, rect.y() + 1, 14, 14);
        boolean consumableTurnIn = task.consumesResources() && task.interactionMode() == TaskInteractionMode.SUBMIT;
        Rect actionRect = consumableTurnIn ? new Rect(rect.maxX() - 37, rect.y() + 2, 33, 12) : null;
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), 0x5A3A2A1D);
        drawInsetBorder(graphics, rect, 0x885F452F, 0x55201510);

        task.icon().draw(graphics, iconRect.x(), iconRect.y(), 14, 14);
        if (task.completed()) {
            renderSmallCheck(graphics, iconRect.maxX() - 4, iconRect.y() - 2);
        }
        if (consumableTurnIn) {
            if (task.canInteract()) {
                renderRequirementMarker(graphics, iconRect, true);
            }
            renderConsumableActionButton(graphics, actionRect, task.canInteract());
        }
        int infoX = rect.x() + 20;
        int progressWidth = Math.round(font.width(task.progressText()) * MODAL_TEXT_SCALE);
        int progressRightBound = consumableTurnIn ? actionRect.x() - 6 : rect.maxX() - 34;
        int progressX = Math.min(infoX, progressRightBound - progressWidth);
        if (progressX < infoX) {
            progressX = infoX;
        }
        drawScaledString(graphics, task.progressText(), progressX, rect.y() + 4, task.completed() ? 0xFF75D65C : 0xFFD9BE96, MODAL_TEXT_SCALE);

        String state = switch (task.interactionMode()) {
            case SUBMIT -> consumableTurnIn && task.canInteract() ? "Turn in" : task.canInteract() ? "Ready" : task.completed() ? "Done" : "Locked";
            case VANILLA_FALLBACK -> "Vanilla";
            case READ_ONLY -> task.completed() ? "Done" : "Track";
        };
        if (consumableTurnIn) {
            if (task.canInteract()) {
                clickTargets.add(new ClickTarget(actionRect, () -> {
                    Task liveTask = resolveTask(task.id());
                    if (liveTask != null) {
                        actionRouter.submitTask(liveTask);
                    }
                }));
            }
        } else {
            int stateWidth = Math.round(font.width(state) * MODAL_TEXT_SCALE);
            int stateX = Math.max(infoX + progressWidth + 6, rect.maxX() - stateWidth - 4);
            drawScaledString(graphics, Component.literal(state), stateX, rect.y() + 4, task.completed() ? 0xFF75D65C : 0xFFC8A97C, MODAL_TEXT_SCALE);
        }
        return y + 20;
    }

    private int renderRewardSummaryRow(GuiGraphics graphics, int x, int width, QuestDataSnapshot.RewardSnapshot rewardSnapshot, int y) {
        Rect rect = new Rect(x, y, width, 18);
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), 0x5635261B);
        drawInsetBorder(graphics, rect, 0x88634731, 0x55201510);

        rewardSnapshot.icon().draw(graphics, rect.x() + 3, rect.y() + 1, 14, 14);
        if (rewardSnapshot.claimed()) {
            renderSmallCheck(graphics, rect.x() + 13, rect.y() - 1);
        }

        int statusColor = rewardSnapshot.canClaim() ? 0xFF7DE25E : rewardSnapshot.claimed() ? 0xFFB6B6B6 : 0xFFD5B58C;
        String state = switch (rewardSnapshot.interactionMode()) {
            case CLAIM -> rewardSnapshot.canClaim() ? "Claim" : rewardSnapshot.claimed() ? "Claimed" : "Locked";
            case CHOICE -> rewardSnapshot.canClaim() ? "Choose" : rewardSnapshot.claimed() ? "Claimed" : "Locked";
            case VANILLA_FALLBACK -> rewardSnapshot.canClaim() ? "Vanilla" : "Locked";
        };
        int stateWidth = Math.round(font.width(state) * MODAL_TEXT_SCALE);
        int stateX = Math.max(rect.x() + 20, rect.maxX() - stateWidth - 4);
        drawScaledString(graphics, Component.literal(state), stateX, rect.y() + 4, statusColor, MODAL_TEXT_SCALE);
        return y + 20;
    }

    private void renderSmallCheck(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y + 4, x + 2, y + 6, 0xFF52E652);
        graphics.fill(x + 2, y + 5, x + 4, y + 7, 0xFF52E652);
        graphics.fill(x + 4, y + 1, x + 7, y + 4, 0xFF52E652);
    }

    private void renderScrollArrow(GuiGraphics graphics, int x, int y, boolean up, boolean active) {
        graphics.fill(x, y, x + 12, y + 12, active ? 0xCC72522E : 0x88342519);
        drawInsetBorder(graphics, new Rect(x, y, 12, 12), active ? 0xFFC18F4B : 0xAA58412A, 0x990F0B08);
        graphics.drawCenteredString(font, Component.literal(up ? "^" : "v"), x + 6, y + 2, active ? 0xFFF7E9D3 : 0xFF8F7556);
    }

    private void renderRequirementMarker(GuiGraphics graphics, Rect iconRect, boolean active) {
        Rect badge = new Rect(iconRect.centerX() - 3, iconRect.y() - 4, 6, 6);
        graphics.fill(badge.x(), badge.y(), badge.maxX(), badge.maxY(), active ? 0xFFD2A63B : 0xAA705C3D);
        drawInsetBorder(graphics, badge, active ? 0xFFFFE1A3 : 0xCC9B8764, 0xAA24180F);
        drawCenteredScaledString(graphics, Component.literal("!"), badge.centerX(), badge.y(), active ? 0xFF2B1706 : 0xFF24180F, 0.55F);
    }

    private void renderConsumableActionButton(GuiGraphics graphics, Rect rect, boolean active) {
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), active ? 0xCC926B21 : 0x99433528);
        drawInsetBorder(graphics, rect, active ? 0xFFE1BC72 : 0xAA78624B, 0xAA1C140F);
        drawScaledString(graphics, Component.literal("Turn in"), rect.x() + 2, rect.y() + 4, active ? 0xFFF7EAD1 : 0xFF9A8470, MODAL_TEXT_SCALE);
    }

    private void drawVanillaButton(GuiGraphics graphics, Rect rect, boolean enabled, boolean hovered) {
        int textureY = !enabled ? 46 : hovered ? 86 : 66;
        int halfWidth = rect.width() / 2;
        float yScale = rect.height() / 20F;
        graphics.pose().pushPose();
        graphics.pose().translate(rect.x(), rect.y(), 0F);
        graphics.pose().scale(1F, yScale, 1F);
        graphics.blit(BUTTONS_LOCATION, 0, 0, 0, textureY, halfWidth, 20, 256, 256);
        graphics.blit(BUTTONS_LOCATION, halfWidth, 0, 200 - (rect.width() - halfWidth), textureY, rect.width() - halfWidth, 20, 256, 256);
        graphics.pose().popPose();
    }

    private void renderFooterButton(GuiGraphics graphics, Rect rect, String label, boolean enabled, boolean hovered, boolean pressed) {
        drawVanillaButton(graphics, rect, enabled, hovered);
        if (pressed) {
            graphics.fill(rect.x() + 2, rect.y() + 2, rect.maxX() - 2, rect.maxY() - 2, 0x55201018);
        }
        int textHeight = Math.round(font.lineHeight * MODAL_TEXT_SCALE);
        int textY = rect.y() + Math.max(0, (rect.height() - textHeight) / 2);
        drawCenteredScaledString(graphics, Component.literal(label), rect.centerX(), textY, enabled ? 0xFFE0E0E0 : 0xFFA0A0A0, MODAL_TEXT_SCALE);
    }

    private void renderNotificationMarker(GuiGraphics graphics, int x, int y, boolean completed) {
        if (completed) {
            graphics.fill(x, y + 3, x + 2, y + 5, 0xFF4DE047);
            graphics.fill(x + 2, y + 4, x + 4, y + 6, 0xFF4DE047);
            graphics.fill(x + 4, y + 1, x + 7, y + 4, 0xFF4DE047);
        } else {
            graphics.fill(x, y, x + 7, y + 7, 0xFFE92C2C);
            graphics.fill(x + 3, y + 1, x + 4, y + 5, 0xFFFFFFFF);
            graphics.fill(x + 3, y + 6, x + 4, y + 7, 0xFFFFFFFF);
        }
    }

    private Rect footerButtonRect(int x, int y, int width, int containerHeight) {
        return new Rect(x, y + Math.max(0, (containerHeight - FOOTER_BUTTON_HEIGHT) / 2), width, FOOTER_BUTTON_HEIGHT);
    }

    // ---- Layout and state helpers ----

    private DetailLayout buildDetailLayout(QuestDataSnapshot.QuestSnapshot quest) {
        int desiredWidth = Math.max(
                Math.max(font.width(quest.title()), font.width(resolveObjectiveText(quest))),
                flattenDescription(resolveDescription(quest), MODAL_MAX_WIDTH - 56).stream().mapToInt(font::width).max().orElse(0)
        ) + 54;
        int modalWidth = Mth.clamp(desiredWidth, MODAL_MIN_WIDTH, Math.min(MODAL_MAX_WIDTH, width - 36));

        List<FormattedCharSequence> objectiveLines = font.split(resolveObjectiveText(quest), (int) ((modalWidth - 28) / MODAL_TEXT_SCALE));
        List<FormattedCharSequence> descriptionLines = flattenDescription(resolveDescription(quest), (int) ((modalWidth - 28) / MODAL_TEXT_SCALE));

        int contentHeight = 10 + objectiveLines.size() * 8 + 12;
        contentHeight += 10 + descriptionLines.size() * 8 + 12;
        int requirementsHeight = 10 + Math.max(1, visibleRequirementTasks(quest).size()) * 20;
        int rewardsHeight = 10 + Math.max(1, quest.rewards().size()) * 20;
        contentHeight += Math.max(requirementsHeight, rewardsHeight) + 6;

        int maxHeight = height - MODAL_MARGIN;
        int chromeHeight = 14 + MODAL_HEADER_HEIGHT + MODAL_FOOTER_HEIGHT + 18;
        int modalHeight = Math.min(contentHeight + chromeHeight, maxHeight);
        Rect rect = new Rect((width - modalWidth) / 2, (height - modalHeight) / 2, modalWidth, modalHeight);
        Rect headerRect = new Rect(rect.x() + 10, rect.y() + 8, rect.width() - 20, MODAL_HEADER_HEIGHT);
        Rect footerRect = new Rect(rect.x() + 12, rect.maxY() - MODAL_FOOTER_HEIGHT - 8, rect.width() - 24, MODAL_FOOTER_HEIGHT);
        Rect bodyRect = new Rect(rect.x() + 12, headerRect.maxY() + 8, rect.width() - 28, footerRect.y() - headerRect.maxY() - 14);

        viewState.setDetailScroll(clampScroll(viewState.getDetailScroll(), contentHeight, bodyRect.height()));
        return new DetailLayout(rect, headerRect, bodyRect, footerRect, objectiveLines, descriptionLines, contentHeight);
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

    private Component resolveObjectiveText(QuestDataSnapshot.QuestSnapshot quest) {
        if (!quest.subtitle().getString().isBlank()) {
            return quest.subtitle();
        }
        return quest.title();
    }

    private List<Component> resolveDescription(QuestDataSnapshot.QuestSnapshot quest) {
        if (quest.hiddenDetails()) {
            return List.of(Component.literal("Details stay hidden until this quest becomes available."));
        }
        return quest.description().isEmpty() ? List.of(Component.literal("No additional details.")) : quest.description();
    }

    private boolean canClaimAnyReward(QuestDataSnapshot.QuestSnapshot quest) {
        return quest.rewards().stream().anyMatch(QuestDataSnapshot.RewardSnapshot::canClaim);
    }

    private List<QuestDataSnapshot.TaskSnapshot> visibleRequirementTasks(QuestDataSnapshot.QuestSnapshot quest) {
        if (quest.checkmarkOnly()) {
            return List.of();
        }
        return quest.tasks();
    }

    private void acceptOrUnacceptQuest(QuestDataSnapshot.QuestSnapshot questSnapshot) {
        Quest quest = resolveQuest(questSnapshot.id());
        if (quest == null) {
            return;
        }

        if (questSnapshot.pinned()) {
            actionRouter.togglePin(quest);
            QuestDataController.markDirty();
            return;
        }

        if (questSnapshot.checkmarkOnly()) {
            questSnapshot.tasks().stream()
                    .filter(task -> !task.completed() && task.canInteract() && task.interactionMode() == TaskInteractionMode.SUBMIT)
                    .findFirst()
                    .map(task -> resolveTask(task.id()))
                    .ifPresent(actionRouter::submitTask);
        }

        actionRouter.togglePin(quest);
        QuestDataController.markDirty();
    }

    private void claimCurrentQuestRewards() {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        QuestDataSnapshot.QuestSnapshot questSnapshot = snapshot.findQuest(viewState.getViewedQuestId());
        if (questSnapshot == null) {
            return;
        }

        boolean openedChoiceScreen = false;
        for (QuestDataSnapshot.RewardSnapshot rewardSnapshot : questSnapshot.rewards()) {
            if (!rewardSnapshot.canClaim()) {
                continue;
            }

            Reward liveReward = resolveReward(rewardSnapshot.id());
            if (liveReward == null) {
                continue;
            }

            if (rewardSnapshot.interactionMode() == RewardInteractionMode.CLAIM) {
                actionRouter.claimReward(liveReward, true);
            } else if (rewardSnapshot.interactionMode() == RewardInteractionMode.CHOICE && !openedChoiceScreen && liveReward instanceof ChoiceReward choiceReward) {
                minecraft.setScreen(new ChoiceRewardSelectScreen(this, choiceReward, actionRouter));
                openedChoiceScreen = true;
            } else if (rewardSnapshot.interactionMode() == RewardInteractionMode.VANILLA_FALLBACK) {
                Quest liveQuest = resolveQuest(questSnapshot.id());
                if (liveQuest != null) {
                    actionRouter.openVanillaForQuest(liveQuest);
                    return;
                }
            }
        }
    }

    private int getQuestWidgetState(QuestDataSnapshot.QuestSnapshot quest) {
        if (quest.completed() || quest.hasUnclaimedRewards()) {
            return 0;
        }
        return isQuestAvailable(quest) ? 1 : 2;
    }

    private boolean isQuestAvailable(QuestDataSnapshot.QuestSnapshot quest) {
        return quest.canStart() || quest.started();
    }

    private void refreshState(QuestDataSnapshot snapshot) {
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
            focusedChapterId = chapter.id();
            centered = false;
            ensureSelectedChapterVisible(snapshot.chapters(), chapter.id());
        }

        clampChapterScroll(snapshot);
        updateTabPages();
    }

    private void openVanillaSelected() {
        Quest quest = resolveQuest(viewState.getViewedQuestId());
        if (quest != null) {
            actionRouter.openVanillaForQuest(quest);
        } else {
            actionRouter.openVanillaRoot();
        }
    }

    private void openEditingMode() {
        viewState.setDefaultFtbUiMode(true);
        QuestDataController.saveViewState(viewState);
        if (ClientQuestFile.exists() && !ClientQuestFile.canClientPlayerEdit()) {
            actionRouter.toggleEditingMode();
        }
        actionRouter.openVanillaRoot();
    }

    private void toggleCurrentQuestPin() {
        Quest quest = resolveQuest(viewState.getViewedQuestId());
        if (quest != null) {
            actionRouter.togglePin(quest);
            QuestDataController.markDirty();
        }
    }

    private void selectChapter(long chapterId) {
        if (viewState.getSelectedChapterId() != chapterId) {
            viewState.setSelectedChapterId(chapterId);
            centered = false;
        }
        closeViewedQuest();
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

    private boolean handleChapterScrollbarClick(QuestDataSnapshot snapshot, double mouseX, double mouseY) {
        Rect scrollbarHitbox = chapterScrollbarHitbox(snapshot);
        if (scrollbarHitbox == null || !scrollbarHitbox.contains(mouseX, mouseY)) {
            return false;
        }

        chapterScrollbarDragging = true;
        updateChapterScrollFromMouse(mouseY, snapshot);
        return true;
    }

    private void scrollChapterSelector(double delta, QuestDataSnapshot snapshot) {
        if (snapshot.chapters().isEmpty()) {
            chapterScroll = 0D;
            return;
        }

        chapterScroll = Mth.clamp(chapterScroll + delta, 0D, maxChapterScroll(snapshot));
    }

    private void clampChapterScroll(QuestDataSnapshot snapshot) {
        chapterScroll = Mth.clamp(chapterScroll, 0D, maxChapterScroll(snapshot));
    }

    private void ensureSelectedChapterVisible(List<QuestDataSnapshot.ChapterSnapshot> chapters, long selectedChapterId) {
        int entryTop = 0;
        int selectedIndex = -1;
        for (int i = 0; i < chapters.size(); i++) {
            if (chapters.get(i).firstInGroup()) {
                entryTop += CHAPTER_GROUP_HEADER_HEIGHT;
            }
            if (chapters.get(i).id() == selectedChapterId) {
                selectedIndex = i;
                break;
            }
            entryTop += CHAPTER_SELECTOR_SCROLL_STEP;
        }

        if (selectedIndex < 0) {
            return;
        }

        int viewportHeight = chapterSelectorViewportHeight();
        int entryBottom = entryTop + CHAPTER_BUTTON_HEIGHT;
        if (entryTop < chapterScroll) {
            chapterScroll = entryTop;
        } else if (entryBottom > chapterScroll + viewportHeight) {
            chapterScroll = entryBottom - viewportHeight;
        }

        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        clampChapterScroll(snapshot);
    }

    private void updateChapterScrollFromMouse(double mouseY, QuestDataSnapshot snapshot) {
        Rect trackRect = chapterScrollbarTrackRect();
        Rect thumbRect = chapterScrollbarThumbRect(snapshot);
        if (trackRect == null || thumbRect == null) {
            chapterScroll = 0D;
            return;
        }

        int maxThumbTravel = Math.max(0, trackRect.height() - thumbRect.height());
        if (maxThumbTravel == 0) {
            chapterScroll = 0D;
            return;
        }

        double thumbOffset = Mth.clamp(mouseY - trackRect.y() - thumbRect.height() / 2D, 0D, maxThumbTravel);
        chapterScroll = thumbOffset / maxThumbTravel * maxChapterScroll(snapshot);
    }

    private void renderChapterScrollbar(GuiGraphics graphics, QuestDataSnapshot snapshot) {
        Rect trackRect = chapterScrollbarTrackRect();
        Rect thumbRect = chapterScrollbarThumbRect(snapshot);
        if (trackRect == null || thumbRect == null) {
            return;
        }

        graphics.fill(trackRect.x(), trackRect.y(), trackRect.maxX(), trackRect.maxY(), 0x44271410);
        graphics.fill(thumbRect.x(), thumbRect.y(), thumbRect.maxX(), thumbRect.maxY(),
                chapterScrollbarDragging ? 0xFFF0E2C5 : 0xCCBFA68A);
    }

    private Rect chapterSelectorViewportRect() {
        Rect frame = frameRect();
        return new Rect(frame.x() + CHAPTER_SELECTOR_X, frame.y() + CHAPTER_SELECTOR_Y,
                CHAPTER_BUTTON_ACTIVE_WIDTH, chapterSelectorViewportHeight());
    }

    private int chapterSelectorViewportHeight() {
        return BACKGROUND_HEIGHT - CHAPTER_SELECTOR_Y - CHAPTER_SELECTOR_BOTTOM_PADDING;
    }

    private Rect chapterScrollbarTrackRect() {
        Rect viewportRect = chapterSelectorViewportRect();
        return new Rect(viewportRect.x() - CHAPTER_SCROLLBAR_GAP - CHAPTER_SCROLLBAR_WIDTH, viewportRect.y(),
                CHAPTER_SCROLLBAR_WIDTH, viewportRect.height());
    }

    private Rect chapterScrollbarThumbRect(QuestDataSnapshot snapshot) {
        Rect trackRect = chapterScrollbarTrackRect();
        int contentHeight = chapterContentHeight(snapshot);
        if (contentHeight <= trackRect.height()) {
            return null;
        }

        int thumbHeight = Mth.clamp(Math.round((float) trackRect.height() * trackRect.height() / (float) contentHeight),
                CHAPTER_SCROLLBAR_MIN_THUMB_HEIGHT, trackRect.height());
        int maxThumbTravel = Math.max(0, trackRect.height() - thumbHeight);
        double maxScroll = maxChapterScroll(snapshot);
        int thumbY = trackRect.y() + (maxScroll <= 0D || maxThumbTravel == 0 ? 0
                : Mth.floor(chapterScroll / maxScroll * maxThumbTravel));
        return new Rect(trackRect.x(), thumbY, trackRect.width(), thumbHeight);
    }

    private Rect chapterScrollbarHitbox(QuestDataSnapshot snapshot) {
        Rect trackRect = chapterScrollbarTrackRect();
        if (chapterScrollbarThumbRect(snapshot) == null) {
            return null;
        }

        return new Rect(trackRect.x() - (CHAPTER_SCROLLBAR_HITBOX_WIDTH - trackRect.width()) / 2, trackRect.y(),
                CHAPTER_SCROLLBAR_HITBOX_WIDTH, trackRect.height());
    }

    private int chapterContentHeight(QuestDataSnapshot snapshot) {
        int height = 0;
        for (QuestDataSnapshot.ChapterSnapshot chapter : snapshot.chapters()) {
            if (chapter.firstInGroup()) {
                height += CHAPTER_GROUP_HEADER_HEIGHT;
            }
            height += CHAPTER_SELECTOR_SCROLL_STEP;
        }
        return Math.max(0, height - CHAPTER_SELECTOR_ENTRY_SPACING);
    }

    private double maxChapterScroll(QuestDataSnapshot snapshot) {
        return Math.max(0D, chapterContentHeight(snapshot) - chapterSelectorViewportHeight());
    }

    private Rect frameRect() {
        return new Rect((width - BACKGROUND_WIDTH) / 2, (height - BACKGROUND_HEIGHT) / 2, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    private Rect treeViewportRect() {
        Rect frame = frameRect();
        return new Rect(frame.x() + TREE_X, frame.y() + TREE_Y, TREE_WIDTH, TREE_HEIGHT);
    }

    private boolean isCreativeControlsVisible() {
        return minecraft == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() != GameType.SURVIVAL;
    }

    // ---- Drawing primitives ----

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

    private void drawSectionDivider(GuiGraphics graphics, int x, int width, int y) {
        graphics.fill(x, y, x + width, y + 1, 0x88664B30);
        graphics.fill(x, y + 1, x + width, y + 2, 0x44180F0B);
    }

    private void drawScaledString(GuiGraphics graphics, Component component, int x, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.drawString(font, component, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private void drawScaledString(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.drawString(font, text, 0, 0, color);
        graphics.pose().popPose();
    }

    private void drawCenteredScaledString(GuiGraphics graphics, Component component, int centerX, int y, int color, float scale) {
        int scaledWidth = Math.round(font.width(component) * scale);
        drawScaledString(graphics, component, centerX - scaledWidth / 2, y, color, scale);
    }

    private double clampScroll(double scroll, int contentHeight, int visibleHeight) {
        return Mth.clamp(scroll, 0D, Math.max(0, contentHeight - visibleHeight));
    }

    private Component trim(Component component, int width) {
        return Component.literal(font.plainSubstrByWidth(component.getString(), Math.max(8, width)));
    }

    private Component trim(Component component, int width, float scale) {
        return Component.literal(font.plainSubstrByWidth(component.getString(), Math.max(8, Math.round(width / Math.max(0.1F, scale)))));
    }

    private void renderScrollingChapterLabel(GuiGraphics graphics, Component component, Rect buttonRect, int textLeft, int textY,
                                             int availableWidth, int color, boolean animate) {
        int textWidth = Math.round(font.width(component) * CHAPTER_SELECTOR_TEXT_SCALE);
        if (textWidth <= availableWidth) {
            drawScaledString(graphics, component, textLeft, textY, color, CHAPTER_SELECTOR_TEXT_SCALE);
            return;
        }

        int overflow = textWidth - availableWidth;
        int offset = animate ? Mth.floor(getMarqueeOffset(overflow)) : 0;
        graphics.enableScissor(textLeft, buttonRect.y(), textLeft + availableWidth, buttonRect.maxY());
        drawScaledString(graphics, component, textLeft - offset, textY, color, CHAPTER_SELECTOR_TEXT_SCALE);
        graphics.disableScissor();
    }

    private double getMarqueeOffset(int overflow) {
        if (overflow <= 0) {
            return 0D;
        }

        double holdDuration = 700D;
        double travelDuration = Math.max(1200D, overflow * 60D);
        double cycleDuration = holdDuration + travelDuration + holdDuration + travelDuration;
        double cyclePos = Util.getMillis() % cycleDuration;

        if (cyclePos < holdDuration) {
            return 0D;
        }
        cyclePos -= holdDuration;

        if (cyclePos < travelDuration) {
            return overflow * (cyclePos / travelDuration);
        }
        cyclePos -= travelDuration;

        if (cyclePos < holdDuration) {
            return overflow;
        }
        cyclePos -= holdDuration;

        return overflow * (1D - (cyclePos / travelDuration));
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

    // ---- Records ----

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
            Rect headerRect,
            Rect bodyRect,
            Rect footerRect,
            List<FormattedCharSequence> objectiveLines,
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

        boolean intersects(Rect other) {
            return maxX() > other.x() && x < other.maxX() && maxY() > other.y() && y < other.maxY();
        }
    }
}
