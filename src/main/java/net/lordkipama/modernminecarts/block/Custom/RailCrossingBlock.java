package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class RailCrossingBlock extends BaseRailBlock implements ModernMinecartRailSpeed {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;

    public RailCrossingBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends BaseRailBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public @NotNull Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }

    public @NotNull RailShape getRailDirection(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, AbstractMinecart cart) {
        if (cart == null) {
            return RailShape.NORTH_SOUTH;
        }

        Vec3 movement = cart.getDeltaMovement();
        return Math.abs(movement.z) > Math.abs(movement.x) ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST;
    }

    @Override
    public float getModernMinecartRailSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return (float) ModernMinecartsConfig.copperSpeed();
    }
}
