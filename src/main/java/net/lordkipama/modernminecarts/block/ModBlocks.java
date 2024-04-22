package net.lordkipama.modernminecarts.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.RailCrossingBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block COPPER_RAIL = registerBlock("copper_rail",
            new CopperRailBlock(Oxidizable.OxidationLevel.UNAFFECTED,FabricBlockSettings.copyOf(Blocks.POWERED_RAIL)));
    public static final Block WAXED_COPPER_RAIL = registerBlock("waxed_copper_rail",
            new WaxedCopperRailBlock(Oxidizable.OxidationLevel.UNAFFECTED,FabricBlockSettings.copyOf(Blocks.POWERED_RAIL)));

    public static final Block EXPOSED_COPPER_RAIL = registerBlock("exposed_copper_rail",
            new CopperRailBlock(Oxidizable.OxidationLevel.EXPOSED,FabricBlockSettings.copyOf(Blocks.POWERED_RAIL)));
    public static final Block WAXED_EXPOSED_COPPER_RAIL = registerBlock("waxed_exposed_copper_rail",
            new WaxedCopperRailBlock(Oxidizable.OxidationLevel.EXPOSED,FabricBlockSettings.copyOf(Blocks.POWERED_RAIL)));

    public static final Block WEATHERED_COPPER_RAIL = registerBlock("weathered_copper_rail",
            new CopperRailBlock(Oxidizable.OxidationLevel.WEATHERED,FabricBlockSettings.copyOf(Blocks.POWERED_RAIL)));
    public static final Block WAXED_WEATHERED_COPPER_RAIL = registerBlock("waxed_weathered_copper_rail",
            new WaxedCopperRailBlock(Oxidizable.OxidationLevel.WEATHERED,FabricBlockSettings.copyOf(Blocks.POWERED_RAIL)));

    public static final Block OXIDIZED_COPPER_RAIL = registerBlock("oxidized_copper_rail",
            new CopperRailBlock(Oxidizable.OxidationLevel.OXIDIZED,FabricBlockSettings.copyOf(Blocks.POWERED_RAIL)));
    public static final Block WAXED_OXIDIZED_COPPER_RAIL = registerBlock("waxed_oxidized_copper_rail",
            new WaxedCopperRailBlock(Oxidizable.OxidationLevel.OXIDIZED,FabricBlockSettings.copyOf(Blocks.POWERED_RAIL)));


    public static final Block RAIL_CROSSING = registerBlock("rail_crossing",
            new RailCrossingBlock(FabricBlockSettings.copyOf(Blocks.RAIL)));

    public static final Block RAIL_JUMP = registerBlock("rail_jump",
            new SlopedRailBlock(FabricBlockSettings.copyOf(Blocks.RAIL)));


    private static Block registerBlock(String name, Block block){
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(ModernMinecarts.MOD_ID, name), block);

    }

    public static Item registerBlockItem(String name, Block block){
        return  Registry.register(Registries.ITEM, new Identifier(ModernMinecarts.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks(){
        ModernMinecarts.LOGGER.info("Registering ModBlocks for " + ModernMinecarts.MOD_ID);


    }
}
