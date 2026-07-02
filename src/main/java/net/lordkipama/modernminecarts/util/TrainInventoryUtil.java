package net.lordkipama.modernminecarts.util;

import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.logic.MinecartTuning;
import net.minecraft.world.Container;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class TrainInventoryUtil {
    private TrainInventoryUtil() {
    }

    public static List<Container> collectStorageInventories(
            AbstractMinecart origin,
            boolean includeParents,
            boolean includeChildren,
            boolean includeHoppers
    ) {
        List<Container> inventories = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        visited.add(origin.getUUID());

        if (includeParents) {
            collectDirection(getParent(origin), true, includeHoppers, inventories, visited);
        }
        if (includeChildren) {
            collectDirection(getChild(origin), false, includeHoppers, inventories, visited);
        }

        return inventories;
    }

    private static void collectDirection(
            @Nullable AbstractMinecart current,
            boolean towardParent,
            boolean includeHoppers,
            List<Container> inventories,
            Set<UUID> visited
    ) {
        List<Container> directionInventories = new ArrayList<>();
        int traversed = 0;
        while (current != null
                && traversed++ < MinecartTuning.MAX_TRAIN_LENGTH
                && visited.add(current.getUUID())) {
            if (current instanceof AbstractMinecartContainer inventory
                    && !(current instanceof MinecartFurnace)
                    && (includeHoppers || !(current instanceof MinecartHopper))) {
                directionInventories.add(inventory);
            }

            current = towardParent ? getParent(current) : getChild(current);
        }
        Collections.reverse(directionInventories);
        inventories.addAll(directionInventories);
    }

    private static @Nullable AbstractMinecart getParent(AbstractMinecart cart) {
        return ((ChainMinecartInterface) cart).getLinkedParent();
    }

    private static @Nullable AbstractMinecart getChild(AbstractMinecart cart) {
        return ((ChainMinecartInterface) cart).getLinkedChild();
    }
}
