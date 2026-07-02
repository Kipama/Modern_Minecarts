package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.util.TrainInventoryUtil;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartHopper.class)
public abstract class HopperMinecartMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void modernminecarts$linkTrainInventories(CallbackInfo ci) {
        MinecartHopper hopper = this.modernminecarts$self();
        if (hopper.level().isClientSide() || !hopper.isAlive() || !hopper.isEnabled()) {
            return;
        }

        if (AbstractContainerMenu.getRedstoneSignalFromContainer(hopper) >= 3) {
            modernminecarts$moveFullStackToStorage(hopper);
        } else {
            modernminecarts$getFullStackFromStorage(hopper);
        }
    }

    @Unique
    private static boolean modernminecarts$moveFullStackToStorage(MinecartHopper hopper) {
        for (int hopperSlot = 0; hopperSlot < 4; hopperSlot++) {
            ItemStack source = hopper.getItem(hopperSlot);
            if (source.isEmpty() || source.getCount() < source.getMaxStackSize()) {
                continue;
            }

            for (Container storage : TrainInventoryUtil.collectStorageInventories(hopper, true, true, false)) {
                for (int storageSlot = 0; storageSlot < storage.getContainerSize(); storageSlot++) {
                    if (storage.getItem(storageSlot).isEmpty() && storage.canPlaceItem(storageSlot, source)) {
                        storage.setItem(storageSlot, source.copy());
                        hopper.setItem(hopperSlot, ItemStack.EMPTY);
                        storage.setChanged();
                        hopper.setChanged();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Unique
    private static boolean modernminecarts$getFullStackFromStorage(MinecartHopper hopper) {
        if (!hopper.getItem(4).isEmpty()) {
            return false;
        }

        for (Container storage : TrainInventoryUtil.collectStorageInventories(hopper, true, true, false)) {
            for (int storageSlot = 0; storageSlot < storage.getContainerSize(); storageSlot++) {
                ItemStack source = storage.getItem(storageSlot);
                if (!source.isEmpty() && source.getCount() >= source.getMaxStackSize()) {
                    hopper.setItem(4, source.copy());
                    storage.setItem(storageSlot, ItemStack.EMPTY);
                    storage.setChanged();
                    hopper.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private MinecartHopper modernminecarts$self() {
        return (MinecartHopper) (Object) this;
    }
}
