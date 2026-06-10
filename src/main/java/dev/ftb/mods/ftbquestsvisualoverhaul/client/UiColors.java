package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftbquestsvisualoverhaul.FTBQuestsVisualOverhaul;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Resource-pack-overridable UI colors.
 *
 * <p>Colors are loaded from {@code assets/ftbquestsvisualoverhaul/ui_colors.json}.
 * Resource packs can ship their own copy of that file to re-color every text,
 * scrollbar, and separator the overhaul UI draws (the built-in Questbook Edition
 * pack does exactly this to restore the classic cream palette). Values accept
 * {@code "#RRGGBB"} or {@code "#AARRGGBB"} strings. Missing or invalid entries
 * fall back to the mod defaults, and the file hot-reloads with F3+T.</p>
 */
public final class UiColors implements ResourceManagerReloadListener {
    private static final ResourceLocation FILE =
            new ResourceLocation(FTBQuestsVisualOverhaul.MOD_ID, "ui_colors.json");

    // Chapter selector
    public static final String CHAPTER_BUTTON_TEXT = "chapter_button_text";
    public static final String CHAPTER_BUTTON_TEXT_HOVER = "chapter_button_text_hover";
    public static final String CHAPTER_BUTTON_TEXT_SELECTED = "chapter_button_text_selected";
    public static final String CHAPTER_BUTTON_TEXT_COMPLETED = "chapter_button_text_completed";
    public static final String CHAPTER_BUTTON_TEXT_COMPLETED_HOVER = "chapter_button_text_completed_hover";
    public static final String CHAPTER_BUTTON_TEXT_COMPLETED_SELECTED = "chapter_button_text_completed_selected";
    public static final String GROUP_HEADER_TEXT = "group_header_text";
    public static final String GROUP_HEADER_TEXT_HOVER = "group_header_text_hover";
    public static final String SELECTOR_TITLE_TEXT = "selector_title_text";
    public static final String SCROLLBAR_TRACK = "scrollbar_track";
    public static final String SCROLLBAR_THUMB = "scrollbar_thumb";
    public static final String SCROLLBAR_THUMB_DRAGGING = "scrollbar_thumb_dragging";

    // Quest tree header band
    public static final String CHAPTER_TITLE_TEXT = "chapter_title_text";
    public static final String CHAPTER_TITLE_TEXT_COMPLETED = "chapter_title_text_completed";

    // Quest detail modal
    public static final String MODAL_TITLE_TEXT = "modal_title_text";
    public static final String MODAL_CLOSE_TEXT = "modal_close_text";
    public static final String MODAL_DESCRIPTION_TEXT = "modal_description_text";
    public static final String MODAL_SECTION_LABEL_TEXT = "modal_section_label_text";
    public static final String MODAL_SECTION_COUNT_TEXT = "modal_section_count_text";
    public static final String MODAL_SECTION_COUNT_DONE_TEXT = "modal_section_count_done_text";
    public static final String MODAL_SECTION_EMPTY_TEXT = "modal_section_empty_text";
    public static final String MODAL_OBJECTIVE_TEXT = "modal_objective_text";
    public static final String SECTION_DIVIDER = "section_divider";
    public static final String SECTION_DIVIDER_SHADOW = "section_divider_shadow";

    // Tooltips
    public static final String TOOLTIP_TASK_DONE_TEXT = "tooltip_task_done_text";
    public static final String TOOLTIP_TASK_PENDING_TEXT = "tooltip_task_pending_text";
    public static final String TOOLTIP_FALLBACK_TEXT = "tooltip_fallback_text";
    public static final String TOOLTIP_REWARD_CLAIMABLE_TEXT = "tooltip_reward_claimable_text";
    public static final String TOOLTIP_REWARD_CLAIMED_TEXT = "tooltip_reward_claimed_text";
    public static final String TOOLTIP_REWARD_PENDING_TEXT = "tooltip_reward_pending_text";

    // Choice reward screen
    public static final String CHOICE_TITLE_TEXT = "choice_title_text";
    public static final String CHOICE_SUBTITLE_TEXT = "choice_subtitle_text";
    public static final String CHOICE_CLOSE_TEXT = "choice_close_text";
    public static final String CHOICE_ROW_TEXT = "choice_row_text";
    public static final String CHOICE_ACTION_TEXT = "choice_action_text";
    public static final String CHOICE_ACTION_TEXT_HOVER = "choice_action_text_hover";
    public static final String CHOICE_SCROLL_INDICATOR = "choice_scroll_indicator";
    public static final String CHOICE_SCROLL_INDICATOR_DISABLED = "choice_scroll_indicator_disabled";

