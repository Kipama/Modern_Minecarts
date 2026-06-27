package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Predicate;

public class PoweredDetectorRailBlock extends BaseRailBlock {
    public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WEIGHT_INVERTED = BooleanProperty.create("weight_inverted");
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public PoweredDetectorRailBlock(BlockBehaviour.Properties properties) {
        super(true, properties);
        this.registerDefaultState();
    }

    @Override
    protected MapCodec<? extends BaseRailBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    public boolean getDirInverted(BlockState state) {
        return state.getValue(INVERTED);
    }

    public boolean getWeightInverted(BlockState state) {
        return state.getValue(WEIGHT_INVERTED);
    }

    public RailShape getRailShape(BlockState state) {
        return state.getValue(SHAPE);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    protected void registerDefaultState() {
        this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, Boolean.FALSE).setValue(WATERLOGGED, Boolean.FALSE).setValue(WEIGHT_INVERTED, Boolean.FALSE).setValue(INVERTED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED, POWERED, WEIGHT_INVERTED, INVERTED);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && !state.getValue(POWERED)) {
            this.checkPressed(level, pos, state);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (itemStack.getItem() instanceof MinecartItem || itemStack.is(Items.HOPPER) || itemStack.is(Items.CHEST) || itemStack.is(Items.BARREL)) {
            return InteractionResult.PASS;
        }

        if (player.isCrouching()) {
            state = state.setValue(INVERTED, !state.getValue(INVERTED));
            if (!level.isClientSide()) {
                level.setBlock(pos, ModBlocks.POWERED_DETECTOR_RAIL.get().withPropertiesOf(state), 1);
            }
            level.playSound(player, pos, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 0.3F, 0.6F);
        } else {
            state = state.setValue(WEIGHT_INVERTED, !state.getValue(WEIGHT_INVERTED));
            state = state.setValue(POWERED, state.getValue(WEIGHT_INVERTED));
            if (!level.isClientSide()) {
                level.setBlock(pos, ModBlocks.POWERED_DETECTOR_RAIL.get().withPropertiesOf(state), 1);
            }
            level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.3F, state.getValue(WEIGHT_INVERTED) ? 0.55F : 0.5F);
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected BlockState updateState(BlockState state, Level level, BlockPos pos, boolean movedByPiston) {
        state = this.updateDir(level, pos, state, true);
        level.neighborChanged(state, pos, this, null, movedByPiston);
        return state;
    }

    private void checkPressed(Level level, BlockPos pos, BlockState state) {
        if (this.canSurvive(state, level, pos)) {
            boolean isPowered = state.getValue(POWERED);
            boolean isInverted = state.getValue(WEIGHT_INVERTED);
            List<AbstractMinecart> mclist = this.getInteractingMinecartOfType(level, pos, AbstractMinecart.class, candidate -> true);
            List<AbstractMinecart> containerList = mclist.stream().filter(EntitySelector.CONTAINER_ENTITY_SELECTOR).toList();
            boolean isContainer = !containerList.isEmpty();
            boolean minecartFull = mclist.stream().anyMatch(Entity::isVehicle);

            if ((getAnalogOutputSignal(state, level, pos) == 15 && !isInverted && isContainer) || (getAnalogOutputSignal(state, level, pos) == 0 && isInverted && isContainer)) {
                updateRailState(level, pos, true, state);
            } else if ((getAnalogOutputSignal(state, level, pos) != 0 && isInverted && isContainer) || (getAnalogOutputSignal(state, level, pos) != 15 && !isInverted && isContainer)) {
                updateRailState(level, pos, false, state);
            } else if ((!isPowered && minecartFull && !isInverted && !isContainer) || (!isPowered && !minecartFull && isInverted && !isContainer)) {
                updateRailState(level, pos, true, state);
            } else if ((isPowered && !minecartFull && !isInverted && !isContainer) || (isPowered && minecartFull && isInverted && !isContainer)) {
                updateRailState(level, pos, false, state);
            }
            level.scheduleTick(pos, this, 0);
        }
        level.updateNeighbourForOutputSignal(pos, this);
    }

    private void updateRailState(Level level, BlockPos pos, boolean powered, BlockState state) {
        BlockState blockState = state.setValue(POWERED, powered);
        level.setBlockAndUpdate(pos, blockState);
        this.updatePowerToConnected(level, pos, blockState, true);
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);
        level.setBlocksDirty(pos, state, blockState);
    }

    private <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos pos, Class<T> cartType, Predicate<Entity> filter) {
        return level.getEntitiesOfClass(cartType, this.getSearchBB(pos), filter);
    }

    private AABB getSearchBB(BlockPos pos) {
        return new AABB((double) pos.getX() + 0.2D, (double) pos.getY(), (double) pos.getZ() + 0.2D, (double) (pos.getX() + 1) - 0.2D, (double) (pos.getY() + 1) - 0.2D, (double) (pos.getZ() + 1) - 0.2D);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock())) {
            BlockState blockState = this.updateState(state, level, pos, isMoving);
            this.checkPressed(level, pos, blockState);
        }
    }

    protected void updatePowerToConnected(Level level, BlockPos pos, BlockState state, boolean powered) {
        RailState railState = new RailState(level, pos, state);

        for (BlockPos blockPos : railState.getConnections()) {
            BlockState blockState = level.getBlockState(blockPos);
            level.neighborChanged(blockState, blockPos, blockState.getBlock(), (Orientation) null, false);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.checkPressed(level, pos, state);
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (!blockState.getValue(POWERED)) {
            return 0;
        }
        return side == Direction.UP ? 15 : 0;
    }

    @Override
    public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        List<MinecartCommandBlock> commandBlocks = this.getInteractingMinecartOfType(level, pos, MinecartCommandBlock.class, candidate -> true);
        if (!commandBlocks.isEmpty()) {
            return commandBlocks.get(0).getCommandBlock().getSuccessCount();
        }

        List<AbstractMinecart> list = this.getInteractingMinecartOfType(level, pos, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
        if (!list.isEmpty()) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container) list.get(0));
        }

        return 0;
    }
}
