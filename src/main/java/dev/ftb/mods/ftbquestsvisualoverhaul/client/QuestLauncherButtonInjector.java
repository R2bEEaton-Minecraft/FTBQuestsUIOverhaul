package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.item.FTBQuestsItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class QuestLauncherButtonInjector {
    private static final Component QUEST_LOG = Component.literal("Quest Log");
    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("minecraft", "textures/gui/recipe_button.png");

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!ClientQuestFile.exists()) {
            return;
        }

        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen inventoryScreen) {
            int leftPos = (inventoryScreen.width - 176) / 2;
            int topPos = (inventoryScreen.height - 166) / 2;
            int x = leftPos + 126;
            int y = topPos + 61;

            event.addListener(new QuestLogIconButton(x, y));
        }
    }

    private static class QuestLogIconButton extends Button {
        private static final ItemStack ICON = new ItemStack(FTBQuestsItems.BOOK.get());

        private QuestLogIconButton(int x, int y) {
            super(x, y, 20, 18, Component.empty(), button -> FTBQuestsClient.openGui(), DEFAULT_NARRATION);
            setTooltip(Tooltip.create(QUEST_LOG));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int textureY = isHoveredOrFocused() ? 19 : 0;
            graphics.blit(RECIPE_BUTTON_TEXTURE, getX(), getY(), 0, textureY, width, height, 256, 256);
            graphics.renderFakeItem(ICON, getX() + 2, getY() + 1);

            if (hasUnclaimedRewards()) {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 250.0F);
                renderNotificationMarker(graphics);
                graphics.pose().popPose();
            }
        }

        @Override
        protected boolean isValidClickButton(int button) {
            return button == 0;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (active && visible && CommonInputs.selected(keyCode)) {
                playDownSound(Minecraft.getInstance().getSoundManager());
                onPress();
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        private boolean hasUnclaimedRewards() {
            if (!ClientQuestFile.exists() || Minecraft.getInstance().player == null) {
                return false;
            }

            UUID playerId = Minecraft.getInstance().player.getUUID();
            return ClientQuestFile.INSTANCE.selfTeamData.hasUnclaimedRewards(playerId, ClientQuestFile.INSTANCE);
        }

        private void renderNotificationMarker(GuiGraphics graphics) {
            Component marker = Component.literal("[!]");
            int markerWidth = Minecraft.getInstance().font.width(marker);
            int markerX = getX() + width - markerWidth - 1;
            int markerY = getY() + 1;
            graphics.fill(markerX - 1, markerY - 1, markerX + markerWidth, markerY + 8, 0xFFE04B4B);
            graphics.drawString(Minecraft.getInstance().font, marker, markerX, markerY, 0xFFFFFFFF, false);
        }
    }
}
