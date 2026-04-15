package dev.ftb.mods.ftbquestsvisualoverhaul;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.OverhaulClient;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.config.ModClientConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(FTBQuestsVisualOverhaul.MOD_ID)
public class FTBQuestsVisualOverhaul {
    public static final String MOD_ID = "ftbquestsvisualoverhaul";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FTBQuestsVisualOverhaul() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> OverhaulClient::init);
    }
}
