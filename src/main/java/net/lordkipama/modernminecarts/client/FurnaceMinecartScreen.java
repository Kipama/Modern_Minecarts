package net.lordkipama.modernminecarts.client;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.screen.FurnaceMinecartScreenHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FurnaceMinecartScreen extends HandledScreen<FurnaceMinecartScreenHandler> {
    private static final Identifier TEXTURE =
            ModernMinecarts.id("textures/gui/furnace_minecart_gui.png");

    public FurnaceMinecartScreen(
            FurnaceMinecartScreenHandler handler,
            PlayerInventory inventory,
            Text title
    ) {
        super(handler, inventory, title);
        backgroundWidth = 176;
        backgroundHeight = 166;
        titleX = 8;
        playerInventoryTitleY = 72;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        if (handler.isBurning()) {
            int burn = handler.getBurnProgress();
            context.drawTexture(TEXTURE, x + 80, y + 40 - burn, 176, 12 - burn, 14, burn + 1);
        }

        int speed = handler.getSpeedProgress();
        context.drawTexture(TEXTURE, x + 97, y + 62 - speed, 176, 49 - speed, 14, speed + 1);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
