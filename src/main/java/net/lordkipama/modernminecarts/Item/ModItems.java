package net.lordkipama.modernminecarts.Item;


import net.lordkipama.modernminecarts.ModernMinecarts;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;



public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ModernMinecarts.MOD_ID);


    public static final RegistryObject<Item> CUSTOM_MINECART_ITEM = ITEMS.register("custom_minecart", () -> new CustomMinecartItem(new Item.Properties().stacksTo(1)));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

