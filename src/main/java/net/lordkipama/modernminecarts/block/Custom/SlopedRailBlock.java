package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.RailSpeeds;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
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

public class SlopedRailBlock extends BaseRailBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;
    public static final EnumProperty<RailShape> CONST_SHAPE = EnumProperty.create("const_shape", RailShape.class);

    public BlockState currentBlockState = null;


    public SlopedRailBlock(BlockBehaviour.Properties p_55395_) {
        super(true, p_55395_);

        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_WEST).setValue(WATERLOGGED, Boolean.valueOf(false)));
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
            }
            case WEST -> {
                blockstate = blockstate.setValue(this.getShapeProperty(), RailShape.ASCENDING_WEST).setValue(WATERLOGGED, Boolean.valueOf(flag));
            }
            case NORTH -> {
                blockstate = blockstate.setValue(this.getShapeProperty(), RailShape.ASCENDING_NORTH).setValue(WATERLOGGED, Boolean.valueOf(flag));
            }
            case SOUTH -> {
                blockstate = blockstate.setValue(this.getShapeProperty(), RailShape.ASCENDING_SOUTH).setValue(WATERLOGGED, Boolean.valueOf(flag));
            }
        }

        if(!blockstate.getValue(CONST_SHAPE).isAscending()){
            blockstate = blockstate.setValue(CONST_SHAPE, blockstate.getValue(SHAPE));
        }

        return blockstate;
    }


    //Necessary
    @Override
    protected BlockState updateDir(Level pLevel, BlockPos pPos, BlockState pState, boolean pAlwaysPlace) {
        return pState; //currentBlockState
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
        pBuilder.add(FACING, SHAPE, WATERLOGGED,CONST_SHAPE);
    }



    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!pLevel.isClientSide && pLevel.getBlockState(pPos).is(this)) {
            if (!canSupportRigidBlock(pLevel, pPos.below())) {
                dropResources(pState, pLevel, pPos);
                pLevel.removeBlock(pPos, pIsMoving);
            } else {
                if(!pState.getValue(CONST_SHAPE).isAscending()){
                    pState = pState.setValue(CONST_SHAPE, pState.getValue(SHAPE));
                }
                else{
                    pState = pState.setValue(SHAPE, pState.getValue(CONST_SHAPE));
                }

                if(pState.getValue(SHAPE).toString().equals("east_west") || pState.getValue(SHAPE).toString().equals("north_south")) {
                    pLevel.setBlock(pPos, pState, 0);
                }

                pLevel.setBlock(pPos, pState, 0);

            }

        }
    }


    @Override
    protected BlockState updateState(BlockState pState, Level pLevel, BlockPos pPos, boolean pMovedByPiston) {
        return pState;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {

        boolean airInFront = false;


        if(state.getValue(SHAPE)== RailShape.ASCENDING_NORTH){
            airInFront = level.getBlockState(new BlockPos(pos.getX(),pos.getY(),pos.getZ()-1)).is(Blocks.AIR);
        }
        else if(state.getValue(SHAPE)== RailShape.ASCENDING_EAST){
            airInFront = level.getBlockState(new BlockPos(pos.getX()+1,pos.getY(),pos.getZ())).is(Blocks.AIR);
        }
        else if(state.getValue(SHAPE)== RailShape.ASCENDING_SOUTH){
            airInFront = level.getBlockState(new BlockPos(pos.getX(),pos.getY(),pos.getZ()+1)).is(Blocks.AIR);
        }
        else if(state.getValue(SHAPE)== RailShape.ASCENDING_WEST){
            airInFront = level.getBlockState(new BlockPos(pos.getX()-1,pos.getY(),pos.getZ())).is(Blocks.AIR);
        }

        if(airInFront){
            return RailSpeeds.fastest_speed;
        }
        else {
            return RailSpeeds.max_ascending_speed;
        }
    }

    @Override
    public RailShape getRailDirection(BlockState state, BlockGetter world, BlockPos pos, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.vehicle.AbstractMinecart cart) {

        return state.getValue(SHAPE);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter pLevel, BlockPos pPos, BlockState pState) {
        return new ItemStack(ModBlocks.SLOPED_RAIL.get());
    }
}
