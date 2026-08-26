package dev.ftb.mods.ftbquestsvisualoverhaul.client.integration;

import dev.ftb.mods.ftbquestsvisualoverhaul.FTBQuestsVisualOverhaul;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/**
 * Captures the JEI runtime so quest requirement clicks can open a recipe lookup. JEI only loads
 * this class when it is present, which keeps {@link RecipeViewer} free of any JEI reference.
 */
@JeiPlugin
public class OverhaulJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            new ResourceLocation(FTBQuestsVisualOverhaul.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        FTBQuestsVisualOverhaul.LOGGER.info("JEI runtime captured; quest requirement clicks will open recipe lookups");
        RecipeViewer.setJeiOpener(stack -> runtime.getRecipesGui().show(
                runtime.getJeiHelpers().getFocusFactory()
                        .createFocus(RecipeIngredientRole.OUTPUT, VanillaTypes.ITEM_STACK, stack)));
    }

    @Override
    public void onRuntimeUnavailable() {
        RecipeViewer.setJeiOpener(null);
    }
}
