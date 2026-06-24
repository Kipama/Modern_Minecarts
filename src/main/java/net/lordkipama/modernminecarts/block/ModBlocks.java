package net.lordkipama.modernminecarts.block;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.RailCrossingBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public class ModBlocks {

    public static final Block COPPER_RAIL = registerBlock("copper_rail",
            settings -> new CopperRailBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings), Blocks.POWERED_RAIL);
    public static final Block WAXED_COPPER_RAIL = registerBlock("waxed_copper_rail",
            settings -> new WaxedCopperRailBlock(Oxidizable.OxidationLevel.UNAFFECTED, settings), Blocks.POWERED_RAIL);

    public static final Block EXPOSED_COPPER_RAIL = registerBlock("exposed_copper_rail",
            settings -> new CopperRailBlock(Oxidizable.OxidationLevel.EXPOSED, settings), Blocks.POWERED_RAIL);
    public static final Block WAXED_EXPOSED_COPPER_RAIL = registerBlock("waxed_exposed_copper_rail",
            settings -> new WaxedCopperRailBlock(Oxidizable.OxidationLevel.EXPOSED, settings), Blocks.POWERED_RAIL);

    public static final Block WEATHERED_COPPER_RAIL = registerBlock("weathered_copper_rail",
            settings -> new CopperRailBlock(Oxidizable.OxidationLevel.WEATHERED, settings), Blocks.POWERED_RAIL);
    public static final Block WAXED_WEATHERED_COPPER_RAIL = registerBlock("waxed_weathered_copper_rail",
            settings -> new WaxedCopperRailBlock(Oxidizable.OxidationLevel.WEATHERED, settings), Blocks.POWERED_RAIL);

    public static final Block OXIDIZED_COPPER_RAIL = registerBlock("oxidized_copper_rail",
            settings -> new CopperRailBlock(Oxidizable.OxidationLevel.OXIDIZED, settings), Blocks.POWERED_RAIL);
    public static final Block WAXED_OXIDIZED_COPPER_RAIL = registerBlock("waxed_oxidized_copper_rail",
            settings -> new WaxedCopperRailBlock(Oxidizable.OxidationLevel.OXIDIZED, settings), Blocks.POWERED_RAIL);


    public static final Block RAIL_CROSSING = registerBlock("rail_crossing",
            RailCrossingBlock::new, Blocks.RAIL);

    public static final Block RAIL_JUMP = registerBlock("rail_jump",
            SlopedRailBlock::new, Blocks.RAIL);

    public static final Block POWERED_DETECTOR_RAIL = registerBlock("powered_detector_rail",
            PoweredDetectorRailBlock::new, Blocks.DETECTOR_RAIL);


    private static Block registerBlock(
            String name,
            Function<AbstractBlock.Settings, Block> factory,
            Block settingsSource
    ) {
        var id = ModernMinecarts.id(name);
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        Block block = factory.apply(AbstractBlock.Settings.copy(settingsSource).registryKey(blockKey));
        Registry.register(Registries.BLOCK, blockKey, block);
        registerBlockItem(name, block);
        return block;
    }

    public static Item registerBlockItem(String name, Block block){
        RegistryKey<Item> itemKey =
                RegistryKey.of(RegistryKeys.ITEM, ModernMinecarts.id(name));
        return Registry.register(
                Registries.ITEM,
                itemKey,
                new BlockItem(block, new Item.Settings().registryKey(itemKey))
        );
    }

    public static void registerModBlocks(){
        ModernMinecarts.LOGGER.info("Registering ModBlocks for " + ModernMinecarts.MOD_ID);


    }
}
