package net.lordkipama.modernminecarts.block.Custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
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

public class RailCrossingBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;

    public RailCrossingBlock(BlockBehaviour.Properties p_55395_) {
        super(true, p_55395_);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    public @NotNull Property<RailShape> getShapeProperty() {
        return SHAPE;
    }


    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_55408_) {
        p_55408_.add(SHAPE, WATERLOGGED);
    }


    @Override
    public @NotNull RailShape getRailDirection(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.vehicle.AbstractMinecart cart) {
        if (cart == null) return RailShape.NORTH_SOUTH;

        Vec3 movement = cart.getDeltaMovement();
        if (Math.abs(movement.x) > Math.abs(movement.z)) return RailShape.EAST_WEST;
        else return RailShape.NORTH_SOUTH;
    }


}