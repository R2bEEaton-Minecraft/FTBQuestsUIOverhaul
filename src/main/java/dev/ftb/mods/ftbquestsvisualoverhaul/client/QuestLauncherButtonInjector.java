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
    private static final ResourceLocation ALERT_TEXTURE = new ResourceLocation("ftbquests", "textures/gui/alert.png");

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!ClientQuestFile.exists()) {
            return;
        }

        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen inventoryScreen) {
            event.addListener(new QuestLogIconButton(inventoryScreen));
        }
    }

    private static class QuestLogIconButton extends Button {
        private static final ItemStack ICON = new ItemStack(FTBQuestsItems.BOOK.get());
        private static final int INVENTORY_X_OFFSET = 126;
        private static final int INVENTORY_Y_OFFSET = 61;
        private final InventoryScreen inventoryScreen;

        private QuestLogIconButton(InventoryScreen inventoryScreen) {
            super(0, 0, 20, 18, Component.empty(), button -> FTBQuestsClient.openGui(), DEFAULT_NARRATION);
            this.inventoryScreen = inventoryScreen;
            syncPosition();
            setTooltip(Tooltip.create(QUEST_LOG));
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            syncPosition();
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
            graphics.pose().pushPose();
            graphics.pose().translate(getX() + width - 8, getY() - 1, 0.0F);
            graphics.pose().scale(0.5F, 0.5F, 1.0F);
            graphics.blit(ALERT_TEXTURE, 0, 0, 0.0F, 0.0F, 16, 16, 16, 16);
            graphics.pose().popPose();
        }

        private void syncPosition() {
            setPosition(inventoryScreen.getGuiLeft() + INVENTORY_X_OFFSET, inventoryScreen.getGuiTop() + INVENTORY_Y_OFFSET);
        }
    }
}
