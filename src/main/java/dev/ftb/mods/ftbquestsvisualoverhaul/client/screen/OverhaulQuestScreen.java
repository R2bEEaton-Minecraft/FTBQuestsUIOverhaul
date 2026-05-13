package dev.ftb.mods.ftbquestsvisualoverhaul.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ui.SelectImageResourceScreen;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
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
import dev.ftb.mods.ftbquestsvisualoverhaul.FTBQuestsVisualOverhaul;
import dev.ftb.mods.ftblibrary.ui.CursorType;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.GameType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private static final int CHAPTER_SELECTOR_TITLE_HEIGHT = 10;
    private static final int CHAPTER_SELECTOR_TITLE_BOTTOM_GAP = 8;
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
    private static final int CHAPTER_SELECTOR_TEXT_Y_OFFSET = 2;
    private static final int CHAPTER_GROUP_TEXT_Y_OFFSET = 5;
    private static final int CHAPTER_SELECTOR_TEXT_RIGHT_PADDING = 5;
    private static final int CHAPTER_SCROLLBAR_WIDTH = 1;
    private static final int CHAPTER_SCROLLBAR_GAP = 2;
    private static final int CHAPTER_SCROLLBAR_HITBOX_WIDTH = 5;
    private static final int CHAPTER_SCROLLBAR_MIN_THUMB_HEIGHT = 11;
    private static final int CHAPTER_SELECTOR_BOTTOM_SHADOW_HEIGHT = 13;
    private static final float CHAPTER_SELECTOR_TEXT_SCALE = 0.6F;
    private static final float CHAPTER_SELECTOR_TITLE_SCALE = 0.7F;
    private static final float CHAPTER_GROUP_TEXT_SCALE = 0.65F;
    private static final int CHAPTER_GROUP_HEADER_HEIGHT = 15;
    private static final int CREATIVE_TREE_BUTTON_HEIGHT = 10;
    private static final int CREATIVE_TREE_BUTTON_PADDING_X = 4;
    private static final int CREATIVE_TREE_BUTTON_MIN_WIDTH = 32;
    private static final float CREATIVE_TREE_BUTTON_TEXT_SCALE = 0.5F;
    private static final int MODE_SWITCH_BUTTON_HEIGHT = 14;
    private static final int MODE_SWITCH_BUTTON_PADDING_X = 6;
    private static final int MODE_SWITCH_BUTTON_MIN_WIDTH = 64;
    private static final int MODE_SWITCH_BUTTON_MARGIN = 8;
    private static final int ACTIVE_CHAPTER_TITLE_TOP_PADDING = 12;
    private static final int ACTIVE_CHAPTER_TITLE_BOTTOM_PADDING = 4;
    private static final int ACTIVE_CHAPTER_TITLE_SIDE_PADDING = 5;
    private static final int ACTIVE_CHAPTER_TITLE_ICON_SIZE = 10;
    private static final int ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING = 4;
    private static final int ACTIVE_CHAPTER_TITLE_Y_OFFSET = -1;
    private static final float ACTIVE_CHAPTER_TITLE_TEXT_Y_OFFSET = 0.5F;
    private static final float ACTIVE_CHAPTER_TITLE_ICON_EXTRA_Y_OFFSET = -0.5F;
    private static final float ACTIVE_CHAPTER_TITLE_ICON_Y_OFFSET = 2.0F;
    private static final Component CHAPTER_SELECTOR_TITLE = Component.literal("QUEST CHAPTERS");

    // --- Vanilla advancement textures ---
    private static final ResourceLocation WINDOW_LOCATION = new ResourceLocation("textures/gui/advancements/window.png");
    private static final ResourceLocation TABS_LOCATION = new ResourceLocation("textures/gui/advancements/tabs.png");
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/advancements/widgets.png");
    private static final ResourceLocation ADVANCEMENT_BOX_OBTAINED_TEXTURE = new ResourceLocation("minecraft", "textures/gui/sprites/advancements/box_obtained.png");
    private static final ResourceLocation ADVANCEMENT_BOX_UNOBTAINED_TEXTURE = new ResourceLocation("minecraft", "textures/gui/sprites/advancements/box_unobtained.png");
    private static final ResourceLocation ADVANCEMENT_TITLE_BOX_TEXTURE = new ResourceLocation("minecraft", "textures/gui/sprites/advancements/title_box.png");
    private static final ResourceLocation ADVANCEMENT_TASK_FRAME_OBTAINED_TEXTURE = new ResourceLocation("minecraft", "textures/gui/sprites/advancements/task_frame_obtained.png");
    private static final ResourceLocation ADVANCEMENT_TASK_FRAME_UNOBTAINED_TEXTURE = new ResourceLocation("minecraft", "textures/gui/sprites/advancements/task_frame_unobtained.png");
    private static final ResourceLocation BUTTONS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation OAK_PLANKS_TEXTURE = new ResourceLocation("minecraft", "textures/block/oak_planks.png");
    private static final ResourceLocation OVERHAUL_LOCK_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/icons/lock.png");
    private static final ResourceLocation OVERHAUL_CHECK_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/icons/checked.png");
    private static final ResourceLocation OVERHAUL_NOTIFICATION_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/icons/notification.png");
    private static final int ADVANCEMENT_PANEL_TEXTURE_WIDTH = 200;
    private static final int ADVANCEMENT_PANEL_TEXTURE_HEIGHT = 26;
    private static final int ADVANCEMENT_PANEL_TEXTURE_BORDER = 10;
    private static final int ADVANCEMENT_FRAME_TEXTURE_SIZE = 26;

    // --- Node positioning ---
    // FTB Quests stores authored quest positions directly in chapter data.
    // Use one shared scale for both axes so custom layouts keep their original
    // proportions instead of being distorted by per-axis normalization.
    private static final double NODE_POSITION_SCALE = 14.0D;
    private static final int NODE_MIN_TILE_GAP = 6;
    private static final int NODE_LAYOUT_RELAXATION_PASSES = 24;
    private static final int WIDGET_WIDTH = 26;
    private static final int WIDGET_HEIGHT = 26;
    private static final int WIDGET_ICON_X = 8;
    private static final int WIDGET_ICON_Y = 5;
    private static final int WIDGET_ICON_SIZE = 16;
    private static final int WIDGET_FRAME_X_OFFSET = 3;
    private static final int QUEST_TILE_SHADOW_OFFSET = 2;
    private static final int QUEST_TILE_SHADOW_COLOR = 0x26000000;
    private static final int QUEST_TILE_SHADOW_EDGE_COLOR = 0x33000000;
    private static final int STATUS_CHECK_ICON_WIDTH = 9;
    private static final int STATUS_CHECK_ICON_HEIGHT = 9;
    private static final int STATUS_LOCK_ICON_WIDTH = 9;
    private static final int STATUS_LOCK_ICON_HEIGHT = 12;
    private static final int STATUS_NOTIFICATION_ICON_WIDTH = 9;
    private static final int STATUS_NOTIFICATION_ICON_HEIGHT = 9;
    private static final int STATUS_OVERLAY_RIGHT_INSET = -1;
    private static final int TREE_PAN_BOUND_PADDING = 12;
    private static final double TREE_ZOOM_MIN = 0.5D;
    private static final double TREE_ZOOM_MAX = 1.75D;
    private static final double TREE_ZOOM_SCROLL_FACTOR = 1.125D;
    private static final int HOVER_OUTGOING_LINE_COLOR = 0xFFF2D34F;
    private static final int HOVER_INCOMING_LINE_COLOR = 0xFF58B9FF;

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
    private static final int MODAL_MIN_WIDTH = 244;
    private static final int MODAL_MAX_WIDTH = 286;
    private static final int MODAL_HEADER_HEIGHT = 26;
    private static final int MODAL_FOOTER_HEIGHT = 30;
    private static final int MODAL_MARGIN = 28;
    private static final int FOOTER_BUTTON_HEIGHT = 15;
    private static final int MODAL_CONTENT_SIDE_PADDING = 14;
    private static final int MODAL_CONTENT_TOP_PADDING = 10;
    private static final int MODAL_CONTENT_BOTTOM_PADDING = 10;
    private static final int MODAL_SECTION_DIVIDER_INSET = 8;
    private static final int MODAL_BUTTON_WIDTH = 86;
    private static final int MODAL_BUTTON_GAP = 26;
    private static final float MODAL_TEXT_SCALE = 0.8F;
    private static final float MODAL_SECTION_LABEL_SCALE = 0.65F;
    private static final float MODAL_SECTION_NOTE_SCALE = 0.58F;
    private static final float MODAL_DESCRIPTION_SCALE = MODAL_SECTION_LABEL_SCALE;
    private static final float MODAL_SECTION_COUNT_SCALE = 0.34F;
    private static final int MODAL_SECTION_ITEM_GAP = 4;
    private static final int MODAL_SECTION_GROUP_GAP = 12;
    private static final int MODAL_SECTION_LABEL_GAP = 4;
    private static final int MODAL_SECTION_ICON_SIZE = 16;
    private static final int MODAL_SECTION_COUNT_GAP = 1;
    private static final int MODAL_SECTION_ICON_GAP_X = 2;
    private static final int MODAL_SECTION_ICON_GAP_Y = 4;
    private static final int MODAL_SECTION_MAX_COLUMNS = 4;

    private final QuestOpenContext openContext;
    private final QuestActionRouter actionRouter = new QuestActionRouter();
    private final QuestViewState viewState;
    private final List<ClickTarget> clickTargets = new ArrayList<>();
    private final List<TooltipTarget> modalTooltipTargets = new ArrayList<>();
    private final Set<ResourceLocation> missingModalTextures = new HashSet<>();

    private final Map<Long, NodeLayout> visibleQuestNodes = new HashMap<>();
    private final Map<Long, Rect> adjustedNodeWorldRects = new HashMap<>();
    private QuestDataSnapshot.QuestSnapshot hoveredQuest;
    private boolean isScrolling;
    private boolean chapterScrollbarDragging;
    private long focusedChapterId = Long.MIN_VALUE;
    private float fade;
    private boolean centered;
    private boolean openingQuestSnapPending;
    private int tabPage;
    private int maxPages;
    private double chapterScroll;

    // Scroll bounds tracking (from AdvancementTab)
    private int minNodeX = Integer.MAX_VALUE;
    private int minNodeY = Integer.MAX_VALUE;
    private int maxNodeX = Integer.MIN_VALUE;
    private int maxNodeY = Integer.MIN_VALUE;

    public OverhaulQuestScreen(QuestOpenContext openContext) {
        super(Component.literal("Quests"));
        this.openContext = openContext;
        this.viewState = openContext.previousState().copy();
        this.openingQuestSnapPending = openContext.requestedQuestId() == 0L;

        if (openContext.requestedChapterId() != 0L) {
            viewState.setSelectedChapterId(openContext.requestedChapterId());
        }
        if (openContext.requestedQuestId() != 0L) {
            viewState.setViewedQuestId(openContext.requestedQuestId());
        } else {
            viewState.setViewedQuestId(0L);
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
            if (!modalBounds(layout).contains(mouseX, mouseY)) {
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
        if (viewState.getViewedQuestId() != 0L) {
            return true;
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
            return true;
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
        if (handleTreeZoom(snapshot, mouseX, mouseY, deltaY)) {
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

    private boolean handleTreeZoom(QuestDataSnapshot snapshot, double mouseX, double mouseY, double deltaY) {
        Rect treeViewport = treeViewportRect();
        if (deltaY == 0D || !treeViewport.contains(mouseX, mouseY)) {
            return false;
        }

        QuestDataSnapshot.ChapterSnapshot chapter = snapshot.findChapter(viewState.getSelectedChapterId());
        if (chapter == null || chapter.quests().isEmpty()) {
            return true;
        }

        double oldZoom = viewState.getTreeZoom();
        double newZoom = Mth.clamp(oldZoom * Math.pow(TREE_ZOOM_SCROLL_FACTOR, deltaY), TREE_ZOOM_MIN, TREE_ZOOM_MAX);
        if (Math.abs(newZoom - oldZoom) < 0.0001D) {
            return true;
        }

        double relMouseX = mouseX - treeViewport.x();
        double relMouseY = mouseY - treeViewport.y();
        double contentX = (relMouseX - viewState.getTreePanX()) / oldZoom;
        double contentY = (relMouseY - viewState.getTreePanY()) / oldZoom;

        viewState.setTreeZoom(newZoom);
        viewState.setTreePanX(relMouseX - contentX * newZoom);
        viewState.setTreePanY(relMouseY - contentY * newZoom);

        computeNodeBounds(chapter.quests());
        clampTreePanToQuestBounds();

        return true;
    }

    // ---- Scroll/Pan (matching vanilla AdvancementTab.scroll) ----

    private void scroll(double deltaX, double deltaY) {
        viewState.setTreePanX(clampTreePanAxis(viewState.getTreePanX() + deltaX, minNodeX, maxNodeX, TREE_WIDTH));
        viewState.setTreePanY(clampTreePanAxis(viewState.getTreePanY() + deltaY, minNodeY, maxNodeY, TREE_HEIGHT));
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
        modalTooltipTargets.clear();

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
        renderCreativeTreeControls(graphics, mouseX, mouseY, selectedQuest == null);

        super.render(graphics, mouseX, mouseY, partialTick);

        // Tooltips (rendered after everything else so they appear on top)
        renderTooltips(graphics, snapshot, mouseX, mouseY, treeLeft, treeTop);

        // Detail modal overlay (quest-specific, not in vanilla advancements)
        if (renderDetailModal && selectedQuest != null) {
            hoveredQuest = null;
            clickTargets.clear();
            modalTooltipTargets.clear();
            renderQuestDetailModal(graphics, selectedQuest, mouseX, mouseY);
            renderModalTooltips(graphics, mouseX, mouseY);
        }

        if (selectedQuest == null && renderDefaultButton && shouldShowDefaultViewButton()) {
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
            // Center the zoomed world bounds inside the viewport.
            viewState.setTreePanX((TREE_WIDTH / 2.0D) - ((maxNodeX + minNodeX) * viewState.getTreeZoom()) / 2.0D);
            viewState.setTreePanY((TREE_HEIGHT / 2.0D) - ((maxNodeY + minNodeY) * viewState.getTreeZoom()) / 2.0D);
            centered = true;
        }
        clampTreePanToQuestBounds();

        // Scissor to tree panel area (matching AdvancementTab.drawContents)
        graphics.enableScissor(treeLeft, treeTop, treeLeft + TREE_WIDTH, treeTop + TREE_HEIGHT);
        graphics.pose().pushPose();
        graphics.pose().translate((float) treeLeft, (float) treeTop, 0.0F);

        // Tile background (matching AdvancementTab: 16x16 tiles with scroll offset)
        ResourceLocation tileTexture = resolveTileTexture(viewState.getChapterTitleTexture(chapter.id()));
        if (tileTexture == null) {
            tileTexture = OAK_PLANKS_TEXTURE;
        }
        graphics.pose().pushPose();
        graphics.pose().translate((float) viewState.getTreePanX(), (float) viewState.getTreePanY(), 0.0F);
        graphics.pose().scale((float) viewState.getTreeZoom(), (float) viewState.getTreeZoom(), 1.0F);

        double zoom = viewState.getTreeZoom();
        int firstTileX = Mth.floor((-viewState.getTreePanX() / zoom) / 16.0D) - 1;
        int lastTileX = Mth.ceil((TREE_WIDTH - viewState.getTreePanX()) / zoom / 16.0D) + 1;
        int firstTileY = Mth.floor((-viewState.getTreePanY() / zoom) / 16.0D) - 1;
        int lastTileY = Mth.ceil((TREE_HEIGHT - viewState.getTreePanY()) / zoom / 16.0D) + 1;
        for (int tx = firstTileX; tx <= lastTileX; ++tx) {
            for (int ty = firstTileY; ty <= lastTileY; ++ty) {
                graphics.blit(tileTexture, tx * 16, ty * 16, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }

        hoveredQuest = findHoveredQuest(quests, mouseX - treeLeft, mouseY - treeTop);

        // Draw connections (matching AdvancementWidget.drawConnectivity)
        // First pass: black outline, second pass: white center
        drawAllConnections(graphics, quests, true);
        drawAllConnections(graphics, quests, false);
        if (hoveredQuest != null) {
            drawHoveredQuestConnections(graphics, quests, hoveredQuest, true);
            drawHoveredQuestConnections(graphics, quests, hoveredQuest, false);
        }

        // Draw widgets (matching AdvancementWidget.draw)
        drawAllWidgets(graphics, quests, mouseX - treeLeft, mouseY - treeTop);

        graphics.pose().popPose();
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private void renderChapterSelector(GuiGraphics graphics, QuestDataSnapshot snapshot, int mouseX, int mouseY, boolean interactive) {
        renderChapterSelectorHeader(graphics);
        Rect viewportRect = chapterSelectorViewportRect();
        clampChapterScroll(snapshot);
        int y = viewportRect.y() - Mth.floor(chapterScroll);
        List<ChapterRowLayout> selectedRows = new ArrayList<>();

        graphics.enableScissor(viewportRect.x(), viewportRect.y(), viewportRect.maxX(), viewportRect.maxY());

        for (QuestDataSnapshot.ChapterSnapshot chapter : snapshot.chapters()) {
            if (chapter.firstInGroup()) {
                boolean collapsed = isChapterGroupCollapsed(chapter.groupId());
                Rect headerRect = new Rect(viewportRect.x(), y, CHAPTER_BUTTON_REGULAR_WIDTH, CHAPTER_GROUP_HEADER_HEIGHT);
                boolean headerHovered = interactive && headerRect.contains(mouseX, mouseY);
                if (headerRect.intersects(viewportRect)) {
                    graphics.fill(headerRect.x(), headerRect.y() + 1, headerRect.maxX() - 4, headerRect.maxY() - 1,
                            headerHovered ? 0x99221712 : 0x77140E0C);
                    drawScaledString(graphics, Component.literal(collapsed ? ">" : "v"),
                            headerRect.x() + 4, headerRect.y() + CHAPTER_GROUP_TEXT_Y_OFFSET, 0xFFF0E4CB, CHAPTER_GROUP_TEXT_SCALE);
                    drawScaledString(graphics, trim(chapter.groupTitle(), headerRect.width() - 17, CHAPTER_GROUP_TEXT_SCALE),
                            headerRect.x() + 13, headerRect.y() + CHAPTER_GROUP_TEXT_Y_OFFSET, 0xFFF0E4CB, CHAPTER_GROUP_TEXT_SCALE);
                }
                if (interactive) {
                    addClippedClickTarget(headerRect, viewportRect, () -> toggleChapterGroup(chapter.groupId()));
                }
                y += CHAPTER_GROUP_HEADER_HEIGHT;
                if (collapsed) {
                    continue;
                }
            }
            if (isChapterGroupCollapsed(chapter.groupId())) {
                continue;
            }

            boolean selected = chapter.id() == viewState.getSelectedChapterId();
            int buttonWidth = selected ? CHAPTER_BUTTON_ACTIVE_WIDTH : CHAPTER_BUTTON_REGULAR_WIDTH;
            int textureWidth = CHAPTER_BUTTON_TEXTURE_WIDTH;
            Rect buttonRect = new Rect(viewportRect.x(), y, buttonWidth, CHAPTER_BUTTON_HEIGHT);
            Rect textureRect = new Rect(viewportRect.x(), y - CHAPTER_BUTTON_TEXTURE_Y_OFFSET, textureWidth, CHAPTER_BUTTON_TEXTURE_HEIGHT);
            boolean visible = textureRect.intersects(viewportRect);
            boolean hovered = visible && buttonRect.contains(mouseX, mouseY);

            if (visible) {
                if (selected) {
                    selectedRows.add(new ChapterRowLayout(chapter, buttonRect, textureRect, hovered));
                } else {
                    renderChapterSelectorRow(graphics, chapter, buttonRect, textureRect, hovered, false);
                }
            }

            if (interactive && buttonRect.intersects(viewportRect)) {
                addClippedClickTarget(buttonRect, viewportRect, () -> selectChapter(chapter.id()));
            }

            y += CHAPTER_SELECTOR_SCROLL_STEP;
        }

        renderChapterSelectorBottomShadow(graphics, viewportRect, snapshot);
        for (ChapterRowLayout selectedRow : selectedRows) {
            renderChapterSelectorRow(graphics, selectedRow.chapter(), selectedRow.buttonRect(), selectedRow.textureRect(), selectedRow.hovered(), true);
        }
        graphics.disableScissor();
        renderChapterScrollbar(graphics, snapshot);
    }

    private void renderChapterSelectorRow(GuiGraphics graphics, QuestDataSnapshot.ChapterSnapshot chapter, Rect buttonRect,
                                          Rect textureRect, boolean hovered, boolean selected) {
        ResourceLocation texture = selected ? CHAPTER_BUTTON_ACTIVE_TEXTURE : hovered ? CHAPTER_BUTTON_HOVER_TEXTURE : CHAPTER_BUTTON_INACTIVE_TEXTURE;
        RenderSystem.enableBlend();
        graphics.blit(texture, textureRect.x(), textureRect.y(), 0, 0, textureRect.width(), textureRect.height(),
                CHAPTER_BUTTON_TEXTURE_WIDTH, CHAPTER_BUTTON_TEXTURE_HEIGHT);
        RenderSystem.disableBlend();

        int iconX = buttonRect.x() + CHAPTER_SELECTOR_ICON_X_OFFSET;
        int iconY = buttonRect.y() + Math.max(0, (buttonRect.height() - CHAPTER_SELECTOR_ICON_SIZE) / 2);
        chapter.icon().draw(graphics, iconX, iconY, CHAPTER_SELECTOR_ICON_SIZE, CHAPTER_SELECTOR_ICON_SIZE);

        float scaledTextHeight = font.lineHeight * CHAPTER_SELECTOR_TEXT_SCALE;
        int textLeft = buttonRect.x() + CHAPTER_SELECTOR_TEXT_X_OFFSET;
        int textY = Mth.floor(buttonRect.y() + (buttonRect.height() - scaledTextHeight) * 0.5F) + CHAPTER_SELECTOR_TEXT_Y_OFFSET;
        int availableWidth = buttonRect.maxX() - textLeft - CHAPTER_SELECTOR_TEXT_RIGHT_PADDING;
        int textColor = selected ? 0xFFF2E8D6 : hovered ? 0xFFF6EAD0 : 0xFFE3D5BE;
        renderScrollingChapterLabel(graphics, chapter.title(), buttonRect, textLeft, textY, availableWidth, textColor, hovered || selected);
    }

    private void renderChapterSelectorHeader(GuiGraphics graphics) {
        Rect headerRect = chapterSelectorHeaderRect();
        int titleY = headerRect.y() + Math.max(0, (headerRect.height() - Math.round(font.lineHeight * CHAPTER_SELECTOR_TITLE_SCALE)) / 2);
        int titleCenterX = headerRect.x() + CHAPTER_BUTTON_REGULAR_WIDTH / 2;
        drawCenteredScaledString(graphics, CHAPTER_SELECTOR_TITLE, titleCenterX, titleY, 0xFFF2E8D6, CHAPTER_SELECTOR_TITLE_SCALE);
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
        Component title = Component.literal(chapter.title().getString().toUpperCase(Locale.ROOT));
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

        int scaledTextHeight = Math.max(1, Math.round(font.lineHeight * CHAPTER_SELECTOR_TITLE_SCALE));
        int rowTextOffset = Mth.floor((CHAPTER_BUTTON_HEIGHT - scaledTextHeight) * 0.5F) + 2;
        int rowY = frame.y() + ACTIVE_CHAPTER_TITLE_TOP_PADDING - rowTextOffset + ACTIVE_CHAPTER_TITLE_Y_OFFSET;
        float textY = rowY + rowTextOffset + ACTIVE_CHAPTER_TITLE_TEXT_Y_OFFSET;
        float iconY = rowY + Math.max(0, (CHAPTER_BUTTON_HEIGHT - ACTIVE_CHAPTER_TITLE_ICON_SIZE) / 2)
                + ACTIVE_CHAPTER_TITLE_ICON_Y_OFFSET + ACTIVE_CHAPTER_TITLE_ICON_EXTRA_Y_OFFSET;
        int separatorWidth = Math.round(font.width(separator) * CHAPTER_SELECTOR_TITLE_SCALE);
        int titleWidth = Math.round(font.width(title) * CHAPTER_SELECTOR_TITLE_SCALE);
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
        drawScaledString(graphics, separator, cursorX, textY, color, CHAPTER_SELECTOR_TITLE_SCALE, true);
        cursorX += separatorWidth + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING;
        drawScaledString(graphics, title, cursorX, textY, color, CHAPTER_SELECTOR_TITLE_SCALE, true);
        cursorX += titleWidth + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING;
        drawScaledString(graphics, separator, cursorX, textY, color, CHAPTER_SELECTOR_TITLE_SCALE, true);
        cursorX += separatorWidth + ACTIVE_CHAPTER_TITLE_ELEMENT_SPACING;
        graphics.pose().pushPose();
        graphics.pose().translate(cursorX, iconY, 0F);
        chapter.icon().draw(graphics, 0, 0, ACTIVE_CHAPTER_TITLE_ICON_SIZE, ACTIVE_CHAPTER_TITLE_ICON_SIZE);
        graphics.pose().popPose();
        graphics.disableScissor();
    }

    private void renderCreativeTreeControls(GuiGraphics graphics, int mouseX, int mouseY, boolean interactive) {
        if (!isCreativeControlsVisible()) {
            return;
        }

        Rect frame = frameRect();
        Component freePanLabel = Component.literal(isCurrentChapterFreePan() ? "Free Pan: On" : "Free Pan: Off");
        Component titleLabel = Component.literal("Change Tile");
        int buttonY = frame.y() + TREE_Y + 1;
        Rect freePanButton = creativeTreeButtonRect(frame.x() + TREE_X + 1, buttonY, freePanLabel);
        Rect titleButton = creativeTreeButtonRectRight(frame.x() + TREE_X + TREE_WIDTH - 4, buttonY, titleLabel);
        renderCreativeTreeButton(graphics, freePanButton, freePanLabel, interactive && freePanButton.contains(mouseX, mouseY));
        renderCreativeTreeButton(graphics, titleButton, titleLabel, interactive && titleButton.contains(mouseX, mouseY));

        if (interactive) {
            clickTargets.add(new ClickTarget(freePanButton, this::toggleCurrentChapterFreePan));
            clickTargets.add(new ClickTarget(titleButton, this::openChapterTitleTextureSelector));
        }
    }

    private Rect creativeTreeButtonRect(int x, int y, Component label) {
        int width = Math.max(CREATIVE_TREE_BUTTON_MIN_WIDTH,
                Math.round(font.width(label) * CREATIVE_TREE_BUTTON_TEXT_SCALE) + CREATIVE_TREE_BUTTON_PADDING_X * 2);
        return new Rect(x, y, width, CREATIVE_TREE_BUTTON_HEIGHT);
    }

    private Rect creativeTreeButtonRectRight(int rightX, int y, Component label) {
        int width = Math.max(CREATIVE_TREE_BUTTON_MIN_WIDTH,
                Math.round(font.width(label) * CREATIVE_TREE_BUTTON_TEXT_SCALE) + CREATIVE_TREE_BUTTON_PADDING_X * 2);
        return new Rect(rightX - width, y, width, CREATIVE_TREE_BUTTON_HEIGHT);
    }

    private void renderCreativeTreeButton(GuiGraphics graphics, Rect rect, Component label, boolean hovered) {
        drawVanillaButton(graphics, rect, true, hovered);
        float scaledTextWidth = font.width(label) * CREATIVE_TREE_BUTTON_TEXT_SCALE;
        float scaledTextHeight = font.lineHeight * CREATIVE_TREE_BUTTON_TEXT_SCALE;
        float textX = rect.x() + (rect.width() - scaledTextWidth) * 0.5F;
        float textY = rect.y() + (rect.height() - scaledTextHeight) * 0.5F;
        drawScaledString(graphics, label, textX, textY, hovered ? 0xFFFFA0 : 0xE0E0E0, CREATIVE_TREE_BUTTON_TEXT_SCALE);
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

    private boolean isCurrentChapterFreePan() {
        return viewState.isFreePan(viewState.getSelectedChapterId());
    }

    private void toggleCurrentChapterFreePan() {
        long chapterId = viewState.getSelectedChapterId();
        viewState.setFreePan(chapterId, !viewState.isFreePan(chapterId));
        QuestDataController.saveViewState(viewState);
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
            Rect viewportRect = localTreeViewportRect();

            if (relMouseX > 0 && relMouseX < TREE_WIDTH && relMouseY > 0 && relMouseY < TREE_HEIGHT) {
                for (QuestDataSnapshot.QuestSnapshot quest : chapter.quests()) {
                    Rect nodeRect = getNodeScreenRect(quest);
                    Rect visibleRect = clipRect(nodeRect, viewportRect);
                    if (visibleRect != null && visibleRect.contains(relMouseX, relMouseY)) {
                        foundHovered = true;
                        drawWidgetHover(graphics, quest, this.fade, treeLeft, treeTop);
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
     * Converts the quest's authored FTB center position into a widget top-left.
     * FTB Quests stores coordinates as center positions in chapter space.
     */
    private int getNodeX(QuestDataSnapshot.QuestSnapshot quest) {
        return Mth.floor(quest.x() * NODE_POSITION_SCALE - WIDGET_WIDTH / 2F);
    }

    private int getNodeY(QuestDataSnapshot.QuestSnapshot quest) {
        return Mth.floor(quest.y() * NODE_POSITION_SCALE - WIDGET_HEIGHT / 2F);
    }

    private Rect getAuthoredNodeWorldRect(QuestDataSnapshot.QuestSnapshot quest) {
        int nodeX = getNodeX(quest);
        int nodeY = getNodeY(quest);
        return new Rect(nodeX, nodeY, WIDGET_WIDTH, WIDGET_HEIGHT);
    }

    private Rect getNodeWorldRect(QuestDataSnapshot.QuestSnapshot quest) {
        return adjustedNodeWorldRects.getOrDefault(quest.id(), getAuthoredNodeWorldRect(quest));
    }

    private Rect getNodeScreenRect(QuestDataSnapshot.QuestSnapshot quest) {
        Rect nodeRect = getNodeWorldRect(quest);
        int x = projectTreeX(nodeRect.x());
        int y = projectTreeY(nodeRect.y());
        int maxX = projectTreeMaxX(nodeRect.x() + nodeRect.width());
        int maxY = projectTreeMaxY(nodeRect.y() + nodeRect.height());
        return new Rect(x, y, maxX - x, maxY - y);
    }

    /**
     * Compute min/max node bounds (matching AdvancementTab.addWidget bounds tracking)
     * from the authored quest positions after mapping them into widget space.
     */
    private void computeNodeBounds(List<QuestDataSnapshot.QuestSnapshot> quests) {
        adjustedNodeWorldRects.clear();
        minNodeX = Integer.MAX_VALUE;
        minNodeY = Integer.MAX_VALUE;
        maxNodeX = Integer.MIN_VALUE;
        maxNodeY = Integer.MIN_VALUE;

        if (quests.isEmpty()) {
            return;
        }

        List<MutableNodePosition> nodes = new ArrayList<>(quests.size());
        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            Rect rect = getAuthoredNodeWorldRect(quest);
            nodes.add(new MutableNodePosition(quest.id(), rect.x(), rect.y()));
        }

        relaxNodeOverlaps(nodes);

        for (MutableNodePosition node : nodes) {
            Rect rect = node.toRect();
            adjustedNodeWorldRects.put(node.id(), rect);
            minNodeX = Math.min(minNodeX, rect.x());
            maxNodeX = Math.max(maxNodeX, rect.maxX());
            minNodeY = Math.min(minNodeY, rect.y());
            maxNodeY = Math.max(maxNodeY, rect.maxY());
        }
    }

    private void relaxNodeOverlaps(List<MutableNodePosition> nodes) {
        int minHorizontalSpacing = WIDGET_WIDTH + NODE_MIN_TILE_GAP;
        int minVerticalSpacing = WIDGET_HEIGHT + NODE_MIN_TILE_GAP;

        for (int pass = 0; pass < NODE_LAYOUT_RELAXATION_PASSES; pass++) {
            boolean moved = false;

            for (int i = 0; i < nodes.size(); i++) {
                MutableNodePosition first = nodes.get(i);
                for (int j = i + 1; j < nodes.size(); j++) {
                    MutableNodePosition second = nodes.get(j);
                    int deltaX = second.x() - first.x();
                    int deltaY = second.y() - first.y();
                    int overlapX = minHorizontalSpacing - Math.abs(deltaX);
                    int overlapY = minVerticalSpacing - Math.abs(deltaY);
                    if (overlapX <= 0 || overlapY <= 0) {
                        continue;
                    }

                    boolean separateOnX = overlapX < overlapY
                            || overlapX == overlapY && (Math.abs(deltaX) > Math.abs(deltaY)
                            || Math.abs(deltaX) == Math.abs(deltaY) && ((i + j) & 1) == 0);

                    if (separateOnX) {
                        int direction = separationDirection(deltaX, i, j);
                        int firstPush = overlapX / 2;
                        int secondPush = overlapX - firstPush;
                        first.moveX(-direction * firstPush);
                        second.moveX(direction * secondPush);
                    } else {
                        int direction = separationDirection(deltaY, i, j);
                        int firstPush = overlapY / 2;
                        int secondPush = overlapY - firstPush;
                        first.moveY(-direction * firstPush);
                        second.moveY(direction * secondPush);
                    }

                    moved = true;
                }
            }

            if (!moved) {
                return;
            }
        }
    }

    private int separationDirection(int delta, int firstIndex, int secondIndex) {
        if (delta < 0) {
            return -1;
        }
        if (delta > 0) {
            return 1;
        }
        return ((firstIndex + secondIndex) & 1) == 0 ? 1 : -1;
    }

    private void clampTreePanToQuestBounds() {
        viewState.setTreePanX(clampTreePanAxis(viewState.getTreePanX(), minNodeX, maxNodeX, TREE_WIDTH));
        viewState.setTreePanY(clampTreePanAxis(viewState.getTreePanY(), minNodeY, maxNodeY, TREE_HEIGHT));
    }

    private double clampTreePanAxis(double pan, int minNode, int maxNode, int viewportSize) {
        if (isCurrentChapterFreePan()) {
            return pan;
        }

        double zoom = viewState.getTreeZoom();
        int padding = TREE_PAN_BOUND_PADDING;
        double minContent = (minNode - padding) * zoom;
        double maxContent = (maxNode + padding) * zoom;
        double contentSize = maxContent - minContent;
        if (contentSize <= viewportSize) {
            return (viewportSize - contentSize) / 2.0D - minContent;
        }

        double minPan = viewportSize - maxContent;
        double maxPan = -minContent;
        return Mth.clamp(pan, minPan, maxPan);
    }

    // ---- Connection line drawing ----

    /**
     * Draws stable advancement-style elbow connections between quest nodes.
     */
    private void drawAllConnections(GuiGraphics graphics, List<QuestDataSnapshot.QuestSnapshot> quests, boolean outline) {
        Map<Long, QuestDataSnapshot.QuestSnapshot> questMap = new HashMap<>();
        for (QuestDataSnapshot.QuestSnapshot q : quests) {
            questMap.put(q.id(), q);
        }

        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            for (Long depId : quest.dependencyQuestIds()) {
                QuestDataSnapshot.QuestSnapshot parent = questMap.get(depId);
                if (parent == null) continue;

                Rect parentRect = getNodeWorldRect(parent);
                Rect childRect = getNodeWorldRect(quest);

                int lineColor = outline ? 0xFF000000 : parent.completed() && isQuestAvailable(quest) ? 0xFF35E041 : 0xFFFFFFFF;

                int startX = parentRect.centerX();
                int startY = parentRect.centerY();
                int endX = childRect.centerX();
                int endY = childRect.centerY();
                int bendX = (startX + endX) / 2;
                int thickness = outline ? 3 : 1;

                drawElbowLine(graphics, startX, bendX, startY, endX, endY, thickness, lineColor);
            }
        }
    }

    private void drawHoveredQuestConnections(GuiGraphics graphics, List<QuestDataSnapshot.QuestSnapshot> quests,
                                             QuestDataSnapshot.QuestSnapshot hovered, boolean outline) {
        Map<Long, QuestDataSnapshot.QuestSnapshot> questMap = new HashMap<>();
        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            questMap.put(quest.id(), quest);
        }

        int thickness = outline ? 5 : 3;
        for (Long depId : hovered.dependencyQuestIds()) {
            QuestDataSnapshot.QuestSnapshot parent = questMap.get(depId);
            if (parent != null) {
                drawConnection(graphics, parent, hovered, thickness, outline ? 0xFF000000 : HOVER_INCOMING_LINE_COLOR);
            }
        }

        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            if (quest.dependencyQuestIds().contains(hovered.id())) {
                drawConnection(graphics, hovered, quest, thickness, outline ? 0xFF000000 : HOVER_OUTGOING_LINE_COLOR);
            }
        }
    }

    private void drawConnection(GuiGraphics graphics, QuestDataSnapshot.QuestSnapshot parent,
                                QuestDataSnapshot.QuestSnapshot child, int thickness, int color) {
        Rect parentRect = getNodeWorldRect(parent);
        Rect childRect = getNodeWorldRect(child);

        int startX = parentRect.centerX();
        int startY = parentRect.centerY();
        int endX = childRect.centerX();
        int endY = childRect.centerY();
        int bendX = (startX + endX) / 2;

        drawElbowLine(graphics, startX, bendX, startY, endX, endY, thickness, color);
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

    private QuestDataSnapshot.QuestSnapshot findHoveredQuest(List<QuestDataSnapshot.QuestSnapshot> quests, int relMouseX, int relMouseY) {
        if (relMouseX <= 0 || relMouseX >= TREE_WIDTH || relMouseY <= 0 || relMouseY >= TREE_HEIGHT) {
            return null;
        }

        Rect viewportRect = localTreeViewportRect();
        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            Rect screenRect = getNodeScreenRect(quest);
            Rect visibleRect = clipRect(screenRect, viewportRect);
            if (visibleRect != null && visibleRect.contains(relMouseX, relMouseY)) {
                return quest;
            }
        }

        return null;
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
    private void drawAllWidgets(GuiGraphics graphics, List<QuestDataSnapshot.QuestSnapshot> quests, int relMouseX, int relMouseY) {
        Rect viewportRect = localTreeViewportRect();
        for (QuestDataSnapshot.QuestSnapshot quest : quests) {
            Rect worldRect = getNodeWorldRect(quest);
            Rect screenRect = getNodeScreenRect(quest);
            int nodeX = worldRect.x();
            int nodeY = worldRect.y();

            boolean obtained = quest.completed() || quest.hasUnclaimedRewards();
            int widgetTypeIndex = obtained ? 0 : 1;

            // Frame texture U: 0=TASK, 26=CHALLENGE, 52=GOAL
            // We use TASK (0) as the default frame for all quests
            int frameU = 0;

            renderQuestWidgetShadow(graphics, worldRect);

            // Matching vanilla: blit at (scrollX + x + 3, scrollY + y)
            graphics.blit(WIDGETS_LOCATION,
                    nodeX + WIDGET_FRAME_X_OFFSET, nodeY,
                    frameU, 128 + widgetTypeIndex * 26,
                    WIDGET_WIDTH, WIDGET_HEIGHT);

            // Matching vanilla: render icon at (scrollX + x + 8, scrollY + y + 5)
            quest.icon().draw(graphics, nodeX + WIDGET_ICON_X, nodeY + WIDGET_ICON_Y, WIDGET_ICON_SIZE, WIDGET_ICON_SIZE);

            if (!obtained && !isQuestAvailable(quest)) {
                renderLockOverlay(graphics, worldRect);
            }
            if (questNeedsAttention(quest)) {
                renderAlertOverlay(graphics, worldRect);
            }

            // Hit detection for click targets (in screen coordinates)
            Rect visibleScreenRect = clipRect(screenRect, viewportRect);
            if (visibleScreenRect != null) {
                Rect frame = frameRect();
                Rect absoluteRect = new Rect(
                        visibleScreenRect.x() + frame.x() + TREE_X,
                        visibleScreenRect.y() + frame.y() + TREE_Y,
                        visibleScreenRect.width(),
                        visibleScreenRect.height());
                if (viewState.getViewedQuestId() == 0L) {
                    clickTargets.add(new ClickTarget(absoluteRect, () -> {
                        viewState.setViewedQuestId(quest.id());
                        viewState.setDetailScroll(0D);
                    }));
                }

                visibleQuestNodes.put(quest.id(), new NodeLayout(quest, absoluteRect));

                if (visibleScreenRect.contains(relMouseX, relMouseY)) {
                    hoveredQuest = quest;
                }
            }
        }
    }

    private void renderQuestWidgetShadow(GuiGraphics graphics, Rect nodeRect) {
        int x = nodeRect.x() + WIDGET_FRAME_X_OFFSET;
        int y = nodeRect.y();
        int right = x + nodeRect.width();
        int bottom = y + nodeRect.height();
        int shadowOffset = QUEST_TILE_SHADOW_OFFSET;

        graphics.fill(x + shadowOffset, y + shadowOffset,
                right + shadowOffset, bottom + shadowOffset,
                QUEST_TILE_SHADOW_COLOR);
        graphics.fill(x + shadowOffset + 1, bottom + 1,
                right + shadowOffset, bottom + shadowOffset + 1,
                QUEST_TILE_SHADOW_EDGE_COLOR);
        graphics.fill(right + 1, y + shadowOffset + 1,
                right + shadowOffset + 1, bottom + shadowOffset,
                QUEST_TILE_SHADOW_EDGE_COLOR);
    }

    private void renderLockOverlay(GuiGraphics graphics, Rect nodeRect) {
        Rect lockRect = widgetStatusRect(nodeRect, STATUS_LOCK_ICON_WIDTH, STATUS_LOCK_ICON_HEIGHT, -3);
        renderStatusTexture(graphics, OVERHAUL_LOCK_TEXTURE, lockRect);
    }

    private void renderAlertOverlay(GuiGraphics graphics, Rect nodeRect) {
        Rect notificationRect = widgetStatusRect(nodeRect, STATUS_NOTIFICATION_ICON_WIDTH, STATUS_NOTIFICATION_ICON_HEIGHT, -1);
        renderStatusTexture(graphics, OVERHAUL_NOTIFICATION_TEXTURE, notificationRect);
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
    private void drawWidgetHover(GuiGraphics graphics, QuestDataSnapshot.QuestSnapshot quest, float currentFade, int treeLeft, int treeTop) {
        Rect nodeRect = getNodeScreenRect(quest);
        boolean obtained = quest.completed() || quest.hasUnclaimedRewards();
        boolean locked = !obtained && !isQuestAvailable(quest);
        int widgetState = getQuestWidgetState(quest);
        int contentX = nodeRect.x();
        int contentY = nodeRect.y();
        int titleStartX = TITLE_X;

        // Build tooltip text (matching AdvancementWidget constructor logic)
        FormattedCharSequence titleSeq = Language.getInstance().getVisualOrder(font.substrByWidth(quest.title(), TITLE_MAX_WIDTH));
        int titleWidth = font.width(titleSeq);

        // Progress text: only shown when partially complete (not 0% or 100%)
        int progress = quest.progress();
        boolean showProgress = progress > 0 && progress < 100;
        String progressText = progress + "%";
        int progressTextWidth = showProgress ? font.width(progressText) : 0;
        int spaceWidth = showProgress ? font.width(" ") : 0;

        int tooltipWidth = titleStartX - TITLE_PADDING_LEFT + titleWidth + progressTextWidth + spaceWidth;

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
        boolean flipLeft = treeLeft + contentX + tooltipWidth + WIDGET_WIDTH >= this.width;
        boolean flipUp = TREE_HEIGHT - contentY - WIDGET_HEIGHT <= 6 + description.size() * 9;

        // Progress-based title bar split (matching vanilla exactly)
        float percent = quest.progress() / 100.0F;
        int splitJ = Mth.floor(percent * (float) tooltipWidth);

        int obtainedIdx; // left half type
        int unobtainedIdx; // right half type
        int frameIdx; // frame overlay type

        if (widgetState == 2) {
            splitJ = tooltipWidth / 2;
            obtainedIdx = 1; // UNOBTAINED
            unobtainedIdx = 1; // UNOBTAINED
            frameIdx = 1; // UNOBTAINED
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
        int drawY = contentY;
        int drawX;
        if (flipLeft) {
            drawX = contentX - tooltipWidth + WIDGET_WIDTH + 6;
        } else {
            drawX = contentX;
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
        graphics.blit(WIDGETS_LOCATION, contentX + 3, contentY, 0, 128 + frameIdx * 26, WIDGET_WIDTH, WIDGET_HEIGHT);

        // Title text with space before progress (progress omitted at 0% and 100%)
        if (flipLeft) {
            graphics.drawString(this.font, titleSeq, drawX + 5, contentY + TITLE_Y, -1);
            if (showProgress) {
                graphics.drawString(this.font, " " + progressText, contentX - progressTextWidth - font.width(" "), contentY + TITLE_Y, -1);
            }
        } else {
            graphics.drawString(this.font, titleSeq, contentX + titleStartX, contentY + TITLE_Y, -1);
            if (showProgress) {
                graphics.drawString(this.font, " " + progressText, contentX + tooltipWidth - progressTextWidth - font.width(" ") - 5, contentY + TITLE_Y, -1);
            }
        }

        // Description lines (matching vanilla text color: -5592406 = 0xFFAAAAAA)
        if (flipUp) {
            for (int idx = 0; idx < description.size(); idx++) {
                graphics.drawString(this.font, description.get(idx), drawX + 5, drawY + 26 - descHeight + 7 + idx * 9, -5592406, false);
            }
        } else {
            for (int idx = 0; idx < description.size(); idx++) {
                graphics.drawString(this.font, description.get(idx), drawX + 5, contentY + 9 + 17 + idx * 9, -5592406, false);
            }
        }

        // Re-render icon on top of tooltip (matching vanilla)
        quest.icon().draw(graphics, contentX + 8, contentY + 5, 16, 16);
        if (locked) {
            renderLockOverlay(graphics, new Rect(contentX, contentY, WIDGET_WIDTH, WIDGET_HEIGHT));
        }
        if (questNeedsAttention(quest)) {
            renderAlertOverlay(graphics, new Rect(contentX, contentY, WIDGET_WIDTH, WIDGET_HEIGHT));
        }
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
        Rect panel = layout.panelRect();
        Rect body = layout.bodyRect();
        Rect footer = layout.footerRect();

        ResourceLocation headerTexture = selectedHeaderTexture(quest);
        boolean panelTexturePresent = blitAdvancementPanel(graphics, ADVANCEMENT_TITLE_BOX_TEXTURE, panel);
        boolean headerTexturePresent = blitAdvancementPanel(graphics, headerTexture, header);
        Rect frameRect = new Rect(header.x() + 3, header.y(), ADVANCEMENT_FRAME_TEXTURE_SIZE, ADVANCEMENT_FRAME_TEXTURE_SIZE);
        boolean frameTexturePresent = blitAdvancementFrame(graphics, selectedFrameTexture(quest), frameRect);
        int headerTextY = header.y() + Math.max(0, (header.height() - Math.round(font.lineHeight * MODAL_TEXT_SCALE)) / 2);
        boolean obtained = quest.completed() || quest.hasUnclaimedRewards();
        boolean locked = !obtained && !isQuestAvailable(quest);

        quest.icon().draw(graphics,
                frameRect.x() + (frameRect.width() - WIDGET_ICON_SIZE) / 2,
                frameRect.y() + (frameRect.height() - WIDGET_ICON_SIZE) / 2,
                WIDGET_ICON_SIZE, WIDGET_ICON_SIZE);
        if (locked) {
            Rect lockRect = topRightRect(frameRect, STATUS_LOCK_ICON_WIDTH, STATUS_LOCK_ICON_HEIGHT, STATUS_OVERLAY_RIGHT_INSET, -3);
            renderStatusTexture(graphics, OVERHAUL_LOCK_TEXTURE, lockRect);
        }
        if (questNeedsAttention(quest)) {
            Rect notificationRect = topRightRect(frameRect, STATUS_NOTIFICATION_ICON_WIDTH, STATUS_NOTIFICATION_ICON_HEIGHT, -1, -1);
            renderStatusTexture(graphics, OVERHAUL_NOTIFICATION_TEXTURE, notificationRect);
        }
        drawScaledString(graphics,
                trim(quest.title(), (int) ((header.width() - TITLE_X - 26) / MODAL_TEXT_SCALE)),
                header.x() + TITLE_X, headerTextY, 0xFFF6EDDB, MODAL_TEXT_SCALE, true);

        int closeX = header.maxX() - 18;
        drawCenteredScaledString(graphics, Component.literal("x"), closeX + 5, headerTextY, 0xFFE9DFC7, MODAL_TEXT_SCALE, true);
        clickTargets.add(new ClickTarget(new Rect(closeX, header.y() + 4, 12, 12), this::closeViewedQuest));

        graphics.enableScissor(body.x(), body.y(), body.maxX(), body.maxY());
        int contentY = body.y() + MODAL_CONTENT_TOP_PADDING - (int) viewState.getDetailScroll();

        contentY = renderObjectiveBlock(graphics, layout, quest, contentY);
        drawSectionDivider(graphics, body.x() + MODAL_SECTION_DIVIDER_INSET, body.width() - MODAL_SECTION_DIVIDER_INSET * 2, contentY);
        contentY += 10;

        for (FormattedCharSequence line : layout.descriptionLines()) {
            drawCenteredScaledString(graphics, line, body.centerX(), contentY, 0xFFF0E2C5, MODAL_DESCRIPTION_SCALE);
            contentY += Math.max(7, Math.round(font.lineHeight * MODAL_DESCRIPTION_SCALE));
        }
        contentY += 8;
        drawSectionDivider(graphics, body.x() + MODAL_SECTION_DIVIDER_INSET, body.width() - MODAL_SECTION_DIVIDER_INSET * 2, contentY);
        contentY += 10;

        List<QuestDataSnapshot.TaskSnapshot> visibleTasks = visibleRequirementTasks(quest);
        SectionPairLayout sections = layout.sectionLayout();
        int sectionsX = body.x() + Math.max(0, (body.width() - sections.width()) / 2);
        int requirementsBottom = renderTaskSection(graphics, sectionsX, contentY, sections.requirements(), visibleTasks);
        int rewardsBottom = renderRewardSection(graphics,
                sectionsX + sections.requirements().width() + sections.gap(),
                contentY,
                sections.rewards(),
                quest.rewards());
        contentY = Math.max(requirementsBottom, rewardsBottom);
        graphics.disableScissor();

        if (layout.contentHeight() > body.height()) {
            int step = Math.max(48, body.height() - 28);
            int arrowX = rect.maxX() - 25;
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

        int buttonRowWidth = MODAL_BUTTON_WIDTH * 2 + MODAL_BUTTON_GAP;
        int buttonRowLeft = footer.x() + Math.max(0, (footer.width() - buttonRowWidth) / 2);
        Rect acceptButton = footerButtonRect(buttonRowLeft, footer.y(), MODAL_BUTTON_WIDTH, footer.height());
        Rect completeButton = footerButtonRect(buttonRowLeft + MODAL_BUTTON_WIDTH + MODAL_BUTTON_GAP, footer.y(), MODAL_BUTTON_WIDTH, footer.height());

        boolean acceptEnabled = canToggleQuestAcceptance(quest);
        renderFooterButton(graphics, acceptButton, quest.pinned() ? "Unaccept" : "Accept",
                acceptEnabled, acceptEnabled && acceptButton.contains(mouseX, mouseY), quest.pinned());
        boolean claimEnabled = canClaimAnyReward(quest);
        renderFooterButton(graphics, completeButton, "Complete", claimEnabled, completeButton.contains(mouseX, mouseY), false);
        if (quest.hasUnclaimedRewards() || claimEnabled) {
            renderNotificationMarker(graphics, completeButton, quest.completed() && !claimEnabled);
        }

        if (acceptEnabled) {
            clickTargets.add(new ClickTarget(acceptButton, () -> acceptOrUnacceptQuest(quest)));
        }
        if (claimEnabled) {
            clickTargets.add(new ClickTarget(completeButton, this::claimCurrentQuestRewards));
        }

        if (!headerTexturePresent || !panelTexturePresent || !frameTexturePresent) {
            drawCenteredScaledString(graphics, Component.literal("Missing modal texture resource"), rect.centerX(), rect.maxY() - 12, 0xFFFF8080, 0.55F);
        }
        graphics.pose().popPose();
    }

    private void renderDefaultViewButton(GuiGraphics graphics, int mouseX, int mouseY) {
        Component label = Component.literal("Editing Mode");
        int buttonWidth = Math.max(MODE_SWITCH_BUTTON_MIN_WIDTH, font.width(label) + MODE_SWITCH_BUTTON_PADDING_X * 2);
        Rect rect = new Rect(width - buttonWidth - MODE_SWITCH_BUTTON_MARGIN,
                height - MODE_SWITCH_BUTTON_HEIGHT - 4, buttonWidth, MODE_SWITCH_BUTTON_HEIGHT);
        boolean hovered = rect.contains(mouseX, mouseY);

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 260.0F);
        drawVanillaButton(graphics, rect, true, hovered);
        int textY = rect.y() + Math.max(0, (rect.height() - 8) / 2);
        graphics.drawCenteredString(font, label, rect.centerX(), textY, hovered ? 0xFFFFA0 : 0xE0E0E0);
        graphics.pose().popPose();

        clickTargets.add(new ClickTarget(rect, this::openEditingMode));
    }

    private boolean shouldShowDefaultViewButton() {
        return minecraft == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() != GameType.SURVIVAL;
    }

    private int renderTaskSection(GuiGraphics graphics, int x, int y, IconSectionLayout sectionLayout, List<QuestDataSnapshot.TaskSnapshot> tasks) {
        drawSectionHeading(graphics, sectionLayout.label(), x, y, sectionLayout.width());
        int contentY = y + sectionLayout.labelHeight() + MODAL_SECTION_LABEL_GAP;
        int contentX = x + Math.max(0, (sectionLayout.width() - sectionLayout.contentWidth()) / 2);
        if (tasks.isEmpty()) {
            drawScaledString(graphics, Component.literal(sectionLayout.emptyText()), contentX, contentY, 0xFFC8B08C, MODAL_SECTION_NOTE_SCALE);
            return y + sectionLayout.height();
        }

        for (int i = 0; i < tasks.size(); i++) {
            QuestDataSnapshot.TaskSnapshot task = tasks.get(i);
            int col = i % sectionLayout.columns();
            int row = i / sectionLayout.columns();
            int cellX = contentX + col * (sectionLayout.cellWidth() + MODAL_SECTION_ICON_GAP_X);
            int cellY = contentY + row * (sectionLayout.cellHeight() + MODAL_SECTION_ICON_GAP_Y);
            int iconX = cellX + Math.max(0, (sectionLayout.cellWidth() - MODAL_SECTION_ICON_SIZE) / 2);
            int iconY = cellY;
            Rect iconRect = new Rect(iconX, iconY, MODAL_SECTION_ICON_SIZE, MODAL_SECTION_ICON_SIZE);
            Rect hoverRect = new Rect(cellX, cellY, sectionLayout.cellWidth(), sectionLayout.cellHeight());
            renderTaskIcon(graphics, task, iconRect);
            int countY = iconRect.maxY() + MODAL_SECTION_COUNT_GAP;
            int countColor = task.completed() ? 0xFF6FE142 : 0xFFF6ECD6;
            drawCenteredScaledString(graphics, task.countLabel(), cellX + sectionLayout.cellWidth() / 2, countY, countColor, MODAL_SECTION_COUNT_SCALE);
            modalTooltipTargets.add(new TooltipTarget(hoverRect, buildTaskTooltip(task)));
        }

        return y + sectionLayout.height();
    }

    private void renderTaskIcon(GuiGraphics graphics, QuestDataSnapshot.TaskSnapshot taskSnapshot, Rect iconRect) {
        TeamData teamData = ClientQuestFile.exists() ? ClientQuestFile.INSTANCE.selfTeamData : null;
        Task liveTask = resolveTask(taskSnapshot.id());
        if (teamData != null && liveTask != null) {
            liveTask.drawGUI(teamData, graphics, iconRect.x(), iconRect.y(), iconRect.width(), iconRect.height());
            return;
        }

        taskSnapshot.icon().draw(graphics, iconRect.x(), iconRect.y(), iconRect.width(), iconRect.height());
    }

    private int renderRewardSection(GuiGraphics graphics, int x, int y, IconSectionLayout sectionLayout, List<QuestDataSnapshot.RewardSnapshot> rewards) {
        drawSectionHeading(graphics, sectionLayout.label(), x, y, sectionLayout.width());
        int contentY = y + sectionLayout.labelHeight() + MODAL_SECTION_LABEL_GAP;
        int contentX = x + Math.max(0, (sectionLayout.width() - sectionLayout.contentWidth()) / 2);
        if (rewards.isEmpty()) {
            drawScaledString(graphics, Component.literal(sectionLayout.emptyText()), contentX, contentY, 0xFFC8B08C, MODAL_SECTION_NOTE_SCALE);
            return y + sectionLayout.height();
        }

        for (int i = 0; i < rewards.size(); i++) {
            QuestDataSnapshot.RewardSnapshot reward = rewards.get(i);
            int col = i % sectionLayout.columns();
            int row = i / sectionLayout.columns();
            int cellX = contentX + col * (sectionLayout.cellWidth() + MODAL_SECTION_ICON_GAP_X);
            int cellY = contentY + row * (sectionLayout.cellHeight() + MODAL_SECTION_ICON_GAP_Y);
            int iconX = cellX + Math.max(0, (sectionLayout.cellWidth() - MODAL_SECTION_ICON_SIZE) / 2);
            int iconY = cellY;
            Rect iconRect = new Rect(iconX, iconY, MODAL_SECTION_ICON_SIZE, MODAL_SECTION_ICON_SIZE);
            Rect hoverRect = new Rect(cellX, cellY, sectionLayout.cellWidth(), sectionLayout.cellHeight());
            reward.icon().draw(graphics, iconRect.x(), iconRect.y(), iconRect.width(), iconRect.height());
            int countY = iconRect.maxY() + MODAL_SECTION_COUNT_GAP;
            int countColor = reward.claimed() ? 0xFF6FE142 : 0xFFF6ECD6;
            drawCenteredScaledString(graphics, reward.countLabel(), cellX + sectionLayout.cellWidth() / 2, countY, countColor, MODAL_SECTION_COUNT_SCALE);
            modalTooltipTargets.add(new TooltipTarget(hoverRect, buildRewardTooltip(reward)));
        }

        return y + sectionLayout.height();
    }

    private void drawSectionHeading(GuiGraphics graphics, String label, int x, int y, int width) {
        int labelHeight = Math.max(8, Math.round(font.lineHeight * MODAL_SECTION_LABEL_SCALE));
        int textY = y;
        drawCenteredScaledString(graphics, Component.literal(label + ":"), x + width / 2, textY, 0xFFF6ECD6, MODAL_SECTION_LABEL_SCALE);
    }

    private void renderScrollArrow(GuiGraphics graphics, int x, int y, boolean up, boolean active) {
        graphics.fill(x, y, x + 12, y + 12, active ? 0xCC72522E : 0x88342519);
        drawInsetBorder(graphics, new Rect(x, y, 12, 12), active ? 0xFFC18F4B : 0xAA58412A, 0x990F0B08);
        graphics.drawCenteredString(font, Component.literal(up ? "^" : "v"), x + 6, y + 2, active ? 0xFFF7E9D3 : 0xFF8F7556);
    }

    private void drawVanillaButton(GuiGraphics graphics, Rect rect, boolean enabled, boolean hovered) {
        int textureY = !enabled ? 46 : hovered ? 86 : 66;
        float scale = rect.height() / 20F;
        int logicalWidth = Math.max(2, Math.round(rect.width() / scale));
        int halfWidth = logicalWidth / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(rect.x(), rect.y(), 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.blit(BUTTONS_LOCATION, 0, 0, 0, textureY, halfWidth, 20, 256, 256);
        graphics.blit(BUTTONS_LOCATION, halfWidth, 0, 200 - (logicalWidth - halfWidth), textureY, logicalWidth - halfWidth, 20, 256, 256);
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
        if (pressed) {
            Rect checkRect = topRightRect(rect, STATUS_CHECK_ICON_WIDTH, STATUS_CHECK_ICON_HEIGHT, 3, 3);
            renderCheckTexture(graphics, checkRect.x(), checkRect.y());
        }
    }

    private void renderNotificationMarker(GuiGraphics graphics, Rect ownerRect, boolean completed) {
        Rect badge = topRightRect(ownerRect,
                completed ? STATUS_CHECK_ICON_WIDTH : STATUS_NOTIFICATION_ICON_WIDTH,
                completed ? STATUS_CHECK_ICON_HEIGHT : STATUS_NOTIFICATION_ICON_HEIGHT,
                -4, -3);
        if (completed) {
            renderCheckTexture(graphics, badge.x(), badge.y());
            return;
        }
        renderStatusTexture(graphics, OVERHAUL_NOTIFICATION_TEXTURE, badge);
    }

    private void renderCheckTexture(GuiGraphics graphics, int x, int y) {
        renderStatusTexture(graphics, OVERHAUL_CHECK_TEXTURE, new Rect(x, y, STATUS_CHECK_ICON_WIDTH, STATUS_CHECK_ICON_HEIGHT));
    }

    private void renderStatusTexture(GuiGraphics graphics, ResourceLocation texture, Rect rect) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        graphics.pose().pushPose();
        graphics.pose().translate(rect.x(), rect.y(), 200.0F);
        graphics.blit(texture, 0, 0, 0.0F, 0.0F, rect.width(), rect.height(), rect.width(), rect.height());
        graphics.pose().popPose();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private Rect widgetStatusRect(Rect nodeRect, int overlayWidth, int overlayHeight, int topInset) {
        Rect widgetFrameRect = new Rect(nodeRect.x() + WIDGET_FRAME_X_OFFSET, nodeRect.y(), WIDGET_WIDTH, WIDGET_HEIGHT);
        return topRightRect(widgetFrameRect, overlayWidth, overlayHeight, STATUS_OVERLAY_RIGHT_INSET, topInset);
    }

    private Rect topRightRect(Rect ownerRect, int overlayWidth, int overlayHeight, int rightInset, int topInset) {
        return new Rect(ownerRect.maxX() - overlayWidth - rightInset, ownerRect.y() + topInset, overlayWidth, overlayHeight);
    }

    private Rect footerButtonRect(int x, int y, int width, int containerHeight) {
        return new Rect(x, y + Math.max(0, (containerHeight - FOOTER_BUTTON_HEIGHT) / 2), width, FOOTER_BUTTON_HEIGHT);
    }

    // ---- Layout and state helpers ----

    private DetailLayout buildDetailLayout(QuestDataSnapshot.QuestSnapshot quest) {
        int maxModalWidth = Math.min(MODAL_MAX_WIDTH, width - 36);
        SectionPairLayout preferredSections = buildRequirementRewardLayout(quest, Integer.MAX_VALUE);
        int desiredWidth = Math.max(MODAL_MIN_WIDTH, preferredSections.width() + 48);
        int modalWidth = Mth.clamp(desiredWidth, MODAL_MIN_WIDTH, maxModalWidth);

        int textWidth = Math.max(8, Math.round(
                (modalWidth - 64) / MODAL_DESCRIPTION_SCALE
        ));
        List<FormattedCharSequence> objectiveLines = font.split(resolveObjectiveText(quest), textWidth);
        List<FormattedCharSequence> descriptionLines = flattenDescription(resolveDescription(quest), textWidth);
        SectionPairLayout sectionLayout = buildRequirementRewardLayout(quest, modalWidth - 48);

        int objectiveHeight = 10 + objectiveLines.size() * 8;
        int descriptionHeight = descriptionLines.size() * Math.max(7, Math.round(font.lineHeight * MODAL_DESCRIPTION_SCALE));
        int contentHeight = objectiveHeight + 10 + descriptionHeight + 10 + sectionLayout.height() + MODAL_CONTENT_BOTTOM_PADDING;

        int maxHeight = height - MODAL_MARGIN;
        int chromeHeight = 18 + MODAL_HEADER_HEIGHT + MODAL_FOOTER_HEIGHT + 24;
        int modalHeight = Math.min(contentHeight + chromeHeight, maxHeight);
        Rect rect = new Rect((width - modalWidth) / 2, (height - modalHeight) / 2, modalWidth, modalHeight);
        Rect headerRect = new Rect(rect.x() + 10, rect.y() + 8, rect.width() - 20, MODAL_HEADER_HEIGHT);
        Rect panelRect = new Rect(headerRect.x(), headerRect.maxY() - ADVANCEMENT_PANEL_TEXTURE_BORDER, headerRect.width(),
                rect.maxY() - (headerRect.maxY() - ADVANCEMENT_PANEL_TEXTURE_BORDER) - 8);
        Rect footerRect = new Rect(panelRect.x() + 10, panelRect.maxY() - MODAL_FOOTER_HEIGHT - 8, panelRect.width() - 20, MODAL_FOOTER_HEIGHT);
        Rect bodyRect = new Rect(panelRect.x() + MODAL_CONTENT_SIDE_PADDING, headerRect.maxY() + 4,
                panelRect.width() - MODAL_CONTENT_SIDE_PADDING * 2, footerRect.y() - (headerRect.maxY() + 4) - 6);

        viewState.setDetailScroll(clampScroll(viewState.getDetailScroll(), contentHeight, bodyRect.height()));
        return new DetailLayout(rect, headerRect, panelRect, bodyRect, footerRect, objectiveLines, descriptionLines, sectionLayout, contentHeight);
    }

    private SectionPairLayout buildRequirementRewardLayout(QuestDataSnapshot.QuestSnapshot quest, int availableWidth) {
        List<QuestDataSnapshot.TaskSnapshot> tasks = visibleRequirementTasks(quest);
        List<Component> taskCountLabels = tasks.stream().map(QuestDataSnapshot.TaskSnapshot::countLabel).toList();
        List<Component> rewardCountLabels = quest.rewards().stream().map(QuestDataSnapshot.RewardSnapshot::countLabel).toList();
        int requirementColumns = Math.max(1, Math.min(MODAL_SECTION_MAX_COLUMNS, tasks.size()));
        int rewardColumns = Math.max(1, Math.min(MODAL_SECTION_MAX_COLUMNS, quest.rewards().size()));
        IconSectionLayout requirements = measureSectionLayout("Requirements", taskCountLabels, requirementColumns, "None");
        IconSectionLayout rewards = measureSectionLayout("Rewards", rewardCountLabels, rewardColumns, "None");

        while (availableWidth < Integer.MAX_VALUE
                && requirements.width() + rewards.width() + MODAL_SECTION_GROUP_GAP > availableWidth
                && (requirements.columns() > 1 || rewards.columns() > 1)) {
            if (requirements.width() >= rewards.width() && requirements.columns() > 1) {
                requirementColumns--;
            } else if (rewards.columns() > 1) {
                rewardColumns--;
            } else {
                requirementColumns--;
            }

            requirements = measureSectionLayout("Requirements", taskCountLabels, requirementColumns, "None");
            rewards = measureSectionLayout("Rewards", rewardCountLabels, rewardColumns, "None");
        }

        int gap = MODAL_SECTION_GROUP_GAP;
        if (availableWidth < Integer.MAX_VALUE) {
            int remaining = availableWidth - requirements.width() - rewards.width();
            gap = Math.max(0, Math.min(MODAL_SECTION_GROUP_GAP, remaining));
        }

        return new SectionPairLayout(requirements, rewards, gap,
                requirements.width() + rewards.width() + gap,
                Math.max(requirements.height(), rewards.height()));
    }

    private IconSectionLayout measureSectionLayout(String label, List<Component> countLabels, int columns, String emptyText) {
        int labelWidth = Math.round(font.width(Component.literal(label + ":")) * MODAL_SECTION_LABEL_SCALE);
        int labelHeight = Math.max(8, Math.round(font.lineHeight * MODAL_SECTION_LABEL_SCALE));
        int count = countLabels.size();
        if (count <= 0) {
            int noteHeight = Math.max(8, Math.round(font.lineHeight * MODAL_SECTION_NOTE_SCALE));
            int noteWidth = Math.round(font.width(Component.literal(emptyText)) * MODAL_SECTION_NOTE_SCALE);
            int width = Math.max(labelWidth, noteWidth);
            return new IconSectionLayout(label, 1, 0, labelWidth, labelHeight, noteWidth,
                    noteWidth, noteHeight,
                    width,
                    labelHeight + MODAL_SECTION_LABEL_GAP + noteHeight, emptyText);
        }

        int effectiveColumns = Math.max(1, Math.min(columns, Math.min(MODAL_SECTION_MAX_COLUMNS, count)));
        int rows = Mth.ceil(count / (float) effectiveColumns);
        int countHeight = Math.max(4, Math.round(font.lineHeight * MODAL_SECTION_COUNT_SCALE));
        int countWidth = 0;
        for (Component countLabel : countLabels) {
            countWidth = Math.max(countWidth, Math.round(font.width(countLabel) * MODAL_SECTION_COUNT_SCALE));
        }
        int cellWidth = Math.max(MODAL_SECTION_ICON_SIZE, countWidth);
        int cellHeight = MODAL_SECTION_ICON_SIZE + MODAL_SECTION_COUNT_GAP + countHeight;
        int gridWidth = effectiveColumns * cellWidth + Math.max(0, effectiveColumns - 1) * MODAL_SECTION_ICON_GAP_X;
        int gridHeight = rows * cellHeight + Math.max(0, rows - 1) * MODAL_SECTION_ICON_GAP_Y;
        return new IconSectionLayout(label, effectiveColumns, rows, labelWidth, labelHeight, gridWidth,
                cellWidth, cellHeight,
                Math.max(labelWidth, gridWidth),
                labelHeight + MODAL_SECTION_LABEL_GAP + gridHeight, emptyText);
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

    private boolean questNeedsAttention(QuestDataSnapshot.QuestSnapshot quest) {
        return quest.hasUnclaimedRewards() || canClaimAnyReward(quest);
    }

    private List<QuestDataSnapshot.TaskSnapshot> visibleRequirementTasks(QuestDataSnapshot.QuestSnapshot quest) {
        if (quest.checkmarkOnly()) {
            return List.of();
        }
        return quest.tasks();
    }

    private boolean canToggleQuestAcceptance(QuestDataSnapshot.QuestSnapshot questSnapshot) {
        if (questSnapshot.completed() || questSnapshot.hasUnclaimedRewards()) {
            return false;
        }

        return questSnapshot.pinned() || questSnapshot.canStart();
    }

    private void snapToInitialQuest(QuestDataSnapshot snapshot) {
        QuestDataSnapshot.QuestSnapshot rememberedQuest = snapshot.findQuest(QuestDataController.getLastAcceptedQuestId());
        if (rememberedQuest != null && rememberedQuest.pinned()) {
            viewState.setSelectedChapterId(rememberedQuest.chapterId());
            QuestDataSnapshot.ChapterSnapshot chapter = snapshot.findChapter(rememberedQuest.chapterId());
            if (chapter != null) {
                centerTreeOnQuest(chapter, rememberedQuest);
            }
            return;
        }

        viewState.setViewedQuestId(0L);
    }

    private void centerTreeOnQuest(QuestDataSnapshot.ChapterSnapshot chapter, QuestDataSnapshot.QuestSnapshot quest) {
        computeNodeBounds(chapter.quests());
        Rect nodeRect = getNodeWorldRect(quest);
        double zoom = viewState.getTreeZoom();
        viewState.setTreePanX(TREE_WIDTH / 2.0D - nodeRect.centerX() * zoom);
        viewState.setTreePanY(TREE_HEIGHT / 2.0D - nodeRect.centerY() * zoom);
        clampTreePanToQuestBounds();
        centered = true;
    }

    private void acceptOrUnacceptQuest(QuestDataSnapshot.QuestSnapshot questSnapshot) {
        if (!canToggleQuestAcceptance(questSnapshot)) {
            return;
        }

        Quest quest = resolveQuest(questSnapshot.id());
        if (quest == null) {
            return;
        }

        if (questSnapshot.pinned()) {
            actionRouter.togglePin(quest);
            if (QuestDataController.getLastAcceptedQuestId() == questSnapshot.id()) {
                viewState.setLastAcceptedQuestId(0L);
                QuestDataController.setLastAcceptedQuestId(0L);
            }
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
        viewState.setLastAcceptedQuestId(questSnapshot.id());
        QuestDataController.setLastAcceptedQuestId(questSnapshot.id());
        QuestDataController.markDirty();
    }

    private void claimCurrentQuestRewards() {
        QuestDataSnapshot snapshot = QuestDataController.getSnapshot();
        QuestDataSnapshot.QuestSnapshot questSnapshot = snapshot.findQuest(viewState.getViewedQuestId());
        if (questSnapshot == null) {
            return;
        }

        boolean openedChoiceScreen = false;
        boolean startedClaimFlow = false;
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
                startedClaimFlow = true;
            } else if (rewardSnapshot.interactionMode() == RewardInteractionMode.CHOICE && !openedChoiceScreen && liveReward instanceof ChoiceReward choiceReward) {
                minecraft.setScreen(new ChoiceRewardSelectScreen(this, choiceReward, actionRouter));
                openedChoiceScreen = true;
                startedClaimFlow = true;
            } else if (rewardSnapshot.interactionMode() == RewardInteractionMode.VANILLA_FALLBACK) {
                Quest liveQuest = resolveQuest(questSnapshot.id());
                if (liveQuest != null) {
                    actionRouter.openVanillaForQuest(liveQuest);
                    return;
                }
            }
        }

        if (startedClaimFlow) {
            unpinQuestIfPinned(questSnapshot);
        }
    }

    private void submitTask(QuestDataSnapshot.TaskSnapshot taskSnapshot) {
        Task liveTask = resolveTask(taskSnapshot.id());
        if (liveTask != null) {
            actionRouter.submitTask(liveTask);
        }
    }

    private void handleRewardClick(QuestDataSnapshot.RewardSnapshot rewardSnapshot) {
        if (!rewardSnapshot.canClaim()) {
            return;
        }

        Reward liveReward = resolveReward(rewardSnapshot.id());
        if (liveReward == null) {
            return;
        }

        if (rewardSnapshot.interactionMode() == RewardInteractionMode.CLAIM) {
            actionRouter.claimReward(liveReward, true);
            QuestDataSnapshot.QuestSnapshot questSnapshot = QuestDataController.getSnapshot().findQuest(viewState.getViewedQuestId());
            if (questSnapshot != null) {
                unpinQuestIfPinned(questSnapshot);
            }
        } else if (rewardSnapshot.interactionMode() == RewardInteractionMode.CHOICE && liveReward instanceof ChoiceReward choiceReward) {
            minecraft.setScreen(new ChoiceRewardSelectScreen(this, choiceReward, actionRouter));
            QuestDataSnapshot.QuestSnapshot questSnapshot = QuestDataController.getSnapshot().findQuest(viewState.getViewedQuestId());
            if (questSnapshot != null) {
                unpinQuestIfPinned(questSnapshot);
            }
        } else if (rewardSnapshot.interactionMode() == RewardInteractionMode.VANILLA_FALLBACK) {
            openVanillaSelected();
        }
    }

    private void unpinQuestIfPinned(QuestDataSnapshot.QuestSnapshot questSnapshot) {
        if (!questSnapshot.pinned()) {
            return;
        }

        Quest liveQuest = resolveQuest(questSnapshot.id());
        if (liveQuest != null) {
            actionRouter.togglePin(liveQuest);
            if (QuestDataController.getLastAcceptedQuestId() == questSnapshot.id()) {
                viewState.setLastAcceptedQuestId(0L);
                QuestDataController.setLastAcceptedQuestId(0L);
            }
            QuestDataController.markDirty();
        }
    }

    private List<Component> buildTaskTooltip(QuestDataSnapshot.TaskSnapshot task) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(task.title());
        tooltip.add(task.progressText());

        String state = switch (task.interactionMode()) {
            case SUBMIT -> task.completed() ? "Completed" : task.canInteract() ? "Ready" : "Locked";
            case VANILLA_FALLBACK -> task.canInteract() ? "Use default FTB Quests UI" : "Locked";
            case READ_ONLY -> task.completed() ? "Completed" : "Tracked automatically";
        };
        tooltip.add(Component.literal(state).withStyle(task.completed() ? Style.EMPTY.withColor(0x75D65C) : Style.EMPTY.withColor(0xD9BE96)));
        if (!task.fallbackReason().isBlank()) {
            tooltip.add(Component.literal(task.fallbackReason()).withStyle(Style.EMPTY.withColor(0xC8B08C)));
        }
        return tooltip;
    }

    private List<Component> buildRewardTooltip(QuestDataSnapshot.RewardSnapshot reward) {
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(reward.title());
        String rewardStatus = reward.statusText().getString().trim().toLowerCase(Locale.ROOT);
        if (!rewardStatus.equals("cant claim") && !rewardStatus.equals("claimed") && !rewardStatus.equals("can claim")) {
            tooltip.add(reward.statusText());
        }

        String state = switch (reward.interactionMode()) {
            case CLAIM -> reward.canClaim() ? "Claimable via Complete" : reward.claimed() ? "Claimed" : "Locked";
            case CHOICE -> reward.canClaim() ? "Choose via Complete" : reward.claimed() ? "Claimed" : "Locked";
            case VANILLA_FALLBACK -> reward.canClaim() ? "Use Complete or default FTB Quests UI" : "Locked";
        };
        int color = reward.canClaim() ? 0x7DE25E : reward.claimed() ? 0xB6B6B6 : 0xD5B58C;
        tooltip.add(Component.literal(state).withStyle(Style.EMPTY.withColor(color)));
        if (!reward.fallbackReason().isBlank()) {
            tooltip.add(Component.literal(reward.fallbackReason()).withStyle(Style.EMPTY.withColor(0xC8B08C)));
        }
        return tooltip;
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

        if (openingQuestSnapPending) {
            openingQuestSnapPending = false;
            snapToInitialQuest(snapshot);
            chapter = snapshot.findChapter(viewState.getSelectedChapterId());
            if (chapter == null) {
                chapter = snapshot.chapters().get(0);
                viewState.setSelectedChapterId(chapter.id());
            }
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
            QuestDataSnapshot.ChapterSnapshot chapter = chapters.get(i);
            if (chapter.firstInGroup()) {
                entryTop += CHAPTER_GROUP_HEADER_HEIGHT;
            }
            if (chapter.id() == selectedChapterId) {
                selectedIndex = i;
                break;
            }
            if (isChapterGroupCollapsed(chapter.groupId())) {
                continue;
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
        return new Rect(frame.x() + CHAPTER_SELECTOR_X, frame.y() + CHAPTER_SELECTOR_Y + chapterSelectorHeaderReservedHeight(),
                CHAPTER_BUTTON_ACTIVE_WIDTH, chapterSelectorViewportHeight());
    }

    private int chapterSelectorViewportHeight() {
        return BACKGROUND_HEIGHT - CHAPTER_SELECTOR_Y - chapterSelectorHeaderReservedHeight() - CHAPTER_SELECTOR_BOTTOM_PADDING;
    }

    private Rect chapterSelectorHeaderRect() {
        Rect frame = frameRect();
        return new Rect(frame.x() + CHAPTER_SELECTOR_X, frame.y() + CHAPTER_SELECTOR_Y,
                CHAPTER_BUTTON_ACTIVE_WIDTH, CHAPTER_SELECTOR_TITLE_HEIGHT);
    }

    private int chapterSelectorHeaderReservedHeight() {
        return CHAPTER_SELECTOR_TITLE_HEIGHT + CHAPTER_SELECTOR_TITLE_BOTTOM_GAP;
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
            if (isChapterGroupCollapsed(chapter.groupId())) {
                continue;
            }
            height += CHAPTER_SELECTOR_SCROLL_STEP;
        }
        return Math.max(0, height - CHAPTER_SELECTOR_ENTRY_SPACING);
    }

    private double maxChapterScroll(QuestDataSnapshot snapshot) {
        return Math.max(0D, chapterContentHeight(snapshot) - chapterSelectorViewportHeight());
    }

    private boolean isChapterGroupCollapsed(long groupId) {
        return !viewState.isChapterGroupExpanded(groupId);
    }

    private void toggleChapterGroup(long groupId) {
        viewState.setChapterGroupExpanded(groupId, isChapterGroupCollapsed(groupId));
        QuestDataController.saveViewState(viewState);
        clampChapterScroll(QuestDataController.getSnapshot());
    }

    private void addClippedClickTarget(Rect rect, Rect viewportRect, Runnable action) {
        if (!rect.intersects(viewportRect)) {
            return;
        }

        int clippedY = Math.max(rect.y(), viewportRect.y());
        int clippedHeight = Math.min(rect.maxY(), viewportRect.maxY()) - clippedY;
        if (clippedHeight > 0) {
            clickTargets.add(new ClickTarget(new Rect(rect.x(), clippedY, rect.width(), clippedHeight), action));
        }
    }

    private void renderModalTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int i = modalTooltipTargets.size() - 1; i >= 0; i--) {
            TooltipTarget target = modalTooltipTargets.get(i);
            if (target.rect().contains(mouseX, mouseY)) {
                graphics.renderComponentTooltip(font, target.lines(), mouseX, mouseY);
                return;
            }
        }
    }

    private Rect modalBounds(DetailLayout layout) {
        Rect rect = layout.rect();
        Rect header = layout.headerRect();
        Rect panel = layout.panelRect();
        int minX = Math.min(rect.x(), Math.min(header.x(), panel.x()));
        int minY = Math.min(rect.y(), Math.min(header.y(), panel.y()));
        int maxX = Math.max(rect.maxX(), Math.max(header.maxX(), panel.maxX()));
        int maxY = Math.max(rect.maxY(), Math.max(header.maxY(), panel.maxY()));
        return new Rect(minX, minY, maxX - minX, maxY - minY);
    }

    private Rect frameRect() {
        return new Rect((width - BACKGROUND_WIDTH) / 2, (height - BACKGROUND_HEIGHT) / 2, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    private Rect localTreeViewportRect() {
        return new Rect(0, 0, TREE_WIDTH, TREE_HEIGHT);
    }

    private Rect treeViewportRect() {
        Rect frame = frameRect();
        return new Rect(frame.x() + TREE_X, frame.y() + TREE_Y, TREE_WIDTH, TREE_HEIGHT);
    }

    private Rect clipRect(Rect rect, Rect clipBounds) {
        int clippedX = Math.max(rect.x(), clipBounds.x());
        int clippedY = Math.max(rect.y(), clipBounds.y());
        int clippedWidth = Math.min(rect.maxX(), clipBounds.maxX()) - clippedX;
        int clippedHeight = Math.min(rect.maxY(), clipBounds.maxY()) - clippedY;
        if (clippedWidth <= 0 || clippedHeight <= 0) {
            return null;
        }
        return new Rect(clippedX, clippedY, clippedWidth, clippedHeight);
    }

    private int projectTreeX(double worldX) {
        return Mth.floor(viewState.getTreePanX() + worldX * viewState.getTreeZoom());
    }

    private int projectTreeY(double worldY) {
        return Mth.floor(viewState.getTreePanY() + worldY * viewState.getTreeZoom());
    }

    private int projectTreeMaxX(double worldX) {
        return Mth.ceil(viewState.getTreePanX() + worldX * viewState.getTreeZoom());
    }

    private int projectTreeMaxY(double worldY) {
        return Mth.ceil(viewState.getTreePanY() + worldY * viewState.getTreeZoom());
    }

    private boolean isCreativeControlsVisible() {
        return minecraft == null || minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() != GameType.SURVIVAL;
    }

    // ---- Drawing primitives ----

    private void blitScaledRegion(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                  int u, int v, int regionWidth, int regionHeight) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(width / (float) regionWidth, height / (float) regionHeight, 1.0F);
        graphics.blit(texture, 0, 0, u, v, regionWidth, regionHeight);
        graphics.pose().popPose();
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

    private void drawSectionDivider(GuiGraphics graphics, int x, int width, int y) {
        graphics.fill(x, y, x + width, y + 1, 0x88664B30);
        graphics.fill(x, y + 1, x + width, y + 2, 0x44180F0B);
    }

    private void drawScaledString(GuiGraphics graphics, Component component, int x, int y, int color, float scale) {
        drawScaledString(graphics, component, x, y, color, scale, false);
    }

    private void drawScaledString(GuiGraphics graphics, Component component, float x, float y, int color, float scale) {
        drawScaledString(graphics, component, x, y, color, scale, false);
    }

    private void drawScaledString(GuiGraphics graphics, Component component, int x, int y, int color, float scale, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.drawString(font, component, 0, 0, color, shadow);
        graphics.pose().popPose();
    }

    private void drawScaledString(GuiGraphics graphics, Component component, float x, float y, int color, float scale, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.drawString(font, component, 0, 0, color, shadow);
        graphics.pose().popPose();
    }

    private void drawScaledString(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int color, float scale) {
        drawScaledString(graphics, text, x, y, color, scale, false);
    }

    private void drawScaledString(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int color, float scale, boolean shadow) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.drawString(font, text, 0, 0, color, shadow);
        graphics.pose().popPose();
    }

    private void drawCenteredScaledString(GuiGraphics graphics, Component component, int centerX, int y, int color, float scale) {
        drawCenteredScaledString(graphics, component, centerX, y, color, scale, false);
    }

    private void drawCenteredScaledString(GuiGraphics graphics, Component component, int centerX, int y, int color, float scale, boolean shadow) {
        int scaledWidth = Math.round(font.width(component) * scale);
        drawScaledString(graphics, component, centerX - scaledWidth / 2, y, color, scale, shadow);
    }

    private void drawCenteredScaledString(GuiGraphics graphics, FormattedCharSequence text, int centerX, int y, int color, float scale) {
        drawCenteredScaledString(graphics, text, centerX, y, color, scale, false);
    }

    private void drawCenteredScaledString(GuiGraphics graphics, FormattedCharSequence text, int centerX, int y, int color, float scale, boolean shadow) {
        int scaledWidth = Math.round(font.width(text) * scale);
        drawScaledString(graphics, text, centerX - scaledWidth / 2, y, color, scale, shadow);
    }

    private int renderObjectiveBlock(GuiGraphics graphics, DetailLayout layout, QuestDataSnapshot.QuestSnapshot quest, int y) {
        FormattedCharSequence firstLine = layout.objectiveLines().isEmpty()
                ? Language.getInstance().getVisualOrder(Component.empty())
                : layout.objectiveLines().get(0);
        int firstLineWidth = Math.round(font.width(firstLine) * MODAL_TEXT_SCALE);
        int lineX = layout.bodyRect().centerX() - firstLineWidth / 2;

        drawScaledString(graphics, firstLine, lineX, y, 0xFF6FE142, MODAL_TEXT_SCALE);
        y += 10;

        for (int i = 1; i < layout.objectiveLines().size(); i++) {
            drawCenteredScaledString(graphics, layout.objectiveLines().get(i), layout.bodyRect().centerX(), y, 0xFF6FE142, MODAL_TEXT_SCALE);
            y += 8;
        }

        return y;
    }

    private ResourceLocation selectedHeaderTexture(QuestDataSnapshot.QuestSnapshot quest) {
        return quest.completed() || quest.hasUnclaimedRewards() ? ADVANCEMENT_BOX_OBTAINED_TEXTURE : ADVANCEMENT_BOX_UNOBTAINED_TEXTURE;
    }

    private ResourceLocation selectedFrameTexture(QuestDataSnapshot.QuestSnapshot quest) {
        return quest.completed() || quest.hasUnclaimedRewards() ? ADVANCEMENT_TASK_FRAME_OBTAINED_TEXTURE : ADVANCEMENT_TASK_FRAME_UNOBTAINED_TEXTURE;
    }

    private boolean blitAdvancementPanel(GuiGraphics graphics, ResourceLocation texture, Rect rect) {
        if (!hasGuiResource(texture)) {
            renderMissingPanelFallback(graphics, rect, texture);
            return false;
        }
        RenderSystem.enableBlend();
        drawNineSlicePanel(graphics, texture, rect.x(), rect.y(), rect.width(), rect.height(),
                ADVANCEMENT_PANEL_TEXTURE_WIDTH, ADVANCEMENT_PANEL_TEXTURE_HEIGHT, ADVANCEMENT_PANEL_TEXTURE_BORDER);
        RenderSystem.disableBlend();
        return true;
    }

    private boolean blitAdvancementFrame(GuiGraphics graphics, ResourceLocation texture, Rect rect) {
        if (!hasGuiResource(texture)) {
            renderMissingPanelFallback(graphics, rect, texture);
            return false;
        }

        RenderSystem.enableBlend();
        graphics.blit(texture, rect.x(), rect.y(), 0, 0.0F, 0.0F, rect.width(), rect.height(),
                ADVANCEMENT_FRAME_TEXTURE_SIZE, ADVANCEMENT_FRAME_TEXTURE_SIZE);
        RenderSystem.disableBlend();
        return true;
    }

    private void drawNineSlicePanel(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                    int textureWidth, int textureHeight, int border) {
        int left = Math.min(border, width / 2);
        int right = Math.min(border, width / 2);
        int top = Math.min(border, height / 2);
        int bottom = Math.min(border, height / 2);

        int centerWidth = Math.max(0, width - left - right);
        int centerHeight = Math.max(0, height - top - bottom);
        int sourceCenterWidth = Math.max(0, textureWidth - border * 2);
        int sourceCenterHeight = Math.max(0, textureHeight - border * 2);

        blitPanelSlice(graphics, texture, x, y, left, top, 0, 0, left, top, textureWidth, textureHeight);
        blitPanelSlice(graphics, texture, x + width - right, y, right, top, textureWidth - right, 0, right, top, textureWidth, textureHeight);
        blitPanelSlice(graphics, texture, x, y + height - bottom, left, bottom, 0, textureHeight - bottom, left, bottom, textureWidth, textureHeight);
        blitPanelSlice(graphics, texture, x + width - right, y + height - bottom, right, bottom,
                textureWidth - right, textureHeight - bottom, right, bottom, textureWidth, textureHeight);

        if (centerWidth > 0) {
            blitPanelSlice(graphics, texture, x + left, y, centerWidth, top, border, 0, sourceCenterWidth, top, textureWidth, textureHeight);
            blitPanelSlice(graphics, texture, x + left, y + height - bottom, centerWidth, bottom,
                    border, textureHeight - bottom, sourceCenterWidth, bottom, textureWidth, textureHeight);
        }
        if (centerHeight > 0) {
            blitPanelSlice(graphics, texture, x, y + top, left, centerHeight, 0, border, left, sourceCenterHeight, textureWidth, textureHeight);
            blitPanelSlice(graphics, texture, x + width - right, y + top, right, centerHeight,
                    textureWidth - right, border, right, sourceCenterHeight, textureWidth, textureHeight);
        }
        if (centerWidth > 0 && centerHeight > 0) {
            blitPanelSlice(graphics, texture, x + left, y + top, centerWidth, centerHeight,
                    border, border, sourceCenterWidth, sourceCenterHeight, textureWidth, textureHeight);
        }
    }

    private void blitPanelSlice(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height,
                                int u, int v, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        if (width <= 0 || height <= 0 || uWidth <= 0 || vHeight <= 0) {
            return;
        }

        graphics.blit(texture, x, y, width, height, (float) u, (float) v, uWidth, vHeight, textureWidth, textureHeight);
    }

    private boolean hasGuiResource(ResourceLocation texture) {
        Minecraft instance = Minecraft.getInstance();
        Optional<Resource> resource = instance.getResourceManager().getResource(texture);
        if (resource.isPresent()) {
            return true;
        }

        if (missingModalTextures.add(texture)) {
            FTBQuestsVisualOverhaul.LOGGER.warn("Missing quest modal texture resource: {}", texture);
        }
        return false;
    }

    private void renderMissingPanelFallback(GuiGraphics graphics, Rect rect, ResourceLocation texture) {
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), 0xD4211711);
        drawInsetBorder(graphics, rect, 0xFF8F6938, 0xCC160F0B);
        drawCenteredScaledString(graphics, Component.literal("Missing: " + texture), rect.centerX(), rect.centerY() - 3, 0xFFFF8080, 0.45F);
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

    private Rect expand(Rect rect, int amount) {
        return new Rect(rect.x() - amount, rect.y() - amount, rect.width() + amount * 2, rect.height() + amount * 2);
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

    private static final class MutableNodePosition {
        private final long id;
        private int x;
        private int y;

        private MutableNodePosition(long id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        private long id() {
            return id;
        }

        private int x() {
            return x;
        }

        private int y() {
            return y;
        }

        private void moveX(int delta) {
            x += delta;
        }

        private void moveY(int delta) {
            y += delta;
        }

        private Rect toRect() {
            return new Rect(x, y, WIDGET_WIDTH, WIDGET_HEIGHT);
        }
    }

    // ---- Records ----

    private record ChapterRowLayout(QuestDataSnapshot.ChapterSnapshot chapter, Rect buttonRect, Rect textureRect, boolean hovered) {
    }

    private record ClickTarget(Rect rect, Runnable action) {
        boolean contains(double mouseX, double mouseY) {
            return rect.contains(mouseX, mouseY);
        }
    }

    private record TooltipTarget(Rect rect, List<Component> lines) {
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
            Rect panelRect,
            Rect bodyRect,
            Rect footerRect,
            List<FormattedCharSequence> objectiveLines,
            List<FormattedCharSequence> descriptionLines,
            SectionPairLayout sectionLayout,
            int contentHeight
    ) {
    }

    private record SectionPairLayout(
            IconSectionLayout requirements,
            IconSectionLayout rewards,
            int gap,
            int width,
            int height
    ) {
    }

    private record IconSectionLayout(
            String label,
            int columns,
            int rows,
            int labelWidth,
            int labelHeight,
            int contentWidth,
            int cellWidth,
            int cellHeight,
            int width,
            int height,
            String emptyText
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
