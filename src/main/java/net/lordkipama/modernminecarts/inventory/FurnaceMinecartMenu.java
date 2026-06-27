package net.lordkipama.modernminecarts.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;

public class FurnaceMinecartMenu extends AbstractContainerMenu {
    private final Container container;
    private final net.minecraft.world.inventory.ContainerData data;
    private final FuelValues fuelValues;

    public FurnaceMinecartMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new net.minecraft.world.SimpleContainer(1), new net.minecraft.world.inventory.SimpleContainerData(3));
    }

    public FurnaceMinecartMenu(int containerId, Inventory playerInventory, Container container, net.minecraft.world.inventory.ContainerData data) {
        super(ModMenus.FURNACE_MINECART_MENU.get(), containerId);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        this.fuelValues = playerInventory.player.level().fuelValues();
        this.addSlot(new CustomFurnaceFuelSlot(this, container, 0, 80, 45));
        this.addStandardInventorySlots(playerInventory, 8, 84);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotIndex == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public boolean isFuel(ItemStack stack) {
        return this.fuelValues.isFuel(stack);
    }

    public int getSpeed() {
        return this.data.get(2);
    }

    public int getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) {
            i = 200;
        }

        return this.data.get(0) * 13 / i;
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }
}
