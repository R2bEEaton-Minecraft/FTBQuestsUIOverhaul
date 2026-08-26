package dev.ftb.mods.ftbquestsvisualoverhaul.client.config;

import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.DescriptionAlignment;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.LayoutMode;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue REPLACE_FTBQUESTS_SCREEN;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VANILLA_FALLBACK;
    public static final ForgeConfigSpec.EnumValue<LayoutMode> DEFAULT_LAYOUT;
    public static final ForgeConfigSpec.BooleanValue SHOW_UNKNOWN_TYPE_WARNING;
    public static final ForgeConfigSpec.EnumValue<DescriptionAlignment> DESCRIPTION_ALIGNMENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("ui");
        REPLACE_FTBQUESTS_SCREEN = builder
                .comment("Replace FTB Quests' player-facing quest screen with the addon UI.")
                .define("replace_ftbquests_screen", true);
        ALLOW_VANILLA_FALLBACK = builder
                .comment("Allow the addon to temporarily open the vanilla FTB Quests screen for unsupported interactions.")
                .define("allow_vanilla_fallback", true);
        DEFAULT_LAYOUT = builder
                .comment("Default quest browser layout.")
                .defineEnum("default_layout", LayoutMode.GRAPH);
        SHOW_UNKNOWN_TYPE_WARNING = builder
                .comment("Show a warning banner when a task or reward is routed to vanilla fallback.")
                .define("show_unknown_type_warning", true);
        DESCRIPTION_ALIGNMENT = builder
                .comment("Horizontal alignment for quest description text in the detail modal.",
                        "CENTER matches the framed UI artwork, LEFT reads more like a book page.",
                        "Images keep whatever alignment the quest author gave them either way.")
                .defineEnum("description_alignment", DescriptionAlignment.CENTER);
        builder.pop();

        SPEC = builder.build();
    }

    private ModClientConfig() {
    }
}
