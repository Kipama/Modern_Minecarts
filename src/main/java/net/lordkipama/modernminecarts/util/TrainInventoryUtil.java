package net.lordkipama.modernminecarts.util;

import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class TrainInventoryUtil {
    private static final int MAX_TRAIN_LENGTH = 128;

    private TrainInventoryUtil() {
    }

    public static List<Inventory> collectStorageInventories(
            AbstractMinecartEntity origin,
            boolean includeParents,
            boolean includeChildren,
            boolean includeHoppers
    ) {
        List<Inventory> inventories = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        visited.add(origin.getUuid());

        if (includeParents) {
            collectDirection(getParent(origin), true, includeHoppers, inventories, visited);
        }
        if (includeChildren) {
            collectDirection(getChild(origin), false, includeHoppers, inventories, visited);
        }

        return inventories;
    }

    private static void collectDirection(
            @Nullable AbstractMinecartEntity current,
            boolean towardParent,
            boolean includeHoppers,
            List<Inventory> inventories,
            Set<UUID> visited
    ) {
        List<Inventory> directionInventories = new ArrayList<>();
        int traversed = 0;
        while (current != null
                && traversed++ < MAX_TRAIN_LENGTH
                && visited.add(current.getUuid())) {
            if (current instanceof VehicleInventory inventory
                    && !(current instanceof FurnaceMinecartEntity)
                    && (includeHoppers || !(current instanceof HopperMinecartEntity))) {
                directionInventories.add(inventory);
            }

            current = towardParent ? getParent(current) : getChild(current);
        }
        Collections.reverse(directionInventories);
        inventories.addAll(directionInventories);
    }

    private static @Nullable AbstractMinecartEntity getParent(AbstractMinecartEntity cart) {
        return ((ChainMinecartInterface) cart).getLinkedParent();
    }

    private static @Nullable AbstractMinecartEntity getChild(AbstractMinecartEntity cart) {
        return ((ChainMinecartInterface) cart).getLinkedChild();
    }
}
