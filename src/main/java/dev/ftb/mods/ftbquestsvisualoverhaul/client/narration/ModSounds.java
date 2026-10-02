package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

import dev.ftb.mods.ftbquestsvisualoverhaul.FTBQuestsVisualOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FTBQuestsVisualOverhaul.MOD_ID);

    public static final RegistryObject<SoundEvent> DIALOGUE_BLIP = SOUND_EVENTS.register("dialogue_blip",
            () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(FTBQuestsVisualOverhaul.MOD_ID, "dialogue_blip")));

    private ModSounds() {
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
