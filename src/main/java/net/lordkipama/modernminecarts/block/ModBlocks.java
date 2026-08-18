package net.lordkipama.modernminecarts.block;

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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ModernMinecarts.MOD_ID);

    public static final DeferredBlock<CopperRailBlock> COPPER_RAIL = registerBlock(
            "copper_rail",
            properties -> new CopperRailBlock(properties, WeatheringRailBlock.WeatherState.UNAFFECTED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)
    );
    public static final DeferredBlock<WaxedCopperRailBlock> WAXED_COPPER_RAIL = registerBlock(
            "waxed_copper_rail",
            properties -> new WaxedCopperRailBlock(properties, WaxedCopperRailBlock.WaxedWeatherState.WAXED_UNAFFECTED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)
    );
    public static final DeferredBlock<CopperRailBlock> EXPOSED_COPPER_RAIL = registerBlock(
            "exposed_copper_rail",
            properties -> new CopperRailBlock(properties, WeatheringRailBlock.WeatherState.EXPOSED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)
    );
    public static final DeferredBlock<WaxedCopperRailBlock> WAXED_EXPOSED_COPPER_RAIL = registerBlock(
            "waxed_exposed_copper_rail",
            properties -> new WaxedCopperRailBlock(properties, WaxedCopperRailBlock.WaxedWeatherState.WAXED_EXPOSED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)
    );
    public static final DeferredBlock<CopperRailBlock> WEATHERED_COPPER_RAIL = registerBlock(
            "weathered_copper_rail",
            properties -> new CopperRailBlock(properties, WeatheringRailBlock.WeatherState.WEATHERED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)
    );
    public static final DeferredBlock<WaxedCopperRailBlock> WAXED_WEATHERED_COPPER_RAIL = registerBlock(
            "waxed_weathered_copper_rail",
            properties -> new WaxedCopperRailBlock(properties, WaxedCopperRailBlock.WaxedWeatherState.WAXED_WEATHERED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)
    );
    public static final DeferredBlock<CopperRailBlock> OXIDIZED_COPPER_RAIL = registerBlock(
            "oxidized_copper_rail",
            properties -> new CopperRailBlock(properties, WeatheringRailBlock.WeatherState.OXIDIZED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)
    );
    public static final DeferredBlock<WaxedCopperRailBlock> WAXED_OXIDIZED_COPPER_RAIL = registerBlock(
            "waxed_oxidized_copper_rail",
            properties -> new WaxedCopperRailBlock(properties, WaxedCopperRailBlock.WaxedWeatherState.WAXED_OXIDIZED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL)
    );
    public static final DeferredBlock<RailCrossingBlock> RAIL_CROSSING = registerBlock(
            "rail_crossing",
            RailCrossingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)
    );
    public static final DeferredBlock<SlopedRailBlock> SLOPED_RAIL = registerBlock(
            "sloped_rail",
            SlopedRailBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL)
    );
    public static final DeferredBlock<PoweredDetectorRailBlock> POWERED_DETECTOR_RAIL = registerBlock(
            "powered_detector_rail",
            PoweredDetectorRailBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL)
    );

    private static <T extends Block> DeferredBlock<T> registerBlock(
            String name,
            java.util.function.Function<BlockBehaviour.Properties, ? extends T> factory,
            BlockBehaviour.Properties properties
    ) {
        DeferredBlock<T> registered = BLOCKS.registerBlock(name, factory, () -> properties);
        registerBlockItem(name, registered);
        return registered;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        Item.Properties properties = new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ModernMinecarts.MOD_ID, name)))
                .useBlockDescriptionPrefix();

        if (name.contains("copper_rail")) {
            ModItems.ITEMS.register(name, () -> new FeatureToggleBlockItem(block.get(), properties, ModernMinecartsConfig::enableCopperRails));
            return;
        }
        if ("rail_crossing".equals(name)) {
            ModItems.ITEMS.register(name, () -> new FeatureToggleBlockItem(block.get(), properties, ModernMinecartsConfig::enableRailCrossing));
            return;
        }
        if ("sloped_rail".equals(name)) {
            ModItems.ITEMS.register(name, () -> new FeatureToggleBlockItem(block.get(), properties, ModernMinecartsConfig::enableRailJump));
            return;
        }
        if ("powered_detector_rail".equals(name)) {
            ModItems.ITEMS.register(name, () -> new FeatureToggleBlockItem(block.get(), properties, ModernMinecartsConfig::enablePoweredDetectorRail));
            return;
        }
        ModItems.ITEMS.registerSimpleBlockItem(block);
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
