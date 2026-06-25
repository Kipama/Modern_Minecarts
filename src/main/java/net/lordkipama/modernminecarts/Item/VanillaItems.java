package net.lordkipama.modernminecarts.Item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VanillaItems {
    public static final DeferredRegister.Items VANILLA_ITEMS = DeferredRegister.createItems("minecraft");

    public static final DeferredItem<? extends Item> MINECART_ITEM = VANILLA_ITEMS.register("minecart", () -> new CustomMinecartItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<? extends Item> CHEST_MINECART_ITEM = VANILLA_ITEMS.register("chest_minecart", () -> new CustomMinecartChestItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<? extends Item> COMMAND_BLOCK_MINECART_ITEM = VANILLA_ITEMS.register("command_block_minecart", () -> new CustomMinecartCommandBlockItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<? extends Item> FURNACE_MINECART_ITEM = VANILLA_ITEMS.register("furnace_minecart", () -> new CustomMinecartFurnaceItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<? extends Item> HOPPER_MINECART_ITEM = VANILLA_ITEMS.register("hopper_minecart", () -> new CustomMinecartHopperItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<? extends Item> SPAWNER_MINECART_ITEM = VANILLA_ITEMS.register("spawner_minecart", () -> new CustomMinecartSpawnerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<? extends Item> TNT_MINECART_ITEM = VANILLA_ITEMS.register("tnt_minecart", () -> new CustomMinecartTNTItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        VANILLA_ITEMS.register(eventBus);
    }
}
