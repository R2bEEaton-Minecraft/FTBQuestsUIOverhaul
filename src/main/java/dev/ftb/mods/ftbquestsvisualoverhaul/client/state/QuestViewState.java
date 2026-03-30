package dev.ftb.mods.ftbquestsvisualoverhaul.client.state;

import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModClientConfig;

public class QuestViewState {
    private long selectedChapterId;
    private long viewedQuestId;
    private String searchText;
    private double chapterScroll;
    private double detailScroll;
    private double treePanX;
    private double treePanY;
    private double treeZoom;
    private LayoutMode layoutMode;
    private QuestSortMode sortMode;

    public QuestViewState() {
        selectedChapterId = 0L;
        viewedQuestId = 0L;
        searchText = "";
        chapterScroll = 0D;
        detailScroll = 0D;
        treePanX = 0D;
        treePanY = 0D;
        treeZoom = 1D;
        layoutMode = ModClientConfig.DEFAULT_LAYOUT.get();
        sortMode = QuestSortMode.PROGRESSION;
    }

    public QuestViewState copy() {
        QuestViewState copy = new QuestViewState();
        copy.selectedChapterId = selectedChapterId;
        copy.viewedQuestId = viewedQuestId;
        copy.searchText = searchText;
        copy.chapterScroll = chapterScroll;
        copy.detailScroll = detailScroll;
        copy.treePanX = treePanX;
        copy.treePanY = treePanY;
        copy.treeZoom = treeZoom;
        copy.layoutMode = layoutMode;
        copy.sortMode = sortMode;
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
