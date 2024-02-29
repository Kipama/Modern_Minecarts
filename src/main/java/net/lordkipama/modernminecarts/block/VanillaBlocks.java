package net.lordkipama.modernminecarts.block;

import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.block.Custom.CustomRailBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class VanillaBlocks {
    public static final DeferredRegister<Block> VANILLA_BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "minecraft");

    public static final RegistryObject<Block> VANILLA_RAIL = registerBlock("rail",
            () -> new CustomRailBlock(BlockBehaviour.Properties.copy(Blocks.RAIL)));


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = VANILLA_BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static<T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return VanillaItems.VANILLA_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        VANILLA_BLOCKS.register(eventBus);
    }
}
