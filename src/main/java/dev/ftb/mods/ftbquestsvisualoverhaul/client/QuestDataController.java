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
import java.util.Map;
import java.util.Properties;

public class QuestDataController {
    private static final QuestDataSnapshotBuilder SNAPSHOT_BUILDER = new QuestDataSnapshotBuilder();
    private static final Path TILE_TEXTURES_FILE = FMLPaths.CONFIGDIR.get()
            .resolve("ftbquests")
            .resolve("quests")
            .resolve("ftbquestsvisualoverhaul_tiles.properties");
    private static final Path LEGACY_TILE_TEXTURES_FILE = FMLPaths.CONFIGDIR.get().resolve("ftbquestsvisualoverhaul_tiles.properties");

    private static QuestViewState persistedViewState = new QuestViewState();
    private static QuestDataSnapshot snapshot = new QuestDataSnapshot(java.util.List.of(), false);
    private static boolean dirty = true;

    static {
        loadPersistentTileTextures();
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
        persistedViewState = state.copy();
        savePersistentTileTextures();
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
}
