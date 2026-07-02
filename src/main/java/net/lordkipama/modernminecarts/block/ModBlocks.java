package net.lordkipama.modernminecarts.block;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.block.Custom.RailCrossingBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WeatheringRailBlock;
import net.lordkipama.modernminecarts.item.FeatureToggleBlockItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class ModBlocks {
    public static final Block COPPER_RAIL = registerBlock(
            "copper_rail",
            properties -> new CopperRailBlock(properties, WeatheringRailBlock.WeatherState.UNAFFECTED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),
            ModernMinecartsConfig::enableCopperRails
    );
    public static final Block WAXED_COPPER_RAIL = registerBlock(
            "waxed_copper_rail",
            properties -> new WaxedCopperRailBlock(properties, WaxedCopperRailBlock.WaxedWeatherState.WAXED_UNAFFECTED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),
            ModernMinecartsConfig::enableCopperRails
    );
    public static final Block EXPOSED_COPPER_RAIL = registerBlock(
            "exposed_copper_rail",
            properties -> new CopperRailBlock(properties, WeatheringRailBlock.WeatherState.EXPOSED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),
            ModernMinecartsConfig::enableCopperRails
    );
    public static final Block WAXED_EXPOSED_COPPER_RAIL = registerBlock(
            "waxed_exposed_copper_rail",
            properties -> new WaxedCopperRailBlock(properties, WaxedCopperRailBlock.WaxedWeatherState.WAXED_EXPOSED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),
            ModernMinecartsConfig::enableCopperRails
    );
    public static final Block WEATHERED_COPPER_RAIL = registerBlock(
            "weathered_copper_rail",
            properties -> new CopperRailBlock(properties, WeatheringRailBlock.WeatherState.WEATHERED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),
            ModernMinecartsConfig::enableCopperRails
    );
    public static final Block WAXED_WEATHERED_COPPER_RAIL = registerBlock(
            "waxed_weathered_copper_rail",
            properties -> new WaxedCopperRailBlock(properties, WaxedCopperRailBlock.WaxedWeatherState.WAXED_WEATHERED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),
            ModernMinecartsConfig::enableCopperRails
    );
    public static final Block OXIDIZED_COPPER_RAIL = registerBlock(
            "oxidized_copper_rail",
            properties -> new CopperRailBlock(properties, WeatheringRailBlock.WeatherState.OXIDIZED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),
            ModernMinecartsConfig::enableCopperRails
    );
    public static final Block WAXED_OXIDIZED_COPPER_RAIL = registerBlock(
            "waxed_oxidized_copper_rail",
            properties -> new WaxedCopperRailBlock(properties, WaxedCopperRailBlock.WaxedWeatherState.WAXED_OXIDIZED),
            BlockBehaviour.Properties.ofFullCopy(Blocks.POWERED_RAIL),
            ModernMinecartsConfig::enableCopperRails
    );
    public static final Block RAIL_CROSSING = registerBlock(
            "rail_crossing",
            RailCrossingBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL),
            ModernMinecartsConfig::enableRailCrossing
    );
    public static final Block RAIL_JUMP = registerBlock(
            "rail_jump",
            SlopedRailBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.RAIL),
            ModernMinecartsConfig::enableRailJump
    );
    public static final Block POWERED_DETECTOR_RAIL = registerBlock(
            "powered_detector_rail",
            PoweredDetectorRailBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.DETECTOR_RAIL),
            ModernMinecartsConfig::enablePoweredDetectorRail
    );

    private static <T extends Block> T registerBlock(
            String name,
            Function<BlockBehaviour.Properties, ? extends T> factory,
            BlockBehaviour.Properties properties,
            BooleanSupplier enabledSupplier
    ) {
        Identifier id = ModernMinecarts.id(name);
        T block = factory.apply(properties.setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), id)));
        Registry.register(BuiltInRegistries.BLOCK, id, block);
        registerBlockItem(name, block, enabledSupplier);
        return block;
    }

    private static Item registerBlockItem(String name, Block block, BooleanSupplier enabledSupplier) {
        Identifier id = ModernMinecarts.id(name);
        Item.Properties properties = new Item.Properties()
                .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), id))
                .useBlockDescriptionPrefix();
        return Registry.register(
                BuiltInRegistries.ITEM,
                id,
                new FeatureToggleBlockItem(block, properties, enabledSupplier)
        );
    }

    public static void registerModBlocks() {
        ModernMinecarts.LOGGER.info("Registering ModBlocks for {}", ModernMinecarts.MOD_ID);
    }
}
