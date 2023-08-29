package net.lordkipama.modernminecarts.block;


import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.Custom.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ModernMinecarts.MOD_ID);

    //NEW BLOCKS

    //Normal and Waxed normal
    public static final RegistryObject<Block> SWIFT_POWERED_RAIL = registerBlock("swift_powered_rail",
            () -> new SwiftPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL), WeatheringRailBlock.WeatherState.UNAFFECTED));
    public static final RegistryObject<Block> WAXED_SWIFT_POWERED_RAIL = registerBlock("waxed_swift_powered_rail",
            () -> new WaxedSwiftPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL)));

    //Exposed and Waxed Exposed
    public static final RegistryObject<Block> EXPOSED_SWIFT_POWERED_RAIL = registerBlock("exposed_swift_powered_rail",
            () -> new SwiftPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL),WeatheringRailBlock.WeatherState.EXPOSED));
    public static final RegistryObject<Block> WAXED_EXPOSED_SWIFT_POWERED_RAIL = registerBlock("waxed_exposed_swift_powered_rail",
            () -> new WaxedExposedSwiftPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL)));

    //Weathered and Waxed Weathered
    public static final RegistryObject<Block> WEATHERED_SWIFT_POWERED_RAIL = registerBlock("weathered_swift_powered_rail",
            () -> new SwiftPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL),WeatheringRailBlock.WeatherState.WEATHERED));
    public static final RegistryObject<Block> WAXED_WEATHERED_SWIFT_POWERED_RAIL = registerBlock("waxed_weathered_swift_powered_rail",
            () -> new WaxedWeatheredSwiftPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL)));

    //Oxidized and Waxed Oxidized
    public static final RegistryObject<Block> OXIDIZED_SWIFT_POWERED_RAIL = registerBlock("oxidized_swift_powered_rail",
            () -> new SwiftPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL),WeatheringRailBlock.WeatherState.OXIDIZED));
    public static final RegistryObject<Block> WAXED_OXIDIZED_SWIFT_POWERED_RAIL = registerBlock("waxed_oxidized_swift_powered_rail",
            () -> new WaxedOxidizedSwiftPoweredRailBlock(BlockBehaviour.Properties.copy(Blocks.POWERED_RAIL)));

    //END NEW BLOCKS

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static<T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }


    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
