package net.lordkipama.modernminecarts.Item;

import net.lordkipama.modernminecarts.entity.CustomMinecartFurnaceEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartHopperEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CustomMinecartHopperItem extends AbstractMinecartItem {

    public CustomMinecartHopperItem(Properties builder) {
        super(builder);
    }

    @Override
    void createMinecart(ItemStack stack, Level world, double posX, double posY, double posZ) {

        CustomMinecartHopperEntity minecart = new CustomMinecartHopperEntity(world, posX, posY, posZ);
        minecart.setCustomName(Component.translatable("entity.minecraft.hopper_minecart"));
        world.addFreshEntity(minecart);
    }
}