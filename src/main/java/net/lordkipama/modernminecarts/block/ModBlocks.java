package net.lordkipama.modernminecarts.block;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.RailCrossingBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.item.FeatureToggleBlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.BooleanSupplier;

public class ModBlocks {

    public static final Block COPPER_RAIL = registerBlock("copper_rail",
            new CopperRailBlock(Oxidizable.OxidationLevel.UNAFFECTED, AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)),
            ModernMinecartsConfig::enableCopperRails);
    public static final Block WAXED_COPPER_RAIL = registerBlock("waxed_copper_rail",
            new WaxedCopperRailBlock(Oxidizable.OxidationLevel.UNAFFECTED, AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)),
            ModernMinecartsConfig::enableCopperRails);

    public static final Block EXPOSED_COPPER_RAIL = registerBlock("exposed_copper_rail",
            new CopperRailBlock(Oxidizable.OxidationLevel.EXPOSED, AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)),
            ModernMinecartsConfig::enableCopperRails);
    public static final Block WAXED_EXPOSED_COPPER_RAIL = registerBlock("waxed_exposed_copper_rail",
            new WaxedCopperRailBlock(Oxidizable.OxidationLevel.EXPOSED, AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)),
            ModernMinecartsConfig::enableCopperRails);

    public static final Block WEATHERED_COPPER_RAIL = registerBlock("weathered_copper_rail",
            new CopperRailBlock(Oxidizable.OxidationLevel.WEATHERED, AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)),
            ModernMinecartsConfig::enableCopperRails);
    public static final Block WAXED_WEATHERED_COPPER_RAIL = registerBlock("waxed_weathered_copper_rail",
            new WaxedCopperRailBlock(Oxidizable.OxidationLevel.WEATHERED, AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)),
            ModernMinecartsConfig::enableCopperRails);

    public static final Block OXIDIZED_COPPER_RAIL = registerBlock("oxidized_copper_rail",
            new CopperRailBlock(Oxidizable.OxidationLevel.OXIDIZED, AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)),
            ModernMinecartsConfig::enableCopperRails);
    public static final Block WAXED_OXIDIZED_COPPER_RAIL = registerBlock("waxed_oxidized_copper_rail",
            new WaxedCopperRailBlock(Oxidizable.OxidationLevel.OXIDIZED, AbstractBlock.Settings.copy(Blocks.POWERED_RAIL)),
            ModernMinecartsConfig::enableCopperRails);


    public static final Block RAIL_CROSSING = registerBlock("rail_crossing",
            new RailCrossingBlock(AbstractBlock.Settings.copy(Blocks.RAIL)),
            ModernMinecartsConfig::enableRailCrossing);

    public static final Block RAIL_JUMP = registerBlock("rail_jump",
            new SlopedRailBlock(AbstractBlock.Settings.copy(Blocks.RAIL)),
            ModernMinecartsConfig::enableRailJump);

    public static final Block POWERED_DETECTOR_RAIL = registerBlock("powered_detector_rail",
            new PoweredDetectorRailBlock(AbstractBlock.Settings.copy(Blocks.DETECTOR_RAIL)),
            ModernMinecartsConfig::enablePoweredDetectorRail);


    private static Block registerBlock(String name, Block block){
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, ModernMinecarts.id(name), block);

    }

    private static Block registerBlock(String name, Block block, BooleanSupplier enabledSupplier) {
        registerBlockItem(name, block, enabledSupplier);
        return Registry.register(Registries.BLOCK, ModernMinecarts.id(name), block);

    }

    public static Item registerBlockItem(String name, Block block){
        return Registry.register(Registries.ITEM, ModernMinecarts.id(name),
                new BlockItem(block, new Item.Settings()));
    }

    public static Item registerBlockItem(String name, Block block, BooleanSupplier enabledSupplier){
        return Registry.register(Registries.ITEM, ModernMinecarts.id(name),
                new FeatureToggleBlockItem(block, new Item.Settings(), enabledSupplier));
    }

    public static void registerModBlocks(){
        ModernMinecarts.LOGGER.info("Registering ModBlocks for " + ModernMinecarts.MOD_ID);


    }
}
