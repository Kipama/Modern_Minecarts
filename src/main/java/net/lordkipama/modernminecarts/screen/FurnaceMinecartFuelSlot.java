package net.lordkipama.modernminecarts.screen;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class FurnaceMinecartFuelSlot extends Slot {
    public FurnaceMinecartFuelSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return AbstractFurnaceBlockEntity.canUseAsFuel(stack) || stack.isOf(Items.BUCKET);
    }

    @Override
    public int getMaxItemCount(ItemStack stack) {
        return stack.isOf(Items.BUCKET) ? 1 : super.getMaxItemCount(stack);
    }
}
