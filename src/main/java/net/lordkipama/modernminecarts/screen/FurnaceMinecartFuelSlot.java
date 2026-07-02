package net.lordkipama.modernminecarts.screen;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class FurnaceMinecartFuelSlot extends Slot {
    private final int slotIndex;

    public FurnaceMinecartFuelSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.slotIndex = slot;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.container.canPlaceItem(this.slotIndex, stack) || stack.is(Items.BUCKET);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.is(Items.BUCKET) ? 1 : super.getMaxStackSize(stack);
    }
}
