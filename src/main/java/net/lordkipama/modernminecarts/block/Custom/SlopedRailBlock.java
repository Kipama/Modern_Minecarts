package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;

public class SlopedRailBlock extends BaseRailBlock implements ModernMinecartRailSpeed {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;
    public static final EnumProperty<RailShape> CONST_SHAPE = EnumProperty.create(
            "const_shape",
            RailShape.class,
            RailShape.ASCENDING_EAST,
            RailShape.ASCENDING_WEST,
            RailShape.ASCENDING_NORTH,
            RailShape.ASCENDING_SOUTH
    );

    public SlopedRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, RailShape.ASCENDING_NORTH)
                .setValue(CONST_SHAPE, RailShape.ASCENDING_NORTH)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends BaseRailBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean flag = fluidState.getType() == Fluids.WATER;
        BlockState blockState = super.defaultBlockState();
        Direction direction = context.getHorizontalDirection();

        switch (direction) {
            case EAST -> blockState = blockState
                    .setValue(this.getShapeProperty(), RailShape.ASCENDING_EAST)
                    .setValue(CONST_SHAPE, RailShape.ASCENDING_EAST)
                    .setValue(WATERLOGGED, flag);
            case WEST -> blockState = blockState
                    .setValue(this.getShapeProperty(), RailShape.ASCENDING_WEST)
                    .setValue(CONST_SHAPE, RailShape.ASCENDING_WEST)
                    .setValue(WATERLOGGED, flag);
            case NORTH -> blockState = blockState
                    .setValue(this.getShapeProperty(), RailShape.ASCENDING_NORTH)
                    .setValue(CONST_SHAPE, RailShape.ASCENDING_NORTH)
                    .setValue(WATERLOGGED, flag);
            case SOUTH -> blockState = blockState
                    .setValue(this.getShapeProperty(), RailShape.ASCENDING_SOUTH)
                    .setValue(CONST_SHAPE, RailShape.ASCENDING_SOUTH)
                    .setValue(WATERLOGGED, flag);
        }

        return blockState;
    }

    @Override
    protected BlockState updateDir(Level level, BlockPos pos, BlockState state, boolean alwaysPlace) {
        return state;
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, CONST_SHAPE, WATERLOGGED);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, Orientation orientation, boolean isMoving) {
        if (!level.isClientSide() && level.getBlockState(pos).is(this)) {
            if (!canSupportRigidBlock(level, pos.below())) {
                dropResources(state, level, pos);
                level.removeBlock(pos, isMoving);
            } else {
                RailShape constantShape = state.getValue(CONST_SHAPE);
                if (state.getValue(SHAPE) != constantShape) {
                    state = state.setValue(SHAPE, constantShape);
                }
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public float getModernMinecartRailSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        boolean airInFront = false;

        if (state.getValue(SHAPE) == RailShape.ASCENDING_NORTH) {
            airInFront = level.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1)).is(Blocks.AIR);
        } else if (state.getValue(SHAPE) == RailShape.ASCENDING_EAST) {
            airInFront = level.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ())).is(Blocks.AIR);
        } else if (state.getValue(SHAPE) == RailShape.ASCENDING_SOUTH) {
            airInFront = level.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1)).is(Blocks.AIR);
        } else if (state.getValue(SHAPE) == RailShape.ASCENDING_WEST) {
            airInFront = level.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ())).is(Blocks.AIR);
        }

        if (airInFront) {
            return ModernMinecartsConfig.copperSpeed();
        }
        return ModernMinecartsConfig.maxAscendingSpeed();
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @org.jetbrains.annotations.Nullable AbstractMinecart cart) {
        return state.getValue(CONST_SHAPE);
    }

    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlocks.SLOPED_RAIL.get());
    }

    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return this.getCloneItemStack(level, pos, state);
    }
}
