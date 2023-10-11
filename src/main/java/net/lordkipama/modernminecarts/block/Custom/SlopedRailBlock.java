package net.lordkipama.modernminecarts.block.Custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class SlopedRailBlock extends BaseRailBlock {
    /* Ascending Rail Shapes:
   ASCENDING_EAST("ascending_east"),
   ASCENDING_WEST("ascending_west"),
   ASCENDING_NORTH("ascending_north"),
   ASCENDING_SOUTH("ascending_south"),
    * */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;

    public SlopedRailBlock(BlockBehaviour.Properties p_55395_) {
        super(true, p_55395_);
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(false)));

    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        boolean flag = fluidstate.getType() == Fluids.WATER;
        BlockState blockstate = super.defaultBlockState();
        Direction direction = pContext.getHorizontalDirection();

        switch (direction) {
            case EAST -> {
                blockstate = blockstate.setValue(this.getShapeProperty(), RailShape.ASCENDING_EAST).setValue(WATERLOGGED, Boolean.valueOf(flag));
                return blockstate;
            }
            case WEST -> {
                blockstate = blockstate.setValue(this.getShapeProperty(), RailShape.ASCENDING_WEST).setValue(WATERLOGGED, Boolean.valueOf(flag));
                return blockstate;
            }
            case NORTH -> {
                blockstate = blockstate.setValue(this.getShapeProperty(), RailShape.ASCENDING_NORTH).setValue(WATERLOGGED, Boolean.valueOf(flag));
                return blockstate;
            }
            case SOUTH -> {
                blockstate = blockstate.setValue(this.getShapeProperty(), RailShape.ASCENDING_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(flag));
                return blockstate;
            }
        }
        return blockstate;
    }

    //Necessary
    @Override
    protected BlockState updateDir(Level pLevel, BlockPos pPos, BlockState pState, boolean pAlwaysPlace) {
        return pState;
    }

    //Necessary
    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    // Necessary
    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
        return pState;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, SHAPE, WATERLOGGED);
    }

    //On itself useless
    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide && pLevel.getBlockState(pPos).is(this)) {
            //RailShape railshape = getRailDirection(pState, pLevel, pPos, null);
            if (!canSupportRigidBlock(pLevel, pPos.below())) {
                dropResources(pState, pLevel, pPos);
                pLevel.removeBlock(pPos, pIsMoving);
            } else {
                this.updateState(pState, pLevel, pPos, pBlock);
            }

        }
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return 8f;
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.vehicle.AbstractMinecart cart) {
        return state.getValue(SHAPE);
    }
}
