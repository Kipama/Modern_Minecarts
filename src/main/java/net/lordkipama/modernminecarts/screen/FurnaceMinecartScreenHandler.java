package net.lordkipama.modernminecarts.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.lordkipama.modernminecarts.logic.MinecartTuning;

public class FurnaceMinecartScreenHandler extends ScreenHandler {
    private static final int PROPERTY_COUNT = 3;
    private final Inventory inventory;
    private final PropertyDelegate properties;

    public FurnaceMinecartScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(1), new ArrayPropertyDelegate(PROPERTY_COUNT));
    }

    public FurnaceMinecartScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory inventory,
            PropertyDelegate properties
    ) {
        super(ModScreenHandlers.FURNACE_MINECART, syncId);
        checkSize(inventory, 1);
        checkDataCount(properties, PROPERTY_COUNT);
        this.inventory = inventory;
        this.properties = properties;

        inventory.onOpen(playerInventory.player);
        addSlot(new FurnaceMinecartFuelSlot(inventory, 0, 80, 45));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }

        addProperties(properties);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);

        if (slot.hasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (slotIndex == 0) {
                if (!insertItem(stack, 1, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (player.getEntityWorld().getFuelRegistry().isFuel(stack)) {
                if (!insertItem(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotIndex < 28) {
                if (!insertItem(stack, 28, 37, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(stack, 1, 28, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return result;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    public boolean isBurning() {
        return properties.get(0) > 0;
    }

    public int getBurnProgress() {
        int total = properties.get(1);
        if (total <= 0) {
            total = 200;
        }
        return properties.get(0) * 13 / total;
    }

    public int getSpeedProgress() {
        return Math.max(0, Math.min(MinecartTuning.SPEEDOMETER_HEIGHT, properties.get(2)));
    }
}
