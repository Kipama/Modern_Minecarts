package net.lordkipama.modernminecarts.inventory;

import net.lordkipama.modernminecarts.attachment.FurnaceMinecartData;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;

public class FurnaceMinecartContainer implements Container {
    private final MinecartFurnace minecart;

    public FurnaceMinecartContainer(MinecartFurnace minecart) {
        this.minecart = minecart;
    }

    public MinecartFurnace getMinecart() {
        return minecart;
    }

    private FurnaceMinecartData data() {
        return FurnaceMinecartHelper.getData(minecart);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return data().getFuelSlot().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? data().getFuelSlot() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }

        ItemStack current = data().getFuelSlot();
        ItemStack result = current.split(amount);
        if (current.isEmpty()) {
            data().setFuelSlot(ItemStack.EMPTY);
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) {
            return ItemStack.EMPTY;
        }

        ItemStack current = data().getFuelSlot();
        data().setFuelSlot(ItemStack.EMPTY);
        return current;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            data().setFuelSlot(stack);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return !minecart.isRemoved() && minecart.distanceToSqr(player) <= 64.0D;
    }

    @Override
    public void clearContent() {
        data().setFuelSlot(ItemStack.EMPTY);
    }
}
