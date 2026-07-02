package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface WeatheringRailBlock extends ChangeOverTimeBlock<WeatheringRailBlock.WeatherState> {
    private static Optional<Block> modernminecarts$getBlock(String path) {
        return BuiltInRegistries.BLOCK.getOptional(ModernMinecarts.id(path));
    }

    static Optional<Block> getPrevious(Block block) {
        Identifier key = BuiltInRegistries.BLOCK.getKey(block);
        if (key == null) {
            return Optional.empty();
        }

        return switch (key.getPath()) {
            case "exposed_copper_rail" -> modernminecarts$getBlock("copper_rail");
            case "weathered_copper_rail" -> modernminecarts$getBlock("exposed_copper_rail");
            case "oxidized_copper_rail" -> modernminecarts$getBlock("weathered_copper_rail");
            default -> Optional.empty();
        };
    }

    static Block getFirst(Block block) {
        Block current = block;

        Optional<Block> previous = getPrevious(block);
        while (previous.isPresent()) {
            current = previous.get();
            previous = getPrevious(current);
        }

        return current;
    }

    static Optional<BlockState> getPrevious(BlockState state) {
        return getPrevious(state.getBlock()).map(previous -> previous.withPropertiesOf(state));
    }

    static Optional<Block> getNext(Block block) {
        Identifier key = BuiltInRegistries.BLOCK.getKey(block);
        if (key == null) {
            return Optional.empty();
        }

        return switch (key.getPath()) {
            case "copper_rail" -> modernminecarts$getBlock("exposed_copper_rail");
            case "exposed_copper_rail" -> modernminecarts$getBlock("weathered_copper_rail");
            case "weathered_copper_rail" -> modernminecarts$getBlock("oxidized_copper_rail");
            default -> Optional.empty();
        };
    }

    static BlockState getFirst(BlockState state) {
        return getFirst(state.getBlock()).withPropertiesOf(state);
    }

    default Optional<BlockState> getNext(BlockState state) {
        return getNext(state.getBlock()).map(next -> next.withPropertiesOf(state));
    }

    default float getChanceModifier() {
        return this.getAge() == WeatherState.UNAFFECTED ? 0.75F : 1.0F;
    }

    enum WeatherState {
        UNAFFECTED,
        EXPOSED,
        WEATHERED,
        OXIDIZED
    }
}
