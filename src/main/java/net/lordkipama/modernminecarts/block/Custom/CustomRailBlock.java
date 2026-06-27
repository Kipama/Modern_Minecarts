package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.util.RailShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
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
    public MapCodec<RailBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        if (RailShapeHelper.isAscending(getRailDirection(state, level, pos, null))) {
            return ModernMinecartsConfig.max_ascending_speed;
        }
        return ModernMinecartsConfig.copper_speed;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockState replacementState = this.getReplacementState(state, level, pos);
        if (replacementState == null) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            level.setBlock(pos, replacementState, 3);
            if (!player.isCreative() && !player.isSpectator()) {
                itemStack.shrink(1);
            }
        }

        player.swing(interactionHand);
        level.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    private BlockState getReplacementState(BlockState state, Level level, BlockPos pos) {
        RailShape railShape = state.getValue(BlockStateProperties.RAIL_SHAPE);
        if (railShape == RailShape.ASCENDING_NORTH || railShape == RailShape.ASCENDING_SOUTH || railShape == RailShape.ASCENDING_EAST || railShape == RailShape.ASCENDING_WEST) {
            return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state);
        }
        if (railShape == RailShape.NORTH_SOUTH) {
            BlockState northernBlockState = level.getBlockState(pos.north());
            BlockState northernBelowBlockState = level.getBlockState(pos.below().north());
            BlockState southernBlockState = level.getBlockState(pos.south());
            BlockState southernBelowBlockState = level.getBlockState(pos.below().south());

            if ((southernBlockState.is(BlockTags.RAILS) || southernBelowBlockState.is(BlockTags.RAILS)) && !(northernBlockState.is(BlockTags.RAILS) || northernBelowBlockState.is(BlockTags.RAILS))) {
                return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(SHAPE, RailShape.ASCENDING_NORTH));
            }
            if (!(southernBlockState.is(BlockTags.RAILS) || southernBelowBlockState.is(BlockTags.RAILS)) && (northernBlockState.is(BlockTags.RAILS) || northernBelowBlockState.is(BlockTags.RAILS))) {
                return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(SHAPE, RailShape.ASCENDING_SOUTH));
            }
            return null;
        }
        if (railShape == RailShape.EAST_WEST) {
            BlockState westernBlockState = level.getBlockState(pos.west());
            BlockState westernBelowBlockState = level.getBlockState(pos.below().west());
            BlockState easternBlockState = level.getBlockState(pos.east());
            BlockState easternBelowBlockState = level.getBlockState(pos.below().east());

            if ((easternBlockState.is(BlockTags.RAILS) || easternBelowBlockState.is(BlockTags.RAILS)) && !(westernBlockState.is(BlockTags.RAILS) || westernBelowBlockState.is(BlockTags.RAILS))) {
                return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(SHAPE, RailShape.ASCENDING_WEST));
            }
            if (!(easternBlockState.is(BlockTags.RAILS) || easternBelowBlockState.is(BlockTags.RAILS)) && (westernBlockState.is(BlockTags.RAILS) || westernBelowBlockState.is(BlockTags.RAILS))) {
                return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(SHAPE, RailShape.ASCENDING_EAST));
            }
        }

        return null;
    }
}
