package dev.ftb.mods.ftbquestsvisualoverhaul.client.narration;

import dev.ftb.mods.ftbquestsvisualoverhaul.FTBQuestsVisualOverhaul;
import net.minecraft.resources.ResourceLocation;

/**
 * Sound locations defined in {@code sounds.json}. Deliberately not a Forge registry entry:
 * the blip is only ever played client-side by location, and registered sound events are
 * stripped from the client when it joins a server that does not have this mod, which made
 * {@code RegistryObject.get()} throw while rendering a quest.
 */
public final class ModSounds {
    public static final ResourceLocation DIALOGUE_BLIP =
            new ResourceLocation(FTBQuestsVisualOverhaul.MOD_ID, "dialogue_blip");

    private ModSounds() {
    }
}
