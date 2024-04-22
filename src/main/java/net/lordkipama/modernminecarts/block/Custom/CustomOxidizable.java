package net.lordkipama.modernminecarts.block.Custom;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.block.*;

import java.util.Optional;
import java.util.function.Supplier;

public interface CustomOxidizable
        extends Degradable<Oxidizable.OxidationLevel> {

    static Supplier<BiMap<Block, Block>> OXIDATION_LEVEL_INCREASES = Suppliers.memoize(() -> ((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)ImmutableBiMap.builder().put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER)).put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER)).put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER)).put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER)).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER)).put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER)).put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB)).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB)).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB)).put(ModBlocks.COPPER_RAIL, ModBlocks.EXPOSED_COPPER_RAIL)).put(ModBlocks.EXPOSED_COPPER_RAIL, ModBlocks.WEATHERED_COPPER_RAIL)).put(ModBlocks.WEATHERED_COPPER_RAIL, ModBlocks.OXIDIZED_COPPER_RAIL)).build());

    static Supplier<BiMap<Block, Block>> OXIDATION_LEVEL_DECREASES = Suppliers.memoize(() -> {
        return OXIDATION_LEVEL_INCREASES.get().inverse();
    });
    public static Optional<Block> getDecreasedOxidationBlock(Block block) {
        return Optional.ofNullable((Block)OXIDATION_LEVEL_DECREASES.get().get(block));
    }

    public static Block getUnaffectedOxidationBlock(Block block) {
        Block block2 = block;
        Block block3 = (Block)OXIDATION_LEVEL_DECREASES.get().get(block2);
        while (block3 != null) {
            block2 = block3;
            block3 = (Block)OXIDATION_LEVEL_DECREASES.get().get(block2);
        }
        return block2;
    }

    public static Optional<BlockState> getDecreasedOxidationState(BlockState state) {
        return net.minecraft.block.Oxidizable.getDecreasedOxidationBlock(state.getBlock()).map(block -> block.getStateWithProperties(state));
    }

    public static Optional<Block> getIncreasedOxidationBlock(Block block) {
        return Optional.ofNullable((Block)OXIDATION_LEVEL_INCREASES.get().get(block));
    }

    public static BlockState getUnaffectedOxidationState(BlockState state) {
        return net.minecraft.block.Oxidizable.getUnaffectedOxidationBlock(state.getBlock()).getStateWithProperties(state);
    }

    @Override
    default public Optional<BlockState> getDegradationResult(BlockState state) {
        return getIncreasedOxidationBlock(state.getBlock()).map(block -> block.getStateWithProperties(state));
    }

    @Override
    default public float getDegradationChanceMultiplier() {
        if (this.getDegradationLevel() == net.minecraft.block.Oxidizable.OxidationLevel.UNAFFECTED) {
            return 0.75f;
        }
        return 1.0f;
    }

    public static enum OxidationLevel {
        UNAFFECTED,
        EXPOSED,
        WEATHERED,
        OXIDIZED;

    }
}