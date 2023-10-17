package net.lordkipama.modernminecarts.Item;

import net.lordkipama.modernminecarts.entity.CustomMinecartEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartFurnaceEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CustomMinecartFurnaceItem extends AbstractMinecartItem {

    public CustomMinecartFurnaceItem(Properties builder) {
        super(builder);
    }

    @Override
    void createMinecart(ItemStack stack, Level world, double posX, double posY, double posZ) {

        CustomMinecartFurnaceEntity minecart = new CustomMinecartFurnaceEntity(world, posX, posY, posZ);
        if (stack.hasCustomHoverName()) {
            minecart.setCustomName(stack.getDisplayName());
        }
        world.addFreshEntity(minecart);
    }
}