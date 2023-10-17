package net.lordkipama.modernminecarts.Item;

import net.lordkipama.modernminecarts.entity.CustomMinecartChestEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CustomMinecartChestItem extends AbstractMinecartItem {

    public CustomMinecartChestItem(Properties builder) {
        super(builder);
    }

    @Override
    void createMinecart(ItemStack stack, Level world, double posX, double posY, double posZ) {

        CustomMinecartChestEntity minecart = new CustomMinecartChestEntity(world, posX, posY, posZ);
        minecart.setCustomName(Component.translatable("entity.minecraft.chest_minecart"));
        world.addFreshEntity(minecart);
    }
}