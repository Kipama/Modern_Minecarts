package net.lordkipama.modernminecarts.block;

import java.util.function.Supplier;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.block.Custom.CustomRailBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VanillaBlocks {
    public static final DeferredRegister.Blocks VANILLA_BLOCKS = DeferredRegister.createBlocks("minecraft");

    public static final DeferredBlock<? extends Block> VANILLA_RAIL = registerBlock("rail",
            () -> new CustomRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registered = VANILLA_BLOCKS.register(name, block);
        registerBlockItem(name, registered);
        return registered;
    }

    private static <T extends Block> DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<T> block) {
        return VanillaItems.VANILLA_ITEMS.register(name, () -> new BlockItem(block.get(), new net.minecraft.world.item.Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        VANILLA_BLOCKS.register(eventBus);
    }
}
