package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.util.TrainInventoryUtil;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HopperMinecartEntity.class)
public abstract class HopperMinecartMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void modernminecarts$linkTrainInventories(CallbackInfo ci) {
        HopperMinecartEntity hopper = modernminecarts$self();
        if (hopper.getWorld().isClient()
                || !hopper.isAlive()
                || !hopper.isEnabled()) {
            return;
        }

        if (ScreenHandler.calculateComparatorOutput(hopper) >= 3) {
            modernminecarts$moveFullStackToStorage(hopper);
        } else {
            modernminecarts$getFullStackFromStorage(hopper);
        }
    }

    @Unique
    private static boolean modernminecarts$moveFullStackToStorage(HopperMinecartEntity hopper) {
        for (int hopperSlot = 0; hopperSlot < 4; hopperSlot++) {
            ItemStack source = hopper.getStack(hopperSlot);
            if (source.isEmpty() || source.getCount() < source.getMaxCount()) {
                continue;
            }

            for (Inventory storage : TrainInventoryUtil.collectStorageInventories(
                    hopper,
                    true,
                    true,
                    false
            )) {
                for (int storageSlot = 0; storageSlot < storage.size(); storageSlot++) {
                    if (storage.getStack(storageSlot).isEmpty()
                            && storage.isValid(storageSlot, source)) {
                        storage.setStack(storageSlot, source.copy());
                        hopper.setStack(hopperSlot, ItemStack.EMPTY);
                        storage.markDirty();
                        hopper.markDirty();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Unique
    private static boolean modernminecarts$getFullStackFromStorage(HopperMinecartEntity hopper) {
        if (!hopper.getStack(4).isEmpty()) {
            return false;
        }

        for (Inventory storage : TrainInventoryUtil.collectStorageInventories(
                hopper,
                true,
                true,
                false
        )) {
            for (int storageSlot = 0; storageSlot < storage.size(); storageSlot++) {
                ItemStack source = storage.getStack(storageSlot);
                if (!source.isEmpty() && source.getCount() >= source.getMaxCount()) {
                    hopper.setStack(4, source.copy());
                    storage.setStack(storageSlot, ItemStack.EMPTY);
                    storage.markDirty();
                    hopper.markDirty();
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    private HopperMinecartEntity modernminecarts$self() {
        return (HopperMinecartEntity) (Object) this;
    }
}
