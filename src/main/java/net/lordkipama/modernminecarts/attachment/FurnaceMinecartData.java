package net.lordkipama.modernminecarts.attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class FurnaceMinecartData implements INBTSerializable<CompoundTag> {
    private static final String FUEL_SLOT_TAG = "FuelSlot";
    private static final String FUEL_BURN_TIME_TAG = "FuelBurnTime";

    private ItemStack fuelSlot = ItemStack.EMPTY;
    private int fuelBurnTime;

    public ItemStack getFuelSlot() {
        return fuelSlot;
    }

    public void setFuelSlot(ItemStack fuelSlot) {
        this.fuelSlot = fuelSlot;
    }

    public int getFuelBurnTime() {
        return fuelBurnTime;
    }

    public void setFuelBurnTime(int fuelBurnTime) {
        this.fuelBurnTime = fuelBurnTime;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (!fuelSlot.isEmpty()) {
            tag.put(FUEL_SLOT_TAG, fuelSlot.saveOptional(provider));
        }
        tag.putInt(FUEL_BURN_TIME_TAG, fuelBurnTime);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        fuelSlot = tag.contains(FUEL_SLOT_TAG) ? ItemStack.parseOptional(provider, tag.getCompound(FUEL_SLOT_TAG)) : ItemStack.EMPTY;
        fuelBurnTime = tag.getInt(FUEL_BURN_TIME_TAG);
    }
}
