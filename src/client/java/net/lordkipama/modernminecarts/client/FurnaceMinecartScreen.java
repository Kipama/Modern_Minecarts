package net.lordkipama.modernminecarts.client;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.screen.FurnaceMinecartScreenHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FurnaceMinecartScreen extends AbstractContainerScreen<FurnaceMinecartScreenHandler> {
    private static final Identifier TEXTURE = ModernMinecarts.id("textures/gui/furnace_minecart_gui.png");

    public FurnaceMinecartScreen(FurnaceMinecartScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelX = 8;
        this.inventoryLabelY = 72;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        if (this.menu.isBurning()) {
            int burn = this.menu.getBurnProgress();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 80, y + 40 - burn, 176.0F, 12.0F - burn, 14, burn + 1, 256, 256);
        }

        int speed = this.menu.getSpeedProgress();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 97, y + 62 - speed, 176.0F, 49.0F - speed, 14, speed + 1, 256, 256);
    }
}
