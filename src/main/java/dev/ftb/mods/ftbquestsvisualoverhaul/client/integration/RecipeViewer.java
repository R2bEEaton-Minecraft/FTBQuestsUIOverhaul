package dev.ftb.mods.ftbquestsvisualoverhaul.client.integration;

import dev.ftb.mods.ftbquests.FTBQuests;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Opens a recipe viewer for an item stack.
 *
 * <p>FTB Quests exposes {@code FTBQuests.getRecipeModHelper()} for this, but FTB Quests 2001.x
 * ships no recipe-mod integration in its own jar - the helper is only ever populated by the
 * separate FTB XMod Compat mod. Without that installed the helper stays a no-op that silently
 * swallows every call, so the addon talks to JEI directly and keeps the FTB helper as a second
 * choice (it covers REI and EMI users who do have XMod Compat).</p>
 *
 * <p>No JEI class is named here: {@link OverhaulJeiPlugin} hands over a plain {@link Consumer},
 * so this class stays loadable when JEI is absent.</p>
 */
public final class RecipeViewer {
    private static Consumer<ItemStack> jeiOpener;

    private RecipeViewer() {
    }

    static void setJeiOpener(Consumer<ItemStack> opener) {
        jeiOpener = opener;
    }

    public static boolean isAvailable() {
        return jeiOpener != null || FTBQuests.getRecipeModHelper().isRecipeModAvailable();
    }

    /**
     * @return true when a recipe viewer actually took the stack, false when the caller should
     * fall back to another interaction.
     */
    public static boolean showRecipes(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        Consumer<ItemStack> opener = jeiOpener;
        if (opener != null) {
            opener.accept(stack);
            return true;
        }

        if (FTBQuests.getRecipeModHelper().isRecipeModAvailable()) {
            FTBQuests.getRecipeModHelper().showRecipes(stack);
            return true;
        }

        return false;
    }
}
