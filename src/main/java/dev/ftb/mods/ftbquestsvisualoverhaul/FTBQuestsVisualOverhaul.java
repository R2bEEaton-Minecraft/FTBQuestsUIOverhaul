package dev.ftb.mods.ftbquestsvisualoverhaul;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.OverhaulClient;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModClientConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(FTBQuestsVisualOverhaul.MOD_ID)
public class FTBQuestsVisualOverhaul {
    public static final String MOD_ID = "ftbquestsvisualoverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBQuestsVisualOverhaul() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addPackFinders);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> OverhaulClient::init);
    }

    /**
     * Registers the built-in "Questbook Edition" resource pack (the classic brown
     * quest book textures). The vanilla-style textures are the mod's defaults;
     * players can enable this pack from the standard resource pack menu.
     *
     * <p>The pack lives in {@code src/main/resources/resourcepacks/questbook_edition}.
     * See {@code RESOURCE_PACKS.md} in the repository root for how to create
     * custom packs that re-skin the overhaul UI.</p>
     */
    private void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        Path resourcePath = ModList.get().getModFileById(MOD_ID).getFile()
                .findResource("resourcepacks/questbook_edition");
        Pack pack = Pack.readMetaAndCreate(
                "builtin/questbook_edition",
                Component.literal("Questbook Edition"),
                false,
                path -> new PathPackResources(path, resourcePath, false),
                PackType.CLIENT_RESOURCES,
                Pack.Position.TOP,
                PackSource.BUILT_IN);
        if (pack != null) {
            event.addRepositorySource(consumer -> consumer.accept(pack));
        } else {
            LOGGER.warn("Questbook Edition built-in resource pack could not be loaded");
        }
    }
}
