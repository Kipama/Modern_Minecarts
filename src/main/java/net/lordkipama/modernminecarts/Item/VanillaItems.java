package net.lordkipama.modernminecarts.Item;


import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class VanillaItems {
    public static final DeferredRegister<Item> VANILLA_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");


    public static final RegistryObject<Item> MINECART_ITEM = VANILLA_ITEMS.register("minecart", () -> new CustomMinecartItem(new Item.Properties().stacksTo(1)));



    public static void register(IEventBus eventBus) {
        VANILLA_ITEMS.register(eventBus);
    }
}

