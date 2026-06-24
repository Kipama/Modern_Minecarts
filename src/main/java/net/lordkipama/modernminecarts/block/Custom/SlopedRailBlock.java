package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;

import jdk.jfr.Percentage;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RailPlacementHelper;
import net.minecraft.block.enums.RailShape;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;

public class SlopedRailBlock extends AbstractRailBlock {
    public static final MapCodec<SlopedRailBlock> CODEC = createCodec(SlopedRailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = EnumProperty.of(
            "shape",
            RailShape.class,
            RailShape::isAscending
    );

    public SlopedRailBlock(Settings settings) {
        super(true, settings);
        setDefaultState(getDefaultState().with(SHAPE, RailShape.ASCENDING_NORTH).with(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<SlopedRailBlock> getCodec() {
        return CODEC;
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
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


        return blockState;
    }


    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, WireOrientation orientation, boolean notify) {
        if (world.isClient() || !world.getBlockState(pos).isOf(this)) {
            return;
        }
        if (shouldDropRail(pos, world)) {
            AbstractRailBlock.dropStacks(state, world, pos);
            world.removeBlock(pos, notify);
        } else {
            world.setBlockState(pos, state);
        }
    }

    private static boolean shouldDropRail(BlockPos pos, World world) {
        System.out.println(!AbstractRailBlock.hasTopRim(world, pos.down()));
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
