package net.lordkipama.modernminecarts.Item;

import net.lordkipama.modernminecarts.entity.CustomMinecartEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartTNTEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class CustomMinecartTNTItem extends AbstractMinecartItem {

    public CustomMinecartTNTItem(Properties builder) {
        super(builder);
    }

    @Override
    void createMinecart(ItemStack stack, Level world, double posX, double posY, double posZ) {

        CustomMinecartTNTEntity minecart = new CustomMinecartTNTEntity(world, posX, posY, posZ);
        if (stack.hasCustomHoverName()) {
            minecart.setCustomName(stack.getDisplayName());
        }
        world.addFreshEntity(minecart);
    }
}