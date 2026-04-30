package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.QuestDataSnapshot;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.data.QuestDataSnapshotBuilder;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestViewState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class QuestDataController {
    private static final QuestDataSnapshotBuilder SNAPSHOT_BUILDER = new QuestDataSnapshotBuilder();
    private static final Path TILE_TEXTURES_FILE = FMLPaths.CONFIGDIR.get()
            .resolve("ftbquests")
            .resolve("quests")
            .resolve("ftbquestsvisualoverhaul_tiles.properties");
    private static final Path FREE_PAN_FILE = FMLPaths.CONFIGDIR.get()
            .resolve("ftbquests")
            .resolve("quests")
            .resolve("ftbquestsvisualoverhaul_free_pan.properties");
    private static final Path CHAPTER_GROUPS_FILE = FMLPaths.CONFIGDIR.get()
            .resolve("ftbquests")
            .resolve("quests")
            .resolve("ftbquestsvisualoverhaul_chapter_groups.properties");
    private static final Path LEGACY_TILE_TEXTURES_FILE = FMLPaths.CONFIGDIR.get().resolve("ftbquestsvisualoverhaul_tiles.properties");

    private static QuestViewState persistedViewState = new QuestViewState();
    private static QuestDataSnapshot snapshot = new QuestDataSnapshot(java.util.List.of(), false);
    private static boolean dirty = true;

    static {
        loadPersistentTileTextures();
        loadPersistentFreePanStates();
        loadPersistentChapterGroups();
    }

    private QuestDataController() {
    }

    public static void markDirty() {
        dirty = true;
    }

    public static QuestDataSnapshot getSnapshot() {
        if (dirty && ClientQuestFile.exists()) {
            snapshot = SNAPSHOT_BUILDER.build(ClientQuestFile.INSTANCE.selfTeamData);
            dirty = false;
        }
        return snapshot;
    }

    public static QuestViewState copyPersistedViewState() {
        return persistedViewState.copy();
    }

    public static void saveViewState(QuestViewState state) {
        QuestViewState copy = state.copy();
        copy.setLastAcceptedQuestId(persistedViewState.getLastAcceptedQuestId());
        copy.setHideAcceptedQuests(persistedViewState.isHideAcceptedQuests());
        copy.setHiddenAcceptedQuestIds(persistedViewState.getHiddenAcceptedQuestIds());
        persistedViewState = copy;
        savePersistentTileTextures();
        savePersistentFreePanStates();
        savePersistentChapterGroups();
    }

    public static long getLastAcceptedQuestId() {
        return persistedViewState.getLastAcceptedQuestId();
    }

    public static void setLastAcceptedQuestId(long questId) {
        persistedViewState.setLastAcceptedQuestId(questId);
    }

    public static boolean isHideAcceptedQuests() {
        return persistedViewState.isHideAcceptedQuests();
    }

    public static Set<Long> getHiddenAcceptedQuestIds() {
        return persistedViewState.getHiddenAcceptedQuestIds();
    }

    public static void setHiddenAcceptedQuestState(boolean hidden, Set<Long> questIds) {
        persistedViewState.setHideAcceptedQuests(hidden);
        persistedViewState.setHiddenAcceptedQuestIds(questIds);
    }

    private static void loadPersistentTileTextures() {
        Path sourceFile = Files.isRegularFile(TILE_TEXTURES_FILE) ? TILE_TEXTURES_FILE : LEGACY_TILE_TEXTURES_FILE;
        if (!Files.isRegularFile(sourceFile)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(sourceFile)) {
            properties.load(stream);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        Map<Long, ResourceLocation> textures = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            try {
                textures.put(Long.parseUnsignedLong(key), new ResourceLocation(properties.getProperty(key)));
            } catch (Exception ex) {
                System.err.println("Ignoring invalid FTB Quests UI Overhaul tile texture entry: " + key + "=" + properties.getProperty(key));
            }
        }
        persistedViewState.setChapterTitleTextures(textures);
        if (sourceFile.equals(LEGACY_TILE_TEXTURES_FILE)) {
            savePersistentTileTextures();
        }
    }

    private static void savePersistentTileTextures() {
        Properties properties = new Properties();
        for (Map.Entry<Long, ResourceLocation> entry : persistedViewState.getChapterTitleTextures().entrySet()) {
            properties.setProperty(Long.toUnsignedString(entry.getKey()), entry.getValue().toString());
        }

        try {
            Files.createDirectories(TILE_TEXTURES_FILE.getParent());
            try (OutputStream stream = Files.newOutputStream(TILE_TEXTURES_FILE)) {
                properties.store(stream, "FTB Quests UI Overhaul selected chapter tile textures");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void loadPersistentFreePanStates() {
        if (!Files.isRegularFile(FREE_PAN_FILE)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(FREE_PAN_FILE)) {
            properties.load(stream);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        Map<Long, Boolean> freePanStates = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            try {
                freePanStates.put(Long.parseUnsignedLong(key), Boolean.parseBoolean(properties.getProperty(key)));
            } catch (Exception ex) {
                System.err.println("Ignoring invalid FTB Quests UI Overhaul free-pan entry: " + key + "=" + properties.getProperty(key));
            }
        }
        persistedViewState.setChapterFreePanStates(freePanStates);
    }

    private static void savePersistentFreePanStates() {
        Properties properties = new Properties();
        for (Map.Entry<Long, Boolean> entry : persistedViewState.getChapterFreePanStates().entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                properties.setProperty(Long.toUnsignedString(entry.getKey()), "false");
            }
        }

        try {
            Files.createDirectories(FREE_PAN_FILE.getParent());
            try (OutputStream stream = Files.newOutputStream(FREE_PAN_FILE)) {
                properties.store(stream, "FTB Quests UI Overhaul chapter free-pan preferences");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void loadPersistentChapterGroups() {
        if (!Files.isRegularFile(CHAPTER_GROUPS_FILE)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(CHAPTER_GROUPS_FILE)) {
            properties.load(stream);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        Set<Long> expandedGroups = new LinkedHashSet<>();
        for (String key : properties.stringPropertyNames()) {
            try {
                if (Boolean.parseBoolean(properties.getProperty(key))) {
                    expandedGroups.add(Long.parseUnsignedLong(key));
                }
            } catch (Exception ex) {
                System.err.println("Ignoring invalid FTB Quests UI Overhaul chapter-group entry: " + key + "=" + properties.getProperty(key));
            }
        }
        persistedViewState.setExpandedChapterGroups(expandedGroups);
    }

    private static void savePersistentChapterGroups() {
        Properties properties = new Properties();
        for (Long groupId : persistedViewState.getExpandedChapterGroups()) {
            properties.setProperty(Long.toUnsignedString(groupId), "true");
        }

        try {
            Files.createDirectories(CHAPTER_GROUPS_FILE.getParent());
            try (OutputStream stream = Files.newOutputStream(CHAPTER_GROUPS_FILE)) {
                properties.store(stream, "FTB Quests UI Overhaul expanded chapter groups");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
