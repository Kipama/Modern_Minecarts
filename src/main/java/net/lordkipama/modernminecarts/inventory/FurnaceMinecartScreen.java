package net.lordkipama.modernminecarts.inventory;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;

public class FurnaceMinecartScreen extends AbstractContainerScreen<FurnaceMinecartMenu> {
    private final Identifier texture;

    public FurnaceMinecartScreen(FurnaceMinecartMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        texture = Identifier.fromNamespaceAndPath("modernminecarts", "textures/gui/furnace_minecart_gui.png");
    }

    public void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.isLit()) {
            int k = this.menu.getLitProgress();
            pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, i + 80, j + 40 - k, 176.0F, (float) (12 - k), 14, k + 1, 256, 256);
        }

        int k = this.menu.getSpeed();
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.texture, i + 97, j + 62 - k, 176.0F, (float) (49 - k), 14, k + 1, 256, 256);
    }

    /**
     * Called when the mouse is clicked over a slot or outside the gui.
     */
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ContainerInput pType) {
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
    }

    protected boolean hasClickedOutside(double pMouseX, double pMouseY, int pGuiLeft, int pGuiTop) {
        return pMouseX < (double)pGuiLeft || pMouseY < (double)pGuiTop || pMouseX >= (double)(pGuiLeft + this.imageWidth) || pMouseY >= (double)(pGuiTop + this.imageHeight);
    }
}
