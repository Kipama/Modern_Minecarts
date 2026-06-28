package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.util.AdvancementHelper;
import net.lordkipama.modernminecarts.util.RailShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
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
        float finalSpeed = switch (waxedWeatherState) {
            case WAXED_UNAFFECTED -> ModernMinecartsConfig.copper_speed;
            case WAXED_EXPOSED -> ModernMinecartsConfig.exposed_copper_speed;
            case WAXED_WEATHERED -> ModernMinecartsConfig.weathered_copper_speed;
            case WAXED_OXIDIZED -> ModernMinecartsConfig.oxidized_copper_speed;
        };

        if (RailShapeHelper.isAscending(getRailDirection(state, level, pos, null)) && finalSpeed >= ModernMinecartsConfig.max_ascending_speed) {
            return ModernMinecartsConfig.max_ascending_speed;
        }

        return finalSpeed;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!itemStack.is(ItemTags.AXES)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_UNAFFECTED) {
                level.setBlock(pos, ModBlocks.COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_EXPOSED) {
                level.setBlock(pos, ModBlocks.EXPOSED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_WEATHERED) {
                level.setBlock(pos, ModBlocks.WEATHERED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedCopperRailBlock.WaxedWeatherState.WAXED_OXIDIZED) {
                level.setBlock(pos, ModBlocks.OXIDIZED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            }

            if (!player.isCreative() && !player.isSpectator()) {
                itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            }

            if (player instanceof ServerPlayer serverPlayer) {
                AdvancementHelper.awardWaxOff(serverPlayer);
            }
        }

        player.swing(interactionHand);
        level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(player, 3004, pos, 0);
        return InteractionResult.SUCCESS;
    }

    public enum WaxedWeatherState {
        WAXED_UNAFFECTED,
        WAXED_EXPOSED,
        WAXED_WEATHERED,
        WAXED_OXIDIZED
    }
}
