package net.lordkipama.modernminecarts.Item;


import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class VanillaItems {
    public static final DeferredRegister<Item> VANILLA_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");


    public static final RegistryObject<Item> MINECART_ITEM = VANILLA_ITEMS.register("minecart", () -> new CustomMinecartItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> CHEST_MINECART_ITEM = VANILLA_ITEMS.register("chest_minecart", () -> new CustomMinecartChestItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> COMMAND_BLOCK_MINECART_ITEM = VANILLA_ITEMS.register("command_block_minecart", () -> new CustomMinecartCommandBlockItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> FURNACE_MINECART_ITEM = VANILLA_ITEMS.register("furnace_minecart", () -> new CustomMinecartFurnaceItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> HOPPER_MINECART_ITEM = VANILLA_ITEMS.register("hopper_minecart", () -> new CustomMinecartHopperItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SPAWNER_MINECART_ITEM = VANILLA_ITEMS.register("spawner_minecart", () -> new CustomMinecartSpawnerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> TNT_MINECART_ITEM = VANILLA_ITEMS.register("tnt_minecart", () -> new CustomMinecartTNTItem(new Item.Properties().stacksTo(1)));


    public static void register(IEventBus eventBus) {
        VANILLA_ITEMS.register(eventBus);
    }
}

