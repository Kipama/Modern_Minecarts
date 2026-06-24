package net.lordkipama.modernminecarts.block.Custom;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MinecartItem;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Predicate;

public class PoweredDetectorRailBlock extends DetectorRailBlock {
    public static final EnumProperty<RailShape> SHAPE = Properties.STRAIGHT_RAIL_SHAPE;
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty WEIGHT_INVERTED =
            BooleanProperty.of("weight_inverted");
    public static final BooleanProperty INVERTED = Properties.INVERTED;

    public PoweredDetectorRailBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(SHAPE, RailShape.NORTH_SOUTH)
                .with(POWERED, false)
                .with(WATERLOGGED, false)
                .with(WEIGHT_INVERTED, false)
                .with(INVERTED, false));
    }

    @Override
    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED, POWERED, WEIGHT_INVERTED, INVERTED);
    }

    @Override
    public void onEntityCollision(
            BlockState state,
            World world,
            BlockPos pos,
            Entity entity
    ) {
        if (!world.isClient()) {
            updatePoweredStatus(world, pos, state);
        }
    }

    @Override
    public void scheduledTick(
            BlockState state,
            ServerWorld world,
            BlockPos pos,
            Random random
    ) {
        updatePoweredStatus(world, pos, state);
    }

    @Override
    public void onBlockAdded(
            BlockState state,
            World world,
            BlockPos pos,
            BlockState oldState,
            boolean notify
    ) {
        if (!oldState.isOf(state.getBlock())) {
            BlockState updatedState = updateCurves(state, world, pos, notify);
            updatePoweredStatus(world, pos, updatedState);
        }
    }

    @Override
    public ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.getItem() instanceof MinecartItem
                || stack.isOf(Items.HOPPER)
                || stack.isOf(Items.CHEST)
                || stack.isOf(Items.BARREL)) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            if (player.isSneaking()) {
                state = state.cycle(INVERTED);
                world.playSound(
                        null,
                        pos,
                        SoundEvents.ENTITY_ITEM_FRAME_PLACE,
                        SoundCategory.BLOCKS,
                        0.3F,
                        0.6F
                );
            } else {
                state = state.cycle(WEIGHT_INVERTED);
                world.playSound(
                        null,
                        pos,
                        SoundEvents.BLOCK_COMPARATOR_CLICK,
                        SoundCategory.BLOCKS,
                        0.3F,
                        state.get(WEIGHT_INVERTED) ? 0.55F : 0.5F
                );
            }

            world.setBlockState(pos, state, Block.NOTIFY_ALL);
            updatePoweredStatus(world, pos, state);
        }

        return ActionResult.success(world.isClient());
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(
            BlockState state,
            BlockView world,
            BlockPos pos,
            Direction direction
    ) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Override
    public int getStrongRedstonePower(
            BlockState state,
            BlockView world,
            BlockPos pos,
            Direction direction
    ) {
        return state.get(POWERED) && direction == Direction.UP ? 15 : 0;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        List<CommandBlockMinecartEntity> commandCarts =
                getCarts(world, pos, CommandBlockMinecartEntity.class, Entity::isAlive);
        if (!commandCarts.isEmpty()) {
            return commandCarts.get(0).getCommandExecutor().getSuccessCount();
        }

        List<AbstractMinecartEntity> carts =
                getCarts(world, pos, AbstractMinecartEntity.class, Entity::isAlive);
        for (AbstractMinecartEntity cart : carts) {
            if (cart instanceof VehicleInventory inventory) {
                return ScreenHandler.calculateComparatorOutput(inventory);
            }
        }
        return 0;
    }

    private void updatePoweredStatus(World world, BlockPos pos, BlockState state) {
        if (!canPlaceAt(state, world, pos)) {
            return;
        }

        boolean shouldPower = shouldPower(world, pos, state);
        if (state.get(POWERED) != shouldPower) {
            BlockState updatedState = state.with(POWERED, shouldPower);
            world.setBlockState(pos, updatedState, Block.NOTIFY_ALL);
            updateNearbyRails(world, pos, updatedState, shouldPower);
            world.updateNeighborsAlways(pos, this);
            world.updateNeighborsAlways(pos.down(), this);
            world.scheduleBlockRerenderIfNeeded(pos, state, updatedState);
            state = updatedState;
        }

        world.scheduleBlockTick(pos, this, 20);
        world.updateComparators(pos, this);
    }

    private boolean shouldPower(World world, BlockPos pos, BlockState state) {
        List<AbstractMinecartEntity> carts =
                getCarts(world, pos, AbstractMinecartEntity.class, Entity::isAlive);
        boolean weightInverted = state.get(WEIGHT_INVERTED);

        for (AbstractMinecartEntity cart : carts) {
            if (cart instanceof VehicleInventory inventory) {
                int comparator = ScreenHandler.calculateComparatorOutput(inventory);
                return weightInverted ? comparator == 0 : comparator == 15;
            }
        }

        boolean occupied = carts.stream().anyMatch(Entity::hasPassengers);
        return weightInverted ? !occupied : occupied;
    }

    private <T extends AbstractMinecartEntity> List<T> getCarts(
            World world,
            BlockPos pos,
            Class<T> cartClass,
            Predicate<Entity> predicate
    ) {
        return world.getEntitiesByClass(cartClass, getCartDetectionBox(pos), predicate);
    }

    private Box getCartDetectionBox(BlockPos pos) {
        return new Box(
                pos.getX() + 0.2D,
                pos.getY(),
                pos.getZ() + 0.2D,
                pos.getX() + 0.8D,
                pos.getY() + 0.8D,
                pos.getZ() + 0.8D
        );
    }
}
