package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.RailSpeeds;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WaxedCopperRailBlock extends PoweredRailBlock{
    private final WaxedCopperRailBlock.WaxedWeatherState waxedWeatherState;
    public WaxedCopperRailBlock(Properties copy, WaxedWeatherState waxedWeatherState) {
        super(copy, true);
        this.waxedWeatherState = waxedWeatherState;
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }


    @Override
    public float getRailMaxSpeed(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {

        return switch (waxedWeatherState.toString()) {
            case "WAXED_UNAFFECTED" -> RailSpeeds.default_copper_speed;
            case "WAXED_EXPOSED" -> RailSpeeds.exposed_copper_speed;
            case "WAXED_WEATHERED" -> RailSpeeds.weathered_copper_speed;
            default -> RailSpeeds.oxidized_copper_speed;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if(!player.isCreative() && !player.isSpectator()) {
            player.getItemInHand(interactionHand).setDamageValue(player.getItemInHand(interactionHand).getDamageValue() + 1);
        }
        if(!level.isClientSide()) {
            if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_UNAFFECTED) {
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, itemstack);
                }
                level.setBlock(pos, ModBlocks.COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_EXPOSED) {
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, itemstack);
                }
                level.setBlock(pos, ModBlocks.EXPOSED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_WEATHERED) {
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, itemstack);
                }
                level.setBlock(pos, ModBlocks.WEATHERED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_OXIDIZED) {
                if (player instanceof ServerPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, itemstack);
                }
                level.setBlock(pos, ModBlocks.OXIDIZED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            }
        }

        player.swing(interactionHand);
        level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(player, 3004, pos, 0);

        return super.use(state, level, pos, player, interactionHand, blockHitResult);
    }

    public static enum WaxedWeatherState {
        WAXED_UNAFFECTED,
        WAXED_EXPOSED,
        WAXED_WEATHERED,
        WAXED_OXIDIZED;

    }
}