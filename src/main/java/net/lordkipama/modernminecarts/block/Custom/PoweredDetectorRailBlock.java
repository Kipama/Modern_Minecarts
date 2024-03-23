package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.Item.AbstractMinecartItem;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.entity.CustomAbstractMinecartContainerEntity;
import net.lordkipama.modernminecarts.entity.CustomAbstractMinecartEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartCommandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Predicate;
public class PoweredDetectorRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WEIGHT_INVERTED = BooleanProperty.create("weight_inverted");//BlockStateProperties.INVERTED;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;//

    public PoweredDetectorRailBlock(BlockBehaviour.Properties p_55218_) {
        super(true, p_55218_);
        this.registerDefaultState();
    }
    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    public boolean getDirInverted(BlockState pState){
        return pState.getValue(INVERTED);
    }
    public boolean getWeightInverted(BlockState pState){
        return pState.getValue(WEIGHT_INVERTED);
    }

    public RailShape getRailShape(BlockState pState){
        return pState.getValue(SHAPE);
    }
    public boolean isSignalSource(BlockState pState) {
        return true;
    }

    protected void registerDefaultState() {
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, Boolean.FALSE).setValue(WATERLOGGED, Boolean.FALSE).setValue(WEIGHT_INVERTED, Boolean.FALSE).setValue(INVERTED, Boolean.FALSE));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_55408_) {
        p_55408_.add(SHAPE, WATERLOGGED,POWERED,WEIGHT_INVERTED, INVERTED);
    }

    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        if (!pLevel.isClientSide) {
            if (!pState.getValue(POWERED)) {
                this.checkPressed(pLevel, pPos, pState);
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (itemstack.getItem() instanceof AbstractMinecartItem || itemstack.is(Items.HOPPER) || itemstack.is(Items.CHEST) || itemstack.is(Items.BARREL)) {
            return super.use(state, level, pos, player, interactionHand, blockHitResult);
        }
        if (player.isCrouching()) {
            state = state.setValue(INVERTED, !state.getValue(INVERTED));
            level.setBlock(pos, ModBlocks.POWERED_DETECTOR_RAIL.get().withPropertiesOf(state), 1);
            level.playSound((Player) player, pos, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 0.3F, 0.6F);
        } else {
            state = state.setValue(WEIGHT_INVERTED, !state.getValue(WEIGHT_INVERTED));
            state = state.setValue(POWERED, state.getValue(WEIGHT_INVERTED));
            level.setBlock(pos, ModBlocks.POWERED_DETECTOR_RAIL.get().withPropertiesOf(state), 1);
            level.playSound((Player) player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, state.getValue(WEIGHT_INVERTED) ? 0.55F : 0.5F);
        }
        return InteractionResult.SUCCESS;
    }
    @Override
    protected BlockState updateState(BlockState pState, Level pLevel, BlockPos pPos, boolean pMovedByPiston) {
        pState = this.updateDir(pLevel, pPos, pState, true);
        pLevel.neighborChanged(pState, pPos, this, pPos, pMovedByPiston);
        return pState;
    }

    private void checkPressed(Level pLevel, BlockPos pPos, BlockState pState) {
        if (this.canSurvive(pState, pLevel, pPos)) {
            boolean isPowered = pState.getValue(POWERED);
            boolean isInverted = pState.getValue(WEIGHT_INVERTED);
            List<CustomAbstractMinecartEntity> mclist = this.getInteractingMinecartOfType(pLevel, pPos, CustomAbstractMinecartEntity.class, (p_153125_) -> {
                return true;
            });
            List<CustomAbstractMinecartContainerEntity> container_list = this.getInteractingMinecartOfType(pLevel, pPos, CustomAbstractMinecartContainerEntity.class, (p_153125_) -> {
                return true;
            });
            boolean isContainer = !container_list.isEmpty();

            boolean minecartFull = mclist.stream().anyMatch(Entity::isVehicle);

            if((getAnalogOutputSignal(pState, pLevel, pPos)==15 && !isInverted && isContainer) || (getAnalogOutputSignal(pState, pLevel, pPos)==0 && isInverted && isContainer))
            {
                updateRailState(pLevel, pPos, true, pState);
            }
            else if((getAnalogOutputSignal(pState, pLevel, pPos)!=0 && isInverted && isContainer) || (getAnalogOutputSignal(pState, pLevel, pPos)!=15 && !isInverted && isContainer))
            {
                updateRailState(pLevel, pPos, false, pState);
            }
            else if ((!isPowered && minecartFull && !isInverted && !isContainer) || (!isPowered && !minecartFull && isInverted && !isContainer)) {
                updateRailState(pLevel, pPos, true, pState);
            } else if ((isPowered && !minecartFull && !isInverted && !isContainer)||(isPowered && minecartFull && isInverted && !isContainer)) {
                updateRailState(pLevel, pPos, false, pState);
            }
            pLevel.scheduleTick(pPos, this, 0);

        }
        pLevel.updateNeighbourForOutputSignal(pPos, this);
    }


    private void updateRailState(Level level, BlockPos pos, boolean powered,BlockState pState) {
        BlockState blockstate = pState.setValue(POWERED, powered);
        level.setBlockAndUpdate(pos, blockstate);
        this.updatePowerToConnected(level, pos, blockstate, true);
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);
        level.setBlocksDirty(pos, pState, blockstate);
    }

    private <T extends CustomAbstractMinecartEntity> List<T> getInteractingMinecartOfType(Level pLevel, BlockPos pPos, Class<T> pCartType, Predicate<Entity> pFilter) {
        return  pLevel.getEntitiesOfClass(pCartType, this.getSearchBB(pPos), pFilter);
    }

    private AABB getSearchBB(BlockPos pPos) {
        double d0 = 0.2D;
        return new AABB((double)pPos.getX() + 0.2D, (double)pPos.getY(), (double)pPos.getZ() + 0.2D, (double)(pPos.getX() + 1) - 0.2D, (double)(pPos.getY() + 1) - 0.2D, (double)(pPos.getZ() + 1) - 0.2D);
    }

    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!pOldState.is(pState.getBlock())) {
            BlockState blockstate = this.updateState(pState, pLevel, pPos, pIsMoving);
            this.checkPressed(pLevel, pPos, blockstate);
        }
    }

    protected void updatePowerToConnected(Level pLevel, BlockPos pPos, BlockState pState, boolean pPowered) {
        RailState railstate = new RailState(pLevel, pPos, pState);

        for(BlockPos blockpos : railstate.getConnections()) {
            BlockState blockstate = pLevel.getBlockState(blockpos);
            pLevel.neighborChanged(blockstate, blockpos, blockstate.getBlock(), pPos, false);
        }

    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        //if (pState.getValue(POWERED)) {
            this.checkPressed(pLevel, pPos, pState);
        //}
    }

    public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getValue(POWERED) ? 15 : 0;
    }

    /**
     * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase#getDirectSignal}
     * whenever possible. Implementing/overriding is fine.
     */
    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        if (!pBlockState.getValue(POWERED)) {
            return 0;
        } else {
            return pSide == Direction.UP ? 15 : 0;
        }
    }

    public int getAnalogOutputSignal(BlockState pBlockState, Level pLevel, BlockPos pPos) {
        List<CustomMinecartCommandBlockEntity> list = this.getInteractingMinecartOfType(pLevel, pPos, CustomMinecartCommandBlockEntity.class, (p_153123_) -> {
            return true;
        });
        if (!list.isEmpty()) {
            return list.get(0).getCommandBlock().getSuccessCount();
        }

        List<CustomAbstractMinecartEntity> carts = this.getInteractingMinecartOfType(pLevel, pPos, CustomAbstractMinecartEntity.class, e -> e.isAlive());
        if (!carts.isEmpty() && carts.get(0).getComparatorLevel() > -1) return carts.get(0).getComparatorLevel();
        List<AbstractMinecart> list1 = carts.stream().filter(EntitySelector.CONTAINER_ENTITY_SELECTOR).collect(java.util.stream.Collectors.toList());
        if (!list1.isEmpty()) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container) list1.get(0));
        }


        return 0;
    }
}
