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
import net.minecraft.world.level.GameType;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CleanUiModeOverlay {
    private static final int BUTTON_WIDTH = 92;
    private static final int BUTTON_HEIGHT = 14;
    private static final int BUTTON_MARGIN = 8;

    private CleanUiModeOverlay() {
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        QuestScreen questScreen = getDefaultQuestScreen(event.getScreen());
        if (questScreen == null || !shouldShow()) {
            return;
        }

        Rect rect = buttonRect(event.getScreen());
        boolean hovered = rect.contains(event.getMouseX(), event.getMouseY());
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.pose().pushPose();
        graphics.pose().translate(0F, 0F, 500F);
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.maxY(), hovered ? 0xCC2A2118 : 0xAA201812);
        graphics.fill(rect.x(), rect.y(), rect.maxX(), rect.y() + 1, hovered ? 0xFFE2C98E : 0xCC9A7C50);
        graphics.fill(rect.x(), rect.maxY() - 1, rect.maxX(), rect.maxY(), 0xCC120D09);
        graphics.fill(rect.x(), rect.y(), rect.x() + 1, rect.maxY(), hovered ? 0xFFE2C98E : 0xCC9A7C50);
        graphics.fill(rect.maxX() - 1, rect.y(), rect.maxX(), rect.maxY(), 0xCC120D09);
        graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal("Clean UI Mode"), rect.centerX(), rect.y() + 3, 0xFFEFE1C1);
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
        return new Rect(screen.width - BUTTON_WIDTH - BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_WIDTH, BUTTON_HEIGHT);
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
