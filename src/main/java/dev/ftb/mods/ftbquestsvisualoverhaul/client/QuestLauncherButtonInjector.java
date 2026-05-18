package dev.ftb.mods.ftbquestsvisualoverhaul.client;

import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
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
import net.minecraftforge.event.TickEvent;

public class QuestLauncherButtonInjector {
    private static final Component QUEST_BOOK = Component.translatable("button.ftbquestsvisualoverhaul.quest_book");
    private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("minecraft", "textures/gui/recipe_button.png");
    private static final ResourceLocation NOTIFICATION_TEXTURE = new ResourceLocation("ftbquestsvisualoverhaul", "textures/icons/notification.png");

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!ClientQuestFile.exists()) {
            return;
        }

        Screen screen = event.getScreen();
        if (screen instanceof InventoryScreen inventoryScreen) {
            event.addListener(new QuestBookIconButton(inventoryScreen));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            QuestDataController.hasUnclaimedRewards();
        }
    }

    private static class QuestBookIconButton extends Button {
        private static final ItemStack ICON = new ItemStack(FTBQuestsItems.BOOK.get());
        // Place the quest book directly above the offhand slot and aligned with
        // the lower row of the 2x2 crafting grid.
        private static final int INVENTORY_X_OFFSET = 76;
        private static final int INVENTORY_Y_OFFSET = 35;
        private final InventoryScreen inventoryScreen;

        private QuestBookIconButton(InventoryScreen inventoryScreen) {
            super(0, 0, 20, 18, QUEST_BOOK, button -> ((QuestBookIconButton) button).openQuestBook(), DEFAULT_NARRATION);
            this.inventoryScreen = inventoryScreen;
            syncPosition();
            setTooltip(Tooltip.create(QUEST_BOOK));
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

        private void openQuestBook() {
            Quest targetQuest = resolveReadyToClaimQuest();
            if (targetQuest != null) {
                if (QuestScreenInterceptor.openOverhaulForQuest(targetQuest)) {
                    return;
                }

                ClientQuestFile.openGui();
                return;
            }

            ClientQuestFile.openGui();
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
            return QuestDataController.hasUnclaimedRewards();
        }

        private void renderNotificationMarker(GuiGraphics graphics) {
            graphics.blit(NOTIFICATION_TEXTURE, getX() + width - 6, getY() - 1, 0, 0, 9, 9, 9, 9);
        }

        private Quest resolveReadyToClaimQuest() {
            if (!hasUnclaimedRewards()) {
                return null;
            }

            long preferredQuestId = QuestDataController.getPreferredReadyToClaimQuestId();
            return preferredQuestId == 0L ? null : ClientQuestFile.INSTANCE.getQuest(preferredQuestId);
        }

        private void syncPosition() {
            setPosition(inventoryScreen.getGuiLeft() + INVENTORY_X_OFFSET, inventoryScreen.getGuiTop() + INVENTORY_Y_OFFSET);
        }
    }
}
