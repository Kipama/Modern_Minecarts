package net.lordkipama.modernminecarts.interfaces;

import net.minecraft.inventory.Inventory;

import java.util.List;

public interface ContainerMinecartInteface {
    default void setNumberOfChildren(int number) {}

    default List<Inventory> getChainInventories() {
        return List.of();
    }

    default int calculateActualSpeedForDisplay() {
        return 0;
    }

    default int burnFuelTrain() {
        return 0;
    }

    default double limitTrainSpeed(double railSpeed) {
        return railSpeed;
    }
}
