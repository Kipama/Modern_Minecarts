package net.lordkipama.modernminecarts.screen;

import net.lordkipama.modernminecarts.logic.MinecartTuning;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FurnaceMinecartScreenHandler extends AbstractContainerMenu {
    private static final int PROPERTY_COUNT = 3;
    private final Container inventory;
    private final ContainerData properties;

    public FurnaceMinecartScreenHandler(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(1), new SimpleContainerData(PROPERTY_COUNT));
    }

    public FurnaceMinecartScreenHandler(int containerId, Inventory playerInventory, Container inventory, ContainerData properties) {
        super(ModScreenHandlers.FURNACE_MINECART, containerId);
        checkContainerSize(inventory, 1);
        checkContainerDataCount(properties, PROPERTY_COUNT);
        this.inventory = inventory;
        this.properties = properties;

        inventory.startOpen(playerInventory.player);
        this.addSlot(new FurnaceMinecartFuelSlot(inventory, 0, 80, 45));

        this.addInventorySlots(playerInventory);
        this.addDataSlots(properties);
    }

    private void addInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (slotIndex == 0) {
                if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (player.level().fuelValues().isFuel(stack)) {
                if (!this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < 28) {
                if (!this.moveItemStackTo(stack, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 1, 28, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }

    public boolean isBurning() {
        return this.properties.get(0) > 0;
    }

    public int getBurnProgress() {
        int total = this.properties.get(1);
        if (total <= 0) {
            total = 200;
        }
        return this.properties.get(0) * 13 / total;
    }

    public int getSpeedProgress() {
        return Math.max(0, Math.min(MinecartTuning.SPEEDOMETER_HEIGHT, this.properties.get(2)));
    }
}
