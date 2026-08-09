package net.lordkipama.modernminecarts.inventory;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModernMinecartsConfigScreen extends Screen {
    private static final double DEFAULT_FURNACE_MINECART_SPEED = 0.4D;
    private static final double MIN_SPEED = 0.01D;
    private static final double MAX_SPEED = 1.6D;

    private final Screen parent;
    private EditBox furnaceMinecartSpeed;
    private Component statusMessage;

    public ModernMinecartsConfigScreen(Screen parent) {
        super(Component.literal("ModernMinecarts Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.furnaceMinecartSpeed = this.addRenderableWidget(new EditBox(this.font, centerX + 10, 80, 70, 20, Component.empty()));
        this.furnaceMinecartSpeed.setValue(String.valueOf(ModernMinecartsConfig.furnaceMinecartSpeed()));

        this.addRenderableWidget(Button.builder(Component.literal("Reset"), button -> {
            this.furnaceMinecartSpeed.setValue(String.valueOf(DEFAULT_FURNACE_MINECART_SPEED));
            this.statusMessage = null;
        }).bounds(centerX + 86, 80, 60, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> saveAndClose()).bounds(centerX - 102, 130, 100, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)).bounds(centerX + 2, 130, 100, 20).build());
    }

    private void saveAndClose() {
        try {
            double value = Double.parseDouble(this.furnaceMinecartSpeed.getValue());
            if (value < MIN_SPEED || value > MAX_SPEED) {
                throw new IllegalArgumentException("furnace_minecart_speed must be between 0.01 and 1.6.");
            }
            ModernMinecartsConfig.setFurnaceMinecartSpeed(value);
            ModernMinecartsConfig.save();
            this.minecraft.setScreen(this.parent);
        } catch (NumberFormatException exception) {
            this.statusMessage = Component.literal("furnace_minecart_speed must be a number.");
        } catch (IllegalArgumentException exception) {
            this.statusMessage = Component.literal(exception.getMessage());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 30, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Furnace Minecart Speed", this.width / 2 - 150, 86, 0xFFFFFF);
        if (this.statusMessage != null) {
            guiGraphics.drawCenteredString(this.font, this.statusMessage, this.width / 2, 160, 0xFF5555);
        }
    }
}
