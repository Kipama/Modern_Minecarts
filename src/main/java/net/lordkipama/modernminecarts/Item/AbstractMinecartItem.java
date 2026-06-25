package net.lordkipama.modernminecarts.Item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMinecartItem extends Item {
    public AbstractMinecartItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, DISPENSER_BEHAVIOR);
    }

    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = world.getBlockState(blockpos);

        if (!blockstate.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        if (!world.isClientSide()) {
            RailShape railshape = blockstate.getBlock() instanceof RailBlock railBlock
                    ? railBlock.getRailDirection(blockstate, world, blockpos, null)
                    : RailShape.NORTH_SOUTH;
            double d0 = railshape.isAscending() ? 0.5D : 0.0D;
            createMinecart(stack, world, blockpos.getX() + 0.5D, blockpos.getY() + 0.0625D + d0, blockpos.getZ() + 0.5D);
        }

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    abstract void createMinecart(ItemStack stack, Level world, double x, double y, double z);

    private static final DispenseItemBehavior DISPENSER_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior behaviourDefaultDispenseItem = new DefaultDispenseItemBehavior();

        @Override
        public @NotNull ItemStack execute(BlockSource source, ItemStack stack) {
            Direction direction = source.state().getValue(DispenserBlock.FACING);
            Level world = source.level();

            double d0 = source.pos().getX() + direction.getStepX() * 1.125D;
            double d1 = source.pos().getY() + direction.getStepY();
            double d2 = source.pos().getZ() + direction.getStepZ() * 1.125D;

            BlockPos blockpos = source.pos().offset(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            BlockState blockstate = world.getBlockState(blockpos);
            RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock railBlock
                    ? railBlock.getRailDirection(blockstate, world, blockpos, null)
                    : RailShape.NORTH_SOUTH;
            double d3;
            if (blockstate.is(BlockTags.RAILS)) {
                d3 = railshape.isAscending() ? 0.6D : 0.1D;
            } else {
                if (!blockstate.isAir() || !world.getBlockState(blockpos.below()).is(BlockTags.RAILS)) {
                    return this.behaviourDefaultDispenseItem.dispense(source, stack);
                }

                BlockState state = world.getBlockState(blockpos.below());
                RailShape shape = state.getBlock() instanceof BaseRailBlock railBlock
                        ? railBlock.getRailDirection(state, world, blockpos.below(), null)
                        : RailShape.NORTH_SOUTH;
                d3 = direction != Direction.DOWN && shape.isAscending() ? -0.4D : -0.9D;
            }

            if (stack.getItem() instanceof AbstractMinecartItem minecartItem) {
                minecartItem.createMinecart(stack, world, d0, d1 + d3, d2);
                stack.shrink(1);
            }

            return stack;
        }
    };
}
