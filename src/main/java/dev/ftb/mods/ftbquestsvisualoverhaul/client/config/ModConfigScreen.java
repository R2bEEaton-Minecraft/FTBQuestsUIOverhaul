package dev.ftb.mods.ftbquestsvisualoverhaul.client.config;

import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.DescriptionAlignment;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.LayoutMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Settings screen reached from the Config button on Forge's mod list. Forge 1.20.1 ships no
 * config UI of its own, so the mod supplies this one; every control writes straight through to
 * {@link ModClientConfig} and the whole spec is saved when the screen closes.
 */
public class ModConfigScreen extends Screen {
    private static final String KEY = "config.ftbquestsvisualoverhaul.";
    private static final int ROW_HEIGHT = 24;
    private static final int WIDGET_WIDTH = 200;

    private final Screen lastScreen;

    public ModConfigScreen(Screen lastScreen) {
        super(Component.translatable(KEY + "title"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        int x = width / 2 - WIDGET_WIDTH / 2;
        int y = Math.max(40, height / 2 - ROW_HEIGHT * 3);

        addRenderableWidget(booleanOption("replace_ftbquests_screen", x, y,
                ModClientConfig.REPLACE_FTBQUESTS_SCREEN.get(), ModClientConfig.REPLACE_FTBQUESTS_SCREEN::set));
        y += ROW_HEIGHT;

        addRenderableWidget(enumOption("description_alignment", x, y, DescriptionAlignment.values(),
                ModClientConfig.DESCRIPTION_ALIGNMENT.get(), ModClientConfig.DESCRIPTION_ALIGNMENT::set));
        y += ROW_HEIGHT;

        addRenderableWidget(enumOption("default_layout", x, y, LayoutMode.values(),
                ModClientConfig.DEFAULT_LAYOUT.get(), ModClientConfig.DEFAULT_LAYOUT::set));
        y += ROW_HEIGHT;

        addRenderableWidget(booleanOption("allow_vanilla_fallback", x, y,
                ModClientConfig.ALLOW_VANILLA_FALLBACK.get(), ModClientConfig.ALLOW_VANILLA_FALLBACK::set));
        y += ROW_HEIGHT;

        addRenderableWidget(booleanOption("show_unknown_type_warning", x, y,
                ModClientConfig.SHOW_UNKNOWN_TYPE_WARNING.get(), ModClientConfig.SHOW_UNKNOWN_TYPE_WARNING::set));
        y += ROW_HEIGHT + 8;

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(x, y, WIDGET_WIDTH, 20)
                .build());
    }

    private CycleButton<Boolean> booleanOption(String name, int x, int y, boolean initial, java.util.function.Consumer<Boolean> setter) {
        return CycleButton.onOffBuilder(initial)
                .withTooltip(value -> Tooltip.create(Component.translatable(KEY + name + ".tooltip")))
                .create(x, y, WIDGET_WIDTH, 20, Component.translatable(KEY + name), (button, value) -> setter.accept(value));
    }

    private <T extends Enum<T>> CycleButton<T> enumOption(String name, int x, int y, T[] values, T initial, java.util.function.Consumer<T> setter) {
        return CycleButton.<T>builder(value -> Component.translatable(KEY + name + "." + value.name().toLowerCase(Locale.ROOT)))
                .withValues(values)
                .withInitialValue(initial)
                .withTooltip(value -> Tooltip.create(Component.translatable(KEY + name + ".tooltip")))
                .create(x, y, WIDGET_WIDTH, 20, Component.translatable(KEY + name), (button, value) -> setter.accept(value));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        ModClientConfig.SPEC.save();
        if (minecraft != null) {
            minecraft.setScreen(lastScreen);
        }
    }
}
