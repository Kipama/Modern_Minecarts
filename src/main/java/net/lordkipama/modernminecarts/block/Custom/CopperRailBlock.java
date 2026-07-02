package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.util.AdvancementHelper;
import net.lordkipama.modernminecarts.util.RailShapeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CopperRailBlock extends PoweredRailBlock implements WeatheringRailBlock, ModernMinecartRailSpeed {
    private final WeatheringRailBlock.WeatherState weatherState;

    public CopperRailBlock(BlockBehaviour.Properties properties, WeatheringRailBlock.WeatherState weatherState) {
        super(properties);
        this.weatherState = weatherState;
    }

    @Override
    public MapCodec<PoweredRailBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (itemStack.getItem() == Items.HONEYCOMB) {
            if (!level.isClientSide()) {
                if (!player.isCreative() && !player.isSpectator()) {
                    itemStack.shrink(1);
                }

                if (this.weatherState == WeatheringRailBlock.WeatherState.UNAFFECTED) {
                    level.setBlock(pos, ModBlocks.WAXED_COPPER_RAIL.withPropertiesOf(state), 1);
                } else if (this.weatherState == WeatheringRailBlock.WeatherState.EXPOSED) {
                    level.setBlock(pos, ModBlocks.WAXED_EXPOSED_COPPER_RAIL.withPropertiesOf(state), 1);
                } else if (this.weatherState == WeatheringRailBlock.WeatherState.WEATHERED) {
                    level.setBlock(pos, ModBlocks.WAXED_WEATHERED_COPPER_RAIL.withPropertiesOf(state), 1);
                } else if (this.weatherState == WeatheringRailBlock.WeatherState.OXIDIZED) {
                    level.setBlock(pos, ModBlocks.WAXED_OXIDIZED_COPPER_RAIL.withPropertiesOf(state), 1);
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    AdvancementHelper.awardWaxOn(serverPlayer);
                }
            }

            level.levelEvent(player, 3003, pos, 0);
            player.swing(interactionHand);
            return InteractionResult.SUCCESS;
        }

        if (!itemStack.is(ItemTags.AXES)) {
            return InteractionResult.PASS;
        }
        if (this.weatherState == WeatherState.UNAFFECTED) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (this.weatherState == WeatherState.EXPOSED) {
                level.setBlock(pos, ModBlocks.COPPER_RAIL.withPropertiesOf(state), 1);
            } else if (this.weatherState == WeatherState.WEATHERED) {
                level.setBlock(pos, ModBlocks.EXPOSED_COPPER_RAIL.withPropertiesOf(state), 1);
            } else if (this.weatherState == WeatherState.OXIDIZED) {
                level.setBlock(pos, ModBlocks.WEATHERED_COPPER_RAIL.withPropertiesOf(state), 1);
            }

            if (!player.isCreative() && !player.isSpectator()) {
                itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            }
        }

        player.swing(interactionHand);
        level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(player, 3005, pos, 0);
        return InteractionResult.SUCCESS;
    }

    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public float getModernMinecartRailSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        float finalSpeed = switch (getAge()) {
            case UNAFFECTED -> (float) ModernMinecartsConfig.copperSpeed();
            case EXPOSED -> (float) ModernMinecartsConfig.exposedCopperSpeed();
            case WEATHERED -> (float) ModernMinecartsConfig.weatheredCopperSpeed();
            case OXIDIZED -> (float) ModernMinecartsConfig.oxidizedCopperSpeed();
        };

        if (RailShapeHelper.isAscending(state.getValue(getShapeProperty())) && finalSpeed >= ModernMinecartsConfig.maxAscendingSpeed()) {
            return (float) ModernMinecartsConfig.maxAscendingSpeed();
        }

        return finalSpeed;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.changeOverTime(state, level, pos, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return this.weatherState != WeatherState.OXIDIZED;
    }

    @Override
    public WeatheringRailBlock.WeatherState getAge() {
        return this.weatherState;
    }
}
