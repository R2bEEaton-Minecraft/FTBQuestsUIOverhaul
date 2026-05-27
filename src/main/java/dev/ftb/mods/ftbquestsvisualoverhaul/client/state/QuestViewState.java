package dev.ftb.mods.ftbquestsvisualoverhaul.client.state;

import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModClientConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuestViewState {
    private long selectedChapterId;
    private long viewedQuestId;
    private long lastAcceptedQuestId;
    private long lastReadyToCompleteQuestId;
    private long lastCompletedQuestId;
    private boolean hideAcceptedQuests;
    private Set<Long> hiddenAcceptedQuestIds;
    private String searchText;
    private double chapterScroll;
    private double detailScroll;
    private double treePanX;
    private double treePanY;
    private double treeZoom;
    private boolean defaultFtbUiMode;
    private LayoutMode layoutMode;
    private QuestSortMode sortMode;
    private Map<Long, ResourceLocation> chapterTitleTextures;
    private Map<Long, Boolean> chapterFreePanStates;
    private Set<Long> expandedChapterGroups;

    public QuestViewState() {
        selectedChapterId = 0L;
        viewedQuestId = 0L;
        lastAcceptedQuestId = 0L;
        lastReadyToCompleteQuestId = 0L;
        lastCompletedQuestId = 0L;
        hideAcceptedQuests = false;
        hiddenAcceptedQuestIds = new HashSet<>();
        searchText = "";
        chapterScroll = 0D;
        detailScroll = 0D;
        treePanX = 0D;
        treePanY = 0D;
        treeZoom = 1D;
        defaultFtbUiMode = false;
        layoutMode = ModClientConfig.DEFAULT_LAYOUT.get();
        sortMode = QuestSortMode.PROGRESSION;
        chapterTitleTextures = new HashMap<>();
        chapterFreePanStates = new HashMap<>();
        expandedChapterGroups = new HashSet<>();
    }

    public QuestViewState copy() {
        QuestViewState copy = new QuestViewState();
        copy.selectedChapterId = selectedChapterId;
        copy.viewedQuestId = viewedQuestId;
        copy.lastAcceptedQuestId = lastAcceptedQuestId;
        copy.lastReadyToCompleteQuestId = lastReadyToCompleteQuestId;
        copy.lastCompletedQuestId = lastCompletedQuestId;
        copy.hideAcceptedQuests = hideAcceptedQuests;
        copy.hiddenAcceptedQuestIds = new HashSet<>(hiddenAcceptedQuestIds);
        copy.searchText = searchText;
        copy.chapterScroll = chapterScroll;
        copy.detailScroll = detailScroll;
        copy.treePanX = treePanX;
        copy.treePanY = treePanY;
        copy.treeZoom = treeZoom;
        copy.defaultFtbUiMode = defaultFtbUiMode;
        copy.layoutMode = layoutMode;
        copy.sortMode = sortMode;
        copy.chapterTitleTextures = new HashMap<>(chapterTitleTextures);
        copy.chapterFreePanStates = new HashMap<>(chapterFreePanStates);
        copy.expandedChapterGroups = new HashSet<>(expandedChapterGroups);
        return copy;
    }

    public long getSelectedChapterId() {
        return selectedChapterId;
    }

    public void setSelectedChapterId(long selectedChapterId) {
        this.selectedChapterId = selectedChapterId;
    }

    public long getViewedQuestId() {
        return viewedQuestId;
    }

    public void setViewedQuestId(long viewedQuestId) {
        this.viewedQuestId = viewedQuestId;
    }

    public long getLastAcceptedQuestId() {
        return lastAcceptedQuestId;
    }

    public void setLastAcceptedQuestId(long lastAcceptedQuestId) {
        this.lastAcceptedQuestId = lastAcceptedQuestId;
    }

    public long getLastReadyToCompleteQuestId() {
        return lastReadyToCompleteQuestId;
    }

    public void setLastReadyToCompleteQuestId(long lastReadyToCompleteQuestId) {
        this.lastReadyToCompleteQuestId = lastReadyToCompleteQuestId;
    }

    public long getLastCompletedQuestId() {
        return lastCompletedQuestId;
    }

    public void setLastCompletedQuestId(long lastCompletedQuestId) {
        this.lastCompletedQuestId = lastCompletedQuestId;
    }

    public boolean isHideAcceptedQuests() {
        return hideAcceptedQuests;
    }

    public void setHideAcceptedQuests(boolean hideAcceptedQuests) {
        this.hideAcceptedQuests = hideAcceptedQuests;
    }

    public Set<Long> getHiddenAcceptedQuestIds() {
        return new HashSet<>(hiddenAcceptedQuestIds);
    }

    public void setHiddenAcceptedQuestIds(Set<Long> hiddenAcceptedQuestIds) {
        this.hiddenAcceptedQuestIds = new HashSet<>(hiddenAcceptedQuestIds);
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText == null ? "" : searchText;
    }

    public double getChapterScroll() {
        return chapterScroll;
    }

    public void setChapterScroll(double chapterScroll) {
        this.chapterScroll = chapterScroll;
    }

    public double getDetailScroll() {
        return detailScroll;
    }

    public void setDetailScroll(double detailScroll) {
        this.detailScroll = detailScroll;
    }

    public double getTreePanX() {
        return treePanX;
    }

    public void setTreePanX(double treePanX) {
        this.treePanX = treePanX;
    }

    public double getTreePanY() {
        return treePanY;
    }

    public void setTreePanY(double treePanY) {
        this.treePanY = treePanY;
    }

    public double getTreeZoom() {
        return treeZoom;
    }

    public void setTreeZoom(double treeZoom) {
        this.treeZoom = treeZoom;
    }

    public boolean isFreePan(long chapterId) {
        return chapterFreePanStates.getOrDefault(chapterId, true);
    }

    public void setFreePan(long chapterId, boolean freePan) {
        if (freePan) {
            chapterFreePanStates.remove(chapterId);
        } else {
            chapterFreePanStates.put(chapterId, false);
        }
    }

    public boolean isDefaultFtbUiMode() {
        return defaultFtbUiMode;
    }

    public void setDefaultFtbUiMode(boolean defaultFtbUiMode) {
        this.defaultFtbUiMode = defaultFtbUiMode;
    }

    public ResourceLocation getChapterTitleTexture(long chapterId) {
        return chapterTitleTextures.get(chapterId);
    }

    public void setChapterTitleTexture(long chapterId, ResourceLocation texture) {
        if (texture == null) {
            chapterTitleTextures.remove(chapterId);
        } else {
            chapterTitleTextures.put(chapterId, texture);
        }
    }

    public Map<Long, ResourceLocation> getChapterTitleTextures() {
        return new HashMap<>(chapterTitleTextures);
    }

    public void setChapterTitleTextures(Map<Long, ResourceLocation> chapterTitleTextures) {
        this.chapterTitleTextures = new HashMap<>(chapterTitleTextures);
    }

    public Map<Long, Boolean> getChapterFreePanStates() {
        return new HashMap<>(chapterFreePanStates);
    }

    public void setChapterFreePanStates(Map<Long, Boolean> chapterFreePanStates) {
        this.chapterFreePanStates = new HashMap<>(chapterFreePanStates);
    }

    public boolean isChapterGroupExpanded(long groupId) {
        return expandedChapterGroups.contains(groupId);
    }

    public void setChapterGroupExpanded(long groupId, boolean expanded) {
        if (expanded) {
            expandedChapterGroups.add(groupId);
        } else {
            expandedChapterGroups.remove(groupId);
        }
    }

    public Set<Long> getExpandedChapterGroups() {
        return new HashSet<>(expandedChapterGroups);
    }

    public void setExpandedChapterGroups(Set<Long> expandedChapterGroups) {
        this.expandedChapterGroups = new HashSet<>(expandedChapterGroups);
    }

    public LayoutMode getLayoutMode() {
        return layoutMode;
    }

    public void setLayoutMode(LayoutMode layoutMode) {
        this.layoutMode = layoutMode;
    }

    public QuestSortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(QuestSortMode sortMode) {
        this.sortMode = sortMode;
    }
}
