package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.screen.OverhaulQuestScreen;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestOpenContext;
import dev.ftb.mods.ftbquestsvisualoverhaul.client.state.QuestViewState;
import dev.ftb.mods.ftblibrary.ui.ScreenWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class CleanUiModeOverlay {
    private static final int BUTTON_HEIGHT = 10;
    private static final int BUTTON_MIN_WIDTH = 32;
    private static final int BUTTON_PADDING_X = 4;
    private static final float BUTTON_TEXT_SCALE = 0.5F;
    private static final double BUTTON_LEFT_SHIFT_RATIO = 0.16D;
    private static final ResourceLocation BUTTONS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final Component BUTTON_LABEL = Component.translatable("button.ftbquestsvisualoverhaul.clean_ui_mode");
    private static long handCursor;
    private static boolean handCursorActive;

    private CleanUiModeOverlay() {
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        QuestScreen questScreen = getDefaultQuestScreen(event.getScreen());
        if (questScreen == null || !shouldShow()) {
            setHandCursor(false);
            return;
        }

        Rect rect = buttonRect(event.getScreen());
        boolean hovered = rect.contains(event.getMouseX(), event.getMouseY());
        setHandCursor(hovered);
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.pose().pushPose();
        graphics.pose().translate(0F, 0F, 500F);
        drawVanillaButton(graphics, rect, hovered);
        float scaledTextWidth = Minecraft.getInstance().font.width(BUTTON_LABEL) * BUTTON_TEXT_SCALE;
        float scaledTextHeight = Minecraft.getInstance().font.lineHeight * BUTTON_TEXT_SCALE;
        float textX = rect.x() + (rect.width() - scaledTextWidth) * 0.5F;
        float textY = rect.y() + (rect.height() - scaledTextHeight) * 0.5F;
        drawScaledString(graphics, BUTTON_LABEL, textX, textY, hovered ? 0xFFFFA0 : 0xE0E0E0, BUTTON_TEXT_SCALE);
        graphics.pose().popPose();
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        QuestScreen questScreen = getDefaultQuestScreen(event.getScreen());
        if (questScreen == null || !shouldShow() || event.getButton() != 0) {
            return;
        }

        Rect rect = buttonRect(event.getScreen());
        if (!rect.contains(event.getMouseX(), event.getMouseY())) {
            return;
        }

        QuestViewState state = QuestDataController.copyPersistedViewState();
        state.setDefaultFtbUiMode(false);
        QuestDataController.saveViewState(state);
        Minecraft.getInstance().setScreen(new OverhaulQuestScreen(QuestOpenContext.fromVanilla(questScreen, state)));
        event.setCanceled(true);
    }

    private static QuestScreen getDefaultQuestScreen(Screen screen) {
        if (screen instanceof ScreenWrapper wrapper && wrapper.getGui() instanceof QuestScreen questScreen) {
            return questScreen;
        }
        return null;
    }

    private static boolean shouldShow() {
        Minecraft minecraft = Minecraft.getInstance();
        return ClientQuestFile.exists()
                && (minecraft.gameMode == null || minecraft.gameMode.getPlayerMode() != GameType.SURVIVAL);
    }

    private static Rect buttonRect(Screen screen) {
        int scaledTextWidth = Math.round(Minecraft.getInstance().font.width(BUTTON_LABEL) * BUTTON_TEXT_SCALE);
        int width = Math.max(BUTTON_MIN_WIDTH, scaledTextWidth + BUTTON_PADDING_X * 2);
        int x = screen.width - width - (int) Math.round(screen.width * BUTTON_LEFT_SHIFT_RATIO);
        int y = screen.height - BUTTON_HEIGHT;
        return new Rect(x, y, width, BUTTON_HEIGHT);
    }

    private static void drawVanillaButton(GuiGraphics graphics, Rect rect, boolean hovered) {
        int textureY = hovered ? 86 : 66;
        float scale = rect.height() / 20F;
        int logicalWidth = Math.max(2, Math.round(rect.width() / scale));
        int halfWidth = logicalWidth / 2;
        graphics.pose().pushPose();
        graphics.pose().translate(rect.x(), rect.y(), 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.blit(BUTTONS_LOCATION, 0, 0, 0, textureY, halfWidth, 20, 256, 256);
        graphics.blit(BUTTONS_LOCATION, halfWidth, 0, 200 - (logicalWidth - halfWidth), textureY, logicalWidth - halfWidth, 20, 256, 256);
        graphics.pose().popPose();
    }

    private static void drawScaledString(GuiGraphics graphics, Component component, float x, float y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0F);
        graphics.pose().scale(scale, scale, 1F);
        graphics.drawString(Minecraft.getInstance().font, component, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static void setHandCursor(boolean hovered) {
        if (handCursorActive == hovered) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow() == null) {
            return;
        }

        long window = minecraft.getWindow().getWindow();
        if (hovered) {
            if (handCursor == 0L) {
                handCursor = GLFW.glfwCreateStandardCursor(GLFW.GLFW_HAND_CURSOR);
            }
            GLFW.glfwSetCursor(window, handCursor);
        } else {
            GLFW.glfwSetCursor(window, 0L);
        }
        handCursorActive = hovered;
    }

    private record Rect(int x, int y, int width, int height) {
        int maxX() {
            return x + width;
        }

        int maxY() {
            return y + height;
        }

        int centerX() {
            return x + width / 2;
        }

        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= maxX() && mouseY >= y && mouseY <= maxY();
        }
    }
}
