package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.BlockHitResult;

public class CustomRailBlock extends RailBlock {
    public CustomRailBlock(Properties properties) {
        super(properties);
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (getRailDirection(state, level, pos, null).isAscending()) {
            return ModernMinecartsConfig.maxAscendingSpeed();
        }
        return ModernMinecartsConfig.copperSpeed();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!ModernMinecartsConfig.enableRailJump()) {
            return InteractionResult.PASS;
        }

        // Forge retries block interaction with the offhand after a main-hand PASS, so
        // ignore offhand stick conversions while the player is actively using another item.
        if (interactionHand == InteractionHand.OFF_HAND && !player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.is(Items.STICK)) {
            return InteractionResult.PASS;
        }

        BlockState replacementState = getReplacementState(state, level, pos);
        if (replacementState == null) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel) {
            level.setBlock(pos, createSlopedRailState(replacementState), 3);
            if (!player.isCreative() && !player.isSpectator()) {
                itemStack.shrink(1);
            }
            player.swing(interactionHand);
            level.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private BlockState getReplacementState(BlockState state, Level level, BlockPos pos) {
        RailShape shape = state.getValue(BlockStateProperties.RAIL_SHAPE);
        if (shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST) {
            return state;
        }

        if (shape == RailShape.NORTH_SOUTH) {
            BlockState north = level.getBlockState(pos.north());
            BlockState northBelow = level.getBlockState(pos.north().below());
            BlockState south = level.getBlockState(pos.south());
            BlockState southBelow = level.getBlockState(pos.south().below());

            if ((south.is(BlockTags.RAILS) || southBelow.is(BlockTags.RAILS)) && !(north.is(BlockTags.RAILS) || northBelow.is(BlockTags.RAILS))) {
                return state.setValue(SHAPE, RailShape.ASCENDING_NORTH);
            }
            if (!(south.is(BlockTags.RAILS) || southBelow.is(BlockTags.RAILS)) && (north.is(BlockTags.RAILS) || northBelow.is(BlockTags.RAILS))) {
                return state.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
            }
            return null;
        }

        if (shape == RailShape.EAST_WEST) {
            BlockState west = level.getBlockState(pos.west());
            BlockState westBelow = level.getBlockState(pos.west().below());
            BlockState east = level.getBlockState(pos.east());
            BlockState eastBelow = level.getBlockState(pos.east().below());

            if ((east.is(BlockTags.RAILS) || eastBelow.is(BlockTags.RAILS)) && !(west.is(BlockTags.RAILS) || westBelow.is(BlockTags.RAILS))) {
                return state.setValue(SHAPE, RailShape.ASCENDING_WEST);
            }
            if (!(east.is(BlockTags.RAILS) || eastBelow.is(BlockTags.RAILS)) && (west.is(BlockTags.RAILS) || westBelow.is(BlockTags.RAILS))) {
                return state.setValue(SHAPE, RailShape.ASCENDING_EAST);
            }
        }

        return null;
    }

    private BlockState createSlopedRailState(BlockState replacementState) {
        RailShape shape = replacementState.getValue(SHAPE);
        return ModBlocks.SLOPED_RAIL.get()
                .defaultBlockState()
                .setValue(SlopedRailBlock.SHAPE, shape)
                .setValue(SlopedRailBlock.CONST_SHAPE, shape)
                .setValue(BlockStateProperties.WATERLOGGED, replacementState.getValue(BlockStateProperties.WATERLOGGED));
    }
}
