package net.lordkipama.modernminecarts.util;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class HopperMinecartHelper {
    private HopperMinecartHelper() {
    }

    public static void tick(MinecartHopper hopperMinecart) {
        if (hopperMinecart.level().isClientSide()) {
            return;
        }

        moveFullStacksToStorage(hopperMinecart);
        pullReserveStackFromStorage(hopperMinecart);
    }

    private static void moveFullStacksToStorage(MinecartHopper hopperMinecart) {
        List<ContainerEntity> linkedContainers = getLinkedChestContainers(hopperMinecart);
        if (linkedContainers.isEmpty()) {
            return;
        }

        for (int slot = 0; slot < 4; slot++) {
            ItemStack sourceStack = hopperMinecart.getItem(slot);
            if (sourceStack.isEmpty() || sourceStack.getCount() != sourceStack.getMaxStackSize()) {
                continue;
            }

            for (ContainerEntity container : linkedContainers) {
                for (int targetSlot = 0; targetSlot < container.getContainerSize(); targetSlot++) {
                    if (!container.canPlaceItem(targetSlot, sourceStack) || !container.getItem(targetSlot).isEmpty()) {
                        continue;
                    }

                    container.setItem(targetSlot, sourceStack.copy());
                    hopperMinecart.setItem(slot, ItemStack.EMPTY);
                    container.setChanged();
                    return;
                }
            }
        }
    }

    private static void pullReserveStackFromStorage(MinecartHopper hopperMinecart) {
        if (!hopperMinecart.getItem(4).isEmpty()) {
            return;
        }

        for (ContainerEntity container : getLinkedChestContainers(hopperMinecart)) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack sourceStack = container.getItem(slot);
                if (sourceStack.isEmpty() || sourceStack.getCount() != sourceStack.getMaxStackSize()) {
                    continue;
                }

                hopperMinecart.setItem(4, sourceStack.copy());
                container.setItem(slot, ItemStack.EMPTY);
                container.setChanged();
                return;
            }
        }
    }

    private static List<ContainerEntity> getLinkedChestContainers(AbstractMinecart minecart) {
        List<ContainerEntity> containers = new ArrayList<>();
        AbstractMinecart head = minecart;
        while (MinecartLinkHelper.getLinkedParent(head) != null) {
            head = MinecartLinkHelper.getLinkedParent(head);
        }

        AbstractMinecart current = head;
        while (current != null) {
            if (current instanceof MinecartChest chestMinecart) {
                containers.add(chestMinecart);
            }
            current = MinecartLinkHelper.getLinkedChild(current);
        }

        return containers;
    }
}
