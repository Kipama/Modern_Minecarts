package net.lordkipama.modernminecarts.block.Custom;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class SlopedRailBlock extends AbstractRailBlock {
    public static final EnumProperty<RailShape> SHAPE = EnumProperty.of("shape", RailShape.class, shape -> shape != RailShape.NORTH_EAST && shape != RailShape.NORTH_WEST && shape != RailShape.SOUTH_EAST && shape != RailShape.SOUTH_WEST ); //&& shape != RailShape.NORTH_SOUTH && shape != RailShape.EAST_WEST
    public static final EnumProperty<RailShape> CONST_SHAPE = EnumProperty.of(
            "const_shape",
            RailShape.class,
            RailShape.ASCENDING_EAST,
            RailShape.ASCENDING_WEST,
            RailShape.ASCENDING_NORTH,
            RailShape.ASCENDING_SOUTH
    );

    public SlopedRailBlock(Settings settings) {
        super(true, settings);
        setDefaultState(getDefaultState()
                .with(SHAPE, RailShape.ASCENDING_NORTH)
                .with(CONST_SHAPE, RailShape.ASCENDING_NORTH)
                .with(WATERLOGGED, false));
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED, CONST_SHAPE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = fluidState.getFluid() == Fluids.WATER;
        BlockState blockState = super.getDefaultState();
        Direction direction = ctx.getHorizontalPlayerFacing();
        switch (direction) {
            case EAST -> {
                blockState = blockState.with(this.getShapeProperty(), RailShape.ASCENDING_EAST).with(WATERLOGGED, Boolean.valueOf(bl));
            }
            case WEST -> {
                blockState = blockState.with(this.getShapeProperty(), RailShape.ASCENDING_WEST).with(WATERLOGGED, Boolean.valueOf(bl));
            }
            case NORTH -> {
                blockState = blockState.with(this.getShapeProperty(), RailShape.ASCENDING_NORTH).with(WATERLOGGED, Boolean.valueOf(bl));
            }
            case SOUTH -> {
                blockState = blockState.with(this.getShapeProperty(), RailShape.ASCENDING_SOUTH).with(WATERLOGGED, Boolean.valueOf(bl));
            }
        }


        blockState = blockState.with(CONST_SHAPE, blockState.get(SHAPE));


        return blockState;
    }


    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (world.isClient || !world.getBlockState(pos).isOf(this)) {
            return;
        }
        if(!state.get(CONST_SHAPE).isAscending()){
            state = state.with(CONST_SHAPE, state.get(SHAPE));
        }
        else{
            state = state.with(SHAPE, state.get(CONST_SHAPE));
        }

        if (shouldDropRail(pos, world)) {
            AbstractRailBlock.dropStacks(state, world, pos);
            world.removeBlock(pos, notify);
        } else {
            world.setBlockState(pos, state);
        }
    }

    private static boolean shouldDropRail(BlockPos pos, World world) {
        return !AbstractRailBlock.hasTopRim(world, pos.down());
    }

    @Override
    protected BlockState updateBlockState(World world, BlockPos pos, BlockState state, boolean forceUpdate) {
        return state;
    }

    @Override
    protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor) {
    }
}
