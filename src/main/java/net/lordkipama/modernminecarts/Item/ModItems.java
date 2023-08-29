package net.lordkipama.modernminecarts.Item;

import com.google.common.util.concurrent.ClosingFuture;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ModernMinecarts.MOD_ID);




    public static final RegistryObject<Item> TESTITEM = ITEMS.register("test_item",
            () -> new Item(new Item.Properties()));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

