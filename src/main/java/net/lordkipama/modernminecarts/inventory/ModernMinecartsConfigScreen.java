package net.lordkipama.modernminecarts.inventory;

import java.util.List;
import java.util.function.Consumer;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModernMinecartsConfigScreen extends Screen {
    private static final double MIN_SPEED = 0.01D;
    private static final double MAX_SPEED = 1.6D;
    private static final int ROW_HEIGHT = 24;
    private static final int COLUMN_GAP = 8;
    private static final int LABEL_WIDTH = 225;
    private static final int INPUT_WIDTH = 55;
    private static final int RESET_WIDTH = 60;
    private static final int TABLE_WIDTH = LABEL_WIDTH + INPUT_WIDTH + RESET_WIDTH + COLUMN_GAP * 2;
    private static final int FOOTER_HEIGHT = 30;
    private static final int TITLE_HEIGHT = ROW_HEIGHT;
    private static final int MIN_RECIPE_YIELD = 1;
    private static final int MAX_RECIPE_YIELD = 64;

    private static final double DEFAULT_COPPER_SPEED = 0.8D;
    private static final double DEFAULT_EXPOSED_COPPER_SPEED = 0.6D;
    private static final double DEFAULT_WEATHERED_COPPER_SPEED = 0.3D;
    private static final double DEFAULT_OXIDIZED_COPPER_SPEED = 0.2D;
    private static final double DEFAULT_POWERED_RAIL_SPEED = 0.4D;
    private static final double DEFAULT_MAX_ASCENDING_SPEED = 0.5D;
    private static final boolean DEFAULT_ENABLE_FURNACE_MINECART_CHUNKLOADING = true;
    private static final boolean DEFAULT_ENABLE_MINECART_CHAINING = true;
    private static final boolean DEFAULT_ENABLE_COPPER_RAILS = true;
    private static final boolean DEFAULT_ENABLE_RAIL_CROSSING = true;
    private static final boolean DEFAULT_ENABLE_POWERED_DETECTOR_RAIL = true;
    private static final boolean DEFAULT_ENABLE_RAIL_JUMP = true;
    private static final int DEFAULT_COPPER_RAIL_RECIPE_YIELD = 6;
    private static final int DEFAULT_POWERED_RAIL_RECIPE_YIELD = 12;

    private final Screen parent;
    private ConfigList configList;
    private EditBox copperSpeed;
    private EditBox exposedCopperSpeed;
    private EditBox weatheredCopperSpeed;
    private EditBox oxidizedCopperSpeed;
    private EditBox poweredRailSpeed;
    private EditBox maxAscendingSpeed;
    private ToggleButton enableFurnaceMinecartChunkloading;
    private ToggleButton enableMinecartChaining;
    private ToggleButton enableCopperRails;
    private ToggleButton enableRailCrossing;
    private ToggleButton enablePoweredDetectorRail;
    private ToggleButton enableRailJump;
    private EditBox copperRailRecipeYield;
    private EditBox poweredRailRecipeYield;
    private Component statusMessage;

    public ModernMinecartsConfigScreen(Screen parent) {
        super(Component.literal("ModernMinecarts Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int listTop = TITLE_HEIGHT + 4;
        int footerTop = this.height - FOOTER_HEIGHT;

        this.configList = this.addRenderableWidget(new ConfigList(this.minecraft, this.width, footerTop - listTop, listTop, ROW_HEIGHT));
        this.addConfigEntries();

        int centerX = this.width / 2;
        int buttonY = footerTop + 5;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> saveAndClose()).bounds(centerX - 102, buttonY, 100, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)).bounds(centerX + 2, buttonY, 100, 20).build());
    }

    private void addConfigEntries() {
        this.configList.addConfigEntry(new CategoryEntry("Rail Speeds"));

        this.copperSpeed = createNumberField(ModernMinecartsConfig.copperSpeed());
        this.configList.addConfigEntry(new NumberEntry("Copper Speed", this.copperSpeed, DEFAULT_COPPER_SPEED));

        this.exposedCopperSpeed = createNumberField(ModernMinecartsConfig.exposedCopperSpeed());
        this.configList.addConfigEntry(new NumberEntry("Exposed Copper Speed", this.exposedCopperSpeed, DEFAULT_EXPOSED_COPPER_SPEED));

        this.weatheredCopperSpeed = createNumberField(ModernMinecartsConfig.weatheredCopperSpeed());
        this.configList.addConfigEntry(new NumberEntry("Weathered Copper Speed", this.weatheredCopperSpeed, DEFAULT_WEATHERED_COPPER_SPEED));

        this.oxidizedCopperSpeed = createNumberField(ModernMinecartsConfig.oxidizedCopperSpeed());
        this.configList.addConfigEntry(new NumberEntry("Oxidized Copper Speed", this.oxidizedCopperSpeed, DEFAULT_OXIDIZED_COPPER_SPEED));

        this.poweredRailSpeed = createNumberField(ModernMinecartsConfig.poweredRailSpeed());
        this.configList.addConfigEntry(new NumberEntry("Powered Rail Speed", this.poweredRailSpeed, DEFAULT_POWERED_RAIL_SPEED));

        this.maxAscendingSpeed = createNumberField(ModernMinecartsConfig.maxAscendingSpeed());
        this.configList.addConfigEntry(new NumberEntry("Max Ascending Speed", this.maxAscendingSpeed, DEFAULT_MAX_ASCENDING_SPEED));

        this.configList.addConfigEntry(new CategoryEntry("New Features"));

        this.enableFurnaceMinecartChunkloading = createToggleButton(ModernMinecartsConfig.enableFurnaceMinecartChunkloading());
        this.configList.addConfigEntry(new ToggleEntry("Enable Furnace Minecart Chunkloading", this.enableFurnaceMinecartChunkloading, DEFAULT_ENABLE_FURNACE_MINECART_CHUNKLOADING));

        this.enableMinecartChaining = createToggleButton(ModernMinecartsConfig.enableMinecartChaining());
        this.configList.addConfigEntry(new ToggleEntry("Enable Minecart Chaining", this.enableMinecartChaining, DEFAULT_ENABLE_MINECART_CHAINING));

        this.enableCopperRails = createToggleButton(ModernMinecartsConfig.enableCopperRails());
        this.configList.addConfigEntry(new ToggleEntry("Enable Copper Rails", this.enableCopperRails, DEFAULT_ENABLE_COPPER_RAILS));

        this.enableRailCrossing = createToggleButton(ModernMinecartsConfig.enableRailCrossing());
        this.configList.addConfigEntry(new ToggleEntry("Enable Rail Crossing", this.enableRailCrossing, DEFAULT_ENABLE_RAIL_CROSSING));

        this.enablePoweredDetectorRail = createToggleButton(ModernMinecartsConfig.enablePoweredDetectorRail());
        this.configList.addConfigEntry(new ToggleEntry("Enable Powered Detector Rail", this.enablePoweredDetectorRail, DEFAULT_ENABLE_POWERED_DETECTOR_RAIL));

        this.enableRailJump = createToggleButton(ModernMinecartsConfig.enableRailJump());
        this.configList.addConfigEntry(new ToggleEntry("Enable Rail Jump", this.enableRailJump, DEFAULT_ENABLE_RAIL_JUMP));

        this.configList.addConfigEntry(new CategoryEntry("Crafting Recipes"));

        this.copperRailRecipeYield = createIntegerField(ModernMinecartsConfig.copperRailRecipeYield());
        this.configList.addConfigEntry(new IntegerEntry("Copper Rail Recipe Yield", this.copperRailRecipeYield, DEFAULT_COPPER_RAIL_RECIPE_YIELD));

        this.poweredRailRecipeYield = createIntegerField(ModernMinecartsConfig.poweredRailRecipeYield());
        this.configList.addConfigEntry(new IntegerEntry("Powered Rail Recipe Yield", this.poweredRailRecipeYield, DEFAULT_POWERED_RAIL_RECIPE_YIELD));
    }

    private EditBox createNumberField(float value) {
        EditBox box = new EditBox(this.font, 0, 0, INPUT_WIDTH, 20, Component.empty());
        box.setValue(String.valueOf(value));
        return box;
    }

    private ToggleButton createToggleButton(boolean value) {
        return new ToggleButton(0, 0, INPUT_WIDTH, 20, value, toggled -> this.statusMessage = null);
    }

    private EditBox createIntegerField(int value) {
        EditBox box = new EditBox(this.font, 0, 0, INPUT_WIDTH, 20, Component.empty());
        box.setValue(String.valueOf(value));
        return box;
    }

    private static Component toggleMessage(boolean value) {
        return value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF;
    }

    private static boolean toggleValue(ToggleButton button) {
        return button.getValue();
    }

    private void saveAndClose() {
        try {
            ModernMinecartsConfig.setCopperSpeed(parseRanged(this.copperSpeed.getValue(), "copper_speed"));
            ModernMinecartsConfig.setExposedCopperSpeed(parseRanged(this.exposedCopperSpeed.getValue(), "exposed_copper_speed"));
            ModernMinecartsConfig.setWeatheredCopperSpeed(parseRanged(this.weatheredCopperSpeed.getValue(), "weathered_copper_speed"));
            ModernMinecartsConfig.setOxidizedCopperSpeed(parseRanged(this.oxidizedCopperSpeed.getValue(), "oxidized_copper_speed"));
            ModernMinecartsConfig.setPoweredRailSpeed(parseRanged(this.poweredRailSpeed.getValue(), "powered_rail_speed"));
            ModernMinecartsConfig.setMaxAscendingSpeed(parseRanged(this.maxAscendingSpeed.getValue(), "max_ascending_speed"));
            ModernMinecartsConfig.setEnableFurnaceMinecartChunkloading(toggleValue(this.enableFurnaceMinecartChunkloading));
            ModernMinecartsConfig.setEnableMinecartChaining(toggleValue(this.enableMinecartChaining));
            ModernMinecartsConfig.setEnableCopperRails(toggleValue(this.enableCopperRails));
            ModernMinecartsConfig.setEnableRailCrossing(toggleValue(this.enableRailCrossing));
            ModernMinecartsConfig.setEnablePoweredDetectorRail(toggleValue(this.enablePoweredDetectorRail));
            ModernMinecartsConfig.setEnableRailJump(toggleValue(this.enableRailJump));
            ModernMinecartsConfig.setCopperRailRecipeYield(parseRecipeYield(this.copperRailRecipeYield.getValue(), "copper_rail_recipe_yield"));
            ModernMinecartsConfig.setPoweredRailRecipeYield(parseRecipeYield(this.poweredRailRecipeYield.getValue(), "powered_rail_recipe_yield"));
            ModernMinecartsConfig.save();
            this.minecraft.setScreen(this.parent);
        } catch (IllegalArgumentException exception) {
            this.statusMessage = Component.literal(exception.getMessage());
        }
    }

    private static double parseRanged(String input, String key) {
        double value;

        try {
            value = Double.parseDouble(input);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be a number.");
        }

        if (value < MIN_SPEED || value > MAX_SPEED) {
            throw new IllegalArgumentException(key + " must be between 0.01 and 1.6.");
        }

        return value;
    }

    private static int parseRecipeYield(String input, String key) {
        int value;

        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be a whole number.");
        }

        if (value < MIN_RECIPE_YIELD || value > MAX_RECIPE_YIELD) {
            throw new IllegalArgumentException(key + " must be between 1 and 64.");
        }

        return value;
    }

    private static boolean isDefaultNumberValue(String input, double defaultValue) {
        try {
            return Math.abs(Double.parseDouble(input) - defaultValue) < 0.00001D;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static boolean isDefaultIntegerValue(String input, int defaultValue) {
        try {
            return Integer.parseInt(input) == defaultValue;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int footerTop = this.height - FOOTER_HEIGHT;
        guiGraphics.fill(0, footerTop, this.width, this.height, 0xAA000000);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int titleY = (TITLE_HEIGHT - this.font.lineHeight) / 2;
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, 0xFFFFFF);
        if (this.statusMessage != null) {
            guiGraphics.drawCenteredString(this.font, this.statusMessage, this.width / 2, footerTop + 8, 0xFF5555);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class ConfigList extends ContainerObjectSelectionList<ConfigEntry> {
        ConfigList(net.minecraft.client.Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void addConfigEntry(ConfigEntry entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return TABLE_WIDTH;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width / 2 + TABLE_WIDTH / 2 + 4;
        }
    }

    private abstract class ConfigEntry extends ContainerObjectSelectionList.Entry<ConfigEntry> {
        protected final Component name;
        protected final Button resetButton;

        protected ConfigEntry(String name, Runnable resetAction) {
            this.name = Component.literal(name);
            this.resetButton = Button.builder(Component.literal("Reset"), button -> resetAction.run()).bounds(0, 0, RESET_WIDTH, 20).build();
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.widgets();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.narratableWidgets();
        }

        protected abstract List<? extends GuiEventListener> widgets();

        protected abstract List<? extends NarratableEntry> narratableWidgets();

        protected void renderName(GuiGraphics guiGraphics, int left, int top) {
            guiGraphics.drawString(ModernMinecartsConfigScreen.this.font, this.name, left, top + 6, 0xFFFFFF);
        }

        protected void positionResetButton(int left, int top) {
            int resetX = left + LABEL_WIDTH + COLUMN_GAP + INPUT_WIDTH + COLUMN_GAP;
            this.resetButton.setPosition(resetX, top + 2);
        }
    }

    private class NumberEntry extends ConfigEntry {
        private final EditBox field;
        private final double defaultValue;

        NumberEntry(String name, EditBox field, double defaultValue) {
            super(name, () -> field.setValue(String.valueOf(defaultValue)));
            this.field = field;
            this.defaultValue = defaultValue;
            this.field.setResponder(text -> {
                this.resetButton.active = !isDefaultNumberValue(text, this.defaultValue);
                ModernMinecartsConfigScreen.this.statusMessage = null;
            });
            this.resetButton.active = !isDefaultNumberValue(this.field.getValue(), this.defaultValue);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.resetButton.active = !isDefaultNumberValue(this.field.getValue(), this.defaultValue);
            this.field.setPosition(left + LABEL_WIDTH + COLUMN_GAP, top + 2);
            this.renderName(guiGraphics, left, top);
            this.field.render(guiGraphics, mouseX, mouseY, partialTick);
            this.positionResetButton(left, top);
            this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        protected List<? extends GuiEventListener> widgets() {
            return List.of(this.field, this.resetButton);
        }

        @Override
        protected List<? extends NarratableEntry> narratableWidgets() {
            return List.of(this.field, this.resetButton);
        }
    }

    private class IntegerEntry extends ConfigEntry {
        private final EditBox field;
        private final int defaultValue;

        IntegerEntry(String name, EditBox field, int defaultValue) {
            super(name, () -> field.setValue(String.valueOf(defaultValue)));
            this.field = field;
            this.defaultValue = defaultValue;
            this.field.setResponder(text -> {
                this.resetButton.active = !isDefaultIntegerValue(text, this.defaultValue);
                ModernMinecartsConfigScreen.this.statusMessage = null;
            });
            this.resetButton.active = !isDefaultIntegerValue(this.field.getValue(), this.defaultValue);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.resetButton.active = !isDefaultIntegerValue(this.field.getValue(), this.defaultValue);
            this.field.setPosition(left + LABEL_WIDTH + COLUMN_GAP, top + 2);
            this.renderName(guiGraphics, left, top);
            this.field.render(guiGraphics, mouseX, mouseY, partialTick);
            this.positionResetButton(left, top);
            this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        protected List<? extends GuiEventListener> widgets() {
            return List.of(this.field, this.resetButton);
        }

        @Override
        protected List<? extends NarratableEntry> narratableWidgets() {
            return List.of(this.field, this.resetButton);
        }
    }

    private class ToggleEntry extends ConfigEntry {
        private final ToggleButton toggleButton;
        private final boolean defaultValue;

        ToggleEntry(String name, ToggleButton toggleButton, boolean defaultValue) {
            super(name, () -> toggleButton.setValue(defaultValue));
            this.toggleButton = toggleButton;
            this.defaultValue = defaultValue;
            this.toggleButton.setOnToggle(nextValue -> {
                this.resetButton.active = nextValue != this.defaultValue;
                ModernMinecartsConfigScreen.this.statusMessage = null;
            });
            this.resetButton.active = this.toggleButton.getValue() != this.defaultValue;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.resetButton.active = this.toggleButton.getValue() != this.defaultValue;
            this.toggleButton.setPosition(left + LABEL_WIDTH + COLUMN_GAP, top + 2);
            this.renderName(guiGraphics, left, top);
            this.toggleButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.positionResetButton(left, top);
            this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        protected List<? extends GuiEventListener> widgets() {
            return List.of(this.toggleButton, this.resetButton);
        }

        @Override
        protected List<? extends NarratableEntry> narratableWidgets() {
            return List.of(this.toggleButton, this.resetButton);
        }
    }

    private static class ToggleButton extends Button {
        private boolean value;
        private Consumer<Boolean> onToggle;

        ToggleButton(int x, int y, int width, int height, boolean value, Consumer<Boolean> onToggle) {
            super(x, y, width, height, toggleMessage(value), button -> {}, DEFAULT_NARRATION);
            this.value = value;
            this.onToggle = onToggle;
        }

        @Override
        public void onPress() {
            this.setValue(!this.value);
            this.onToggle.accept(this.value);
        }

        public boolean getValue() {
            return this.value;
        }

        public void setValue(boolean value) {
            this.value = value;
            this.setMessage(toggleMessage(value));
        }

        public void setOnToggle(Consumer<Boolean> onToggle) {
            this.onToggle = onToggle;
        }
    }

    private class CategoryEntry extends ConfigEntry {
        CategoryEntry(String name) {
            super(name, () -> {});
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int centeredX = left + TABLE_WIDTH / 2 - ModernMinecartsConfigScreen.this.font.width(this.name) / 2;
            int textY = top + (ROW_HEIGHT - ModernMinecartsConfigScreen.this.font.lineHeight) / 2;
            guiGraphics.drawString(ModernMinecartsConfigScreen.this.font, this.name, centeredX, textY, 0xFFFFFF);
        }

        @Override
        protected List<? extends GuiEventListener> widgets() {
            return List.of();
        }

        @Override
        protected List<? extends NarratableEntry> narratableWidgets() {
            return List.of();
        }
    }
}
