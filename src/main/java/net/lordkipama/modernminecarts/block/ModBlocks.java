package net.lordkipama.modernminecarts.block;

import java.util.function.Supplier;
import net.lordkipama.modernminecarts.Item.FeatureToggleBlockItem;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.block.Custom.RailCrossingBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WeatheringRailBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModernMinecarts.MOD_ID);

    public static final DeferredBlock<? extends Block> COPPER_RAIL = registerBlock("copper_rail",
            () -> new CopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WeatheringRailBlock.WeatherState.UNAFFECTED));
    public static final DeferredBlock<? extends Block> WAXED_COPPER_RAIL = registerBlock("waxed_copper_rail",
            () -> new WaxedCopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WaxedCopperRailBlock.WaxedWeatherState.WAXED_UNAFFECTED));
    public static final DeferredBlock<? extends Block> EXPOSED_COPPER_RAIL = registerBlock("exposed_copper_rail",
            () -> new CopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WeatheringRailBlock.WeatherState.EXPOSED));
    public static final DeferredBlock<? extends Block> WAXED_EXPOSED_COPPER_RAIL = registerBlock("waxed_exposed_copper_rail",
            () -> new WaxedCopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WaxedCopperRailBlock.WaxedWeatherState.WAXED_EXPOSED));
    public static final DeferredBlock<? extends Block> WEATHERED_COPPER_RAIL = registerBlock("weathered_copper_rail",
            () -> new CopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WeatheringRailBlock.WeatherState.WEATHERED));
    public static final DeferredBlock<? extends Block> WAXED_WEATHERED_COPPER_RAIL = registerBlock("waxed_weathered_copper_rail",
            () -> new WaxedCopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WaxedCopperRailBlock.WaxedWeatherState.WAXED_WEATHERED));
    public static final DeferredBlock<? extends Block> OXIDIZED_COPPER_RAIL = registerBlock("oxidized_copper_rail",
            () -> new CopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WeatheringRailBlock.WeatherState.OXIDIZED));
    public static final DeferredBlock<? extends Block> WAXED_OXIDIZED_COPPER_RAIL = registerBlock("waxed_oxidized_copper_rail",
            () -> new WaxedCopperRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL), WaxedCopperRailBlock.WaxedWeatherState.WAXED_OXIDIZED));
    public static final DeferredBlock<? extends Block> RAIL_CROSSING = registerBlock("rail_crossing",
            () -> new RailCrossingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)));
    public static final DeferredBlock<? extends Block> SLOPED_RAIL = registerBlock("sloped_rail",
            () -> new SlopedRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)));
    public static final DeferredBlock<? extends Block> POWERED_DETECTOR_RAIL = registerBlock("powered_detector_rail",
            () -> new PoweredDetectorRailBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL)));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> registered = BLOCKS.register(name, block);
        registerBlockItem(name, registered);
        return registered;
    }

    private static <T extends Block> DeferredItem<? extends Item> registerBlockItem(String name, DeferredBlock<T> block) {
        if (name.contains("copper_rail")) {
            return ModItems.ITEMS.register(name, () -> new FeatureToggleBlockItem(block.get(), new Item.Properties(), ModernMinecartsConfig::enableCopperRails));
        }
        if ("rail_crossing".equals(name)) {
            return ModItems.ITEMS.register(name, () -> new FeatureToggleBlockItem(block.get(), new Item.Properties(), ModernMinecartsConfig::enableRailCrossing));
        }
        if ("sloped_rail".equals(name)) {
            return ModItems.ITEMS.register(name, () -> new FeatureToggleBlockItem(block.get(), new Item.Properties(), ModernMinecartsConfig::enableRailJump));
        }
        if ("powered_detector_rail".equals(name)) {
            return ModItems.ITEMS.register(name, () -> new FeatureToggleBlockItem(block.get(), new Item.Properties(), ModernMinecartsConfig::enablePoweredDetectorRail));
        }

        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
