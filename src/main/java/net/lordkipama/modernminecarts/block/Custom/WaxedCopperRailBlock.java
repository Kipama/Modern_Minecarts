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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WaxedCopperRailBlock extends PoweredRailBlock implements ModernMinecartRailSpeed {
    private final WaxedWeatherState waxedWeatherState;

    public WaxedCopperRailBlock(BlockBehaviour.Properties properties, WaxedWeatherState waxedWeatherState) {
        super(properties);
        this.waxedWeatherState = waxedWeatherState;
    }

    @Override
    public MapCodec<PoweredRailBlock> codec() {
        return MapCodec.unit(this);
    }

    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public float getModernMinecartRailSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        float finalSpeed = switch (waxedWeatherState) {
            case WAXED_UNAFFECTED -> (float) ModernMinecartsConfig.copperSpeed();
            case WAXED_EXPOSED -> (float) ModernMinecartsConfig.exposedCopperSpeed();
            case WAXED_WEATHERED -> (float) ModernMinecartsConfig.weatheredCopperSpeed();
            case WAXED_OXIDIZED -> (float) ModernMinecartsConfig.oxidizedCopperSpeed();
        };

        if (RailShapeHelper.isAscending(state.getValue(getShapeProperty())) && finalSpeed >= ModernMinecartsConfig.maxAscendingSpeed()) {
            return (float) ModernMinecartsConfig.maxAscendingSpeed();
        }

        return finalSpeed;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (!itemStack.is(ItemTags.AXES)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (this.waxedWeatherState == WaxedWeatherState.WAXED_UNAFFECTED) {
                level.setBlock(pos, ModBlocks.COPPER_RAIL.withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedWeatherState.WAXED_EXPOSED) {
                level.setBlock(pos, ModBlocks.EXPOSED_COPPER_RAIL.withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedWeatherState.WAXED_WEATHERED) {
                level.setBlock(pos, ModBlocks.WEATHERED_COPPER_RAIL.withPropertiesOf(state), 1);
            } else if (this.waxedWeatherState == WaxedWeatherState.WAXED_OXIDIZED) {
                level.setBlock(pos, ModBlocks.OXIDIZED_COPPER_RAIL.withPropertiesOf(state), 1);
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
