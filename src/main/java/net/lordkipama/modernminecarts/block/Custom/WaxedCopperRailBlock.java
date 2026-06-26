package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WaxedCopperRailBlock extends PoweredRailBlock {
    private final WaxedCopperRailBlock.WaxedWeatherState waxedWeatherState;

    public WaxedCopperRailBlock(Properties copy, WaxedWeatherState waxedWeatherState) {
        super(copy, true);
        this.waxedWeatherState = waxedWeatherState;
    }

    @Override
    public MapCodec<PoweredRailBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        String version = String.valueOf(waxedWeatherState);

        float finalSpeed = 0.4f;

        if (version == "WAXED_UNAFFECTED") {
            finalSpeed = ModernMinecartsConfig.copper_speed;
        } else if (version == "WAXED_EXPOSED") {
            finalSpeed = ModernMinecartsConfig.exposed_copper_speed;
        } else if (version == "WAXED_WEATHERED") {
            finalSpeed = ModernMinecartsConfig.weathered_copper_speed;
        } else if (version == "WAXED_OXIDIZED") {
            finalSpeed = ModernMinecartsConfig.oxidized_copper_speed;
        }

        if (getRailDirection(state, level, pos, null).isAscending() && finalSpeed >= ModernMinecartsConfig.max_ascending_speed) {
            return ModernMinecartsConfig.max_ascending_speed;
        }

        return finalSpeed;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!itemStack.is(ItemTags.AXES)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_UNAFFECTED) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                }
                level.setBlock(pos, ModBlocks.COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_EXPOSED) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                }
                level.setBlock(pos, ModBlocks.EXPOSED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_WEATHERED) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                }
                level.setBlock(pos, ModBlocks.WEATHERED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_OXIDIZED) {
                if (player instanceof ServerPlayer serverPlayer) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                }
                level.setBlock(pos, ModBlocks.OXIDIZED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            }

            if (!player.isCreative() && !player.isSpectator()) {
                itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            }
        }

        player.swing(interactionHand);
        level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(player, 3004, pos, 0);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    public enum WaxedWeatherState {
        WAXED_UNAFFECTED,
        WAXED_EXPOSED,
        WAXED_WEATHERED,
        WAXED_OXIDIZED
    }
}
