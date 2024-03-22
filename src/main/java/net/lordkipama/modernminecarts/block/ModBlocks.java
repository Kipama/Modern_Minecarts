package net.lordkipama.modernminecarts.block;


import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.Custom.*;
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

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ModernMinecarts.MOD_ID);

    //NEW BLOCKS

    //Normal and Waxed normal
    public static final RegistryObject<Block> COPPER_RAIL = registerBlock("copper_rail",
            () -> new CopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WeatheringRailBlock.WeatherState.UNAFFECTED));
    public static final RegistryObject<Block> WAXED_COPPER_RAIL = registerBlock("waxed_copper_rail",
            () -> new WaxedCopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WaxedCopperRailBlock.WaxedWeatherState.WAXED_UNAFFECTED));

    //Exposed and Waxed Exposed
    public static final RegistryObject<Block> EXPOSED_COPPER_RAIL = registerBlock("exposed_copper_rail",
            () -> new CopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),WeatheringRailBlock.WeatherState.EXPOSED));
    public static final RegistryObject<Block> WAXED_EXPOSED_COPPER_RAIL = registerBlock("waxed_exposed_copper_rail",
            () -> new WaxedCopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WaxedCopperRailBlock.WaxedWeatherState.WAXED_EXPOSED));

    //Weathered and Waxed Weathered
    public static final RegistryObject<Block> WEATHERED_COPPER_RAIL = registerBlock("weathered_copper_rail",
            () -> new CopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),WeatheringRailBlock.WeatherState.WEATHERED));
    public static final RegistryObject<Block> WAXED_WEATHERED_COPPER_RAIL = registerBlock("waxed_weathered_copper_rail",
            () -> new WaxedCopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WaxedCopperRailBlock.WaxedWeatherState.WAXED_WEATHERED));

    //Oxidized and Waxed Oxidized
    public static final RegistryObject<Block> OXIDIZED_COPPER_RAIL = registerBlock("oxidized_copper_rail",
            () -> new CopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),WeatheringRailBlock.WeatherState.OXIDIZED));
    public static final RegistryObject<Block> WAXED_OXIDIZED_COPPER_RAIL = registerBlock("waxed_oxidized_copper_rail",
            () -> new WaxedCopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WaxedCopperRailBlock.WaxedWeatherState.WAXED_OXIDIZED));

    public static final RegistryObject<Block> RAIL_CROSSING = registerBlock("rail_crossing",
            () -> new RailCrossingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)));

    public static final RegistryObject<Block> SLOPED_RAIL = registerBlock("sloped_rail",
            () -> new SlopedRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)));

    public static final RegistryObject<Block> POWERED_DETECTOR_RAIL = registerBlock("powered_detector_rail",
            () -> new PoweredDetectorRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL)));


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
