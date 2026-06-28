package net.lordkipama.modernminecarts.inventory;

import net.lordkipama.modernminecarts.attachment.FurnaceMinecartData;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.inventory.ContainerData;

public class FurnaceMinecartDataAccess implements ContainerData {
    private final MinecartFurnace minecart;

    public FurnaceMinecartDataAccess(MinecartFurnace minecart) {
        this.minecart = minecart;
    }

    @Override
    public int get(int index) {
        FurnaceMinecartData data = FurnaceMinecartHelper.getData(minecart);
        return switch (index) {
            case 0 -> FurnaceMinecartHelper.getFuel(minecart);
            case 1 -> data.getFuelBurnTime();
            case 2 -> FurnaceMinecartHelper.calculateActualSpeedForDisplay(minecart);
            default -> 0;
        };
    }

    @Override
    public void set(int index, int value) {
        FurnaceMinecartData data = FurnaceMinecartHelper.getData(minecart);
        switch (index) {
            case 0 -> FurnaceMinecartHelper.setFuel(minecart, value);
            case 1 -> data.setFuelBurnTime(value);
            default -> {
            }
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