    private static final Map<String, Integer> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put(CHAPTER_BUTTON_TEXT, 0xFFE0E0E0);
        DEFAULTS.put(CHAPTER_BUTTON_TEXT_HOVER, 0xFFFFFFFF);
        DEFAULTS.put(CHAPTER_BUTTON_TEXT_SELECTED, 0xFFFFFFFF);
        DEFAULTS.put(CHAPTER_BUTTON_TEXT_COMPLETED, 0xFF7BD45F);
        DEFAULTS.put(CHAPTER_BUTTON_TEXT_COMPLETED_HOVER, 0xFF92E574);
        DEFAULTS.put(CHAPTER_BUTTON_TEXT_COMPLETED_SELECTED, 0xFF9BF27B);
        DEFAULTS.put(GROUP_HEADER_TEXT, 0xFFE0E0E0);
        DEFAULTS.put(GROUP_HEADER_TEXT_HOVER, 0xFFFFFFFF);
        DEFAULTS.put(SELECTOR_TITLE_TEXT, 0xFFFFFFFF);
        DEFAULTS.put(SCROLLBAR_TRACK, 0x44303030);
        DEFAULTS.put(SCROLLBAR_THUMB, 0xCCB0B0B0);
        DEFAULTS.put(SCROLLBAR_THUMB_DRAGGING, 0xFFE8E8E8);
        DEFAULTS.put(CHAPTER_TITLE_TEXT, 0xFFFFFFFF);
        DEFAULTS.put(CHAPTER_TITLE_TEXT_COMPLETED, 0xFF9BF27B);
        DEFAULTS.put(MODAL_TITLE_TEXT, 0xFFFFFFFF);
        DEFAULTS.put(MODAL_CLOSE_TEXT, 0xFFE0E0E0);
        DEFAULTS.put(MODAL_DESCRIPTION_TEXT, 0xFFD0D0D0);
        DEFAULTS.put(MODAL_SECTION_LABEL_TEXT, 0xFFE8E8E8);
        DEFAULTS.put(MODAL_SECTION_COUNT_TEXT, 0xFFE8E8E8);
        DEFAULTS.put(MODAL_SECTION_COUNT_DONE_TEXT, 0xFF6FE142);
        DEFAULTS.put(MODAL_SECTION_EMPTY_TEXT, 0xFFAAAAAA);
        DEFAULTS.put(MODAL_OBJECTIVE_TEXT, 0xFF6FE142);
        DEFAULTS.put(SECTION_DIVIDER, 0x88909090);
        DEFAULTS.put(SECTION_DIVIDER_SHADOW, 0x44101010);
        DEFAULTS.put(TOOLTIP_TASK_DONE_TEXT, 0xFF75D65C);
        DEFAULTS.put(TOOLTIP_TASK_PENDING_TEXT, 0xFFCCCCCC);
        DEFAULTS.put(TOOLTIP_FALLBACK_TEXT, 0xFFAAAAAA);
        DEFAULTS.put(TOOLTIP_REWARD_CLAIMABLE_TEXT, 0xFF7DE25E);
        DEFAULTS.put(TOOLTIP_REWARD_CLAIMED_TEXT, 0xFFB6B6B6);
        DEFAULTS.put(TOOLTIP_REWARD_PENDING_TEXT, 0xFFCCCCCC);
        DEFAULTS.put(CHOICE_TITLE_TEXT, 0xFFFFFFFF);
        DEFAULTS.put(CHOICE_SUBTITLE_TEXT, 0xFFC8C8C8);
        DEFAULTS.put(CHOICE_CLOSE_TEXT, 0xFFE0E0E0);
        DEFAULTS.put(CHOICE_ROW_TEXT, 0xFFE0E0E0);
        DEFAULTS.put(CHOICE_ACTION_TEXT, 0xFFB8B8B8);
        DEFAULTS.put(CHOICE_ACTION_TEXT_HOVER, 0xFFFFFFFF);
        DEFAULTS.put(CHOICE_SCROLL_INDICATOR, 0xFFD0D0D0);
        DEFAULTS.put(CHOICE_SCROLL_INDICATOR_DISABLED, 0xAA606060);
    }

    private static volatile Map<String, Integer> colors = new HashMap<>(DEFAULTS);

    /** Returns the color for {@code key} as ARGB. */
    public static int get(String key) {
        Integer value = colors.get(key);
        if (value == null) {
            value = DEFAULTS.get(key);
        }
        return value != null ? value : 0xFFFFFFFF;
    }

    /** Returns the color for {@code key} as RGB without alpha (for text Styles). */
    public static int rgb(String key) {
        return get(key) & 0xFFFFFF;
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        Map<String, Integer> loaded = new HashMap<>(DEFAULTS);
        Optional<Resource> resource = manager.getResource(FILE);
        if (resource.isPresent()) {
            try (BufferedReader reader = resource.get().openAsReader()) {
                JsonObject json = new Gson().fromJson(reader, JsonObject.class);
                if (json != null) {
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        Integer parsed = parseColor(entry.getValue().getAsString());
                        if (parsed != null) {
                            loaded.put(entry.getKey(), parsed);
                        } else {
                            FTBQuestsVisualOverhaul.LOGGER.warn("Invalid color '{}' for key '{}' in {}",
                                    entry.getValue().getAsString(), entry.getKey(), FILE);
                        }
                    }
                }
            } catch (Exception ex) {
                FTBQuestsVisualOverhaul.LOGGER.warn("Failed to read {}", FILE, ex);
            }
        }
        colors = loaded;
    }

    private static Integer parseColor(String raw) {
        if (raw == null) {
            return null;
        }
        String hex = raw.trim().toUpperCase(Locale.ROOT);
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        } else if (hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        try {
            if (hex.length() == 6) {
                return 0xFF000000 | Integer.parseInt(hex, 16);
            }
            if (hex.length() == 8) {
                return (int) Long.parseLong(hex, 16);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    @Mod.EventBusSubscriber(modid = FTBQuestsVisualOverhaul.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ReloadHandler {
        private ReloadHandler() {
        }

        @SubscribeEvent
        public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener(new UiColors());
        }
    }
}
