package net.lordkipama.modernminecarts.Item;

import net.lordkipama.modernminecarts.entity.CustomMinecartEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartSpawnerEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CustomMinecartSpawnerItem extends AbstractMinecartItem {

    public CustomMinecartSpawnerItem(Properties builder) {
        super(builder);
    }

    @Override
    void createMinecart(ItemStack stack, Level world, double posX, double posY, double posZ) {

        CustomMinecartSpawnerEntity minecart = new CustomMinecartSpawnerEntity(world, posX, posY, posZ);
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            minecart.setCustomName(stack.getHoverName());
        }
        world.addFreshEntity(minecart);
    }
}