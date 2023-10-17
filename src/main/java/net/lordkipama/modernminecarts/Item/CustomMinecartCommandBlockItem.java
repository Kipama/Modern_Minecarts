package net.lordkipama.modernminecarts.Item;

import net.lordkipama.modernminecarts.entity.CustomMinecartCommandBlockEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CustomMinecartCommandBlockItem extends AbstractMinecartItem {

    public CustomMinecartCommandBlockItem(Properties builder) {
        super(builder);
    }

    @Override
    void createMinecart(ItemStack stack, Level world, double posX, double posY, double posZ) {

        CustomMinecartCommandBlockEntity minecart = new CustomMinecartCommandBlockEntity(world, posX, posY, posZ);
        if (stack.hasCustomHoverName()) {
            minecart.setCustomName(stack.getDisplayName());
        }
        world.addFreshEntity(minecart);
    }
}