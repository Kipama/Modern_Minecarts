package net.lordkipama.modernminecarts.interfaces;

import java.util.List;
import net.minecraft.world.Container;

public interface ContainerMinecartInteface {
    default void setNumberOfChildren(int number) {
    }

    default List<Container> getChainInventories() {
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
