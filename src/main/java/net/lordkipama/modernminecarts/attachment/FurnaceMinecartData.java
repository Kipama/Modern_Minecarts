package net.lordkipama.modernminecarts.attachment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class FurnaceMinecartData implements ValueIOSerializable {
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
    public void serialize(ValueOutput output) {
        output.store(FUEL_SLOT_TAG, ItemStack.OPTIONAL_CODEC, fuelSlot);
        output.putInt(FUEL_BURN_TIME_TAG, fuelBurnTime);
    }

    @Override
    public void deserialize(ValueInput input) {
        fuelSlot = input.read(FUEL_SLOT_TAG, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        fuelBurnTime = input.getIntOr(FUEL_BURN_TIME_TAG, 0);
    }
}
