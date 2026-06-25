package net.lordkipama.modernminecarts.block.Custom;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CopperRailBlock extends PoweredRailBlock implements WeatheringRailBlock {
    private final WeatheringRailBlock.WeatherState weatherState;

    public CopperRailBlock(Properties copy, WeatheringRailBlock.WeatherState weatherState) {
        super(copy, true);
        this.weatherState = weatherState;
    }

    @Override
    protected MapCodec<? extends PoweredRailBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (itemStack.getItem() == Items.HONEYCOMB) {
            if (!level.isClientSide()) {
                if (!player.isCreative() && !player.isSpectator()) {
                    itemStack.shrink(1);
                }

                if (this.weatherState == CopperRailBlock.WeatherState.UNAFFECTED) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                    }
                    level.setBlock(pos, ModBlocks.WAXED_COPPER_RAIL.get().withPropertiesOf(state), 1);
                } else if (this.weatherState == CopperRailBlock.WeatherState.EXPOSED) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                    }
                    level.setBlock(pos, ModBlocks.WAXED_EXPOSED_COPPER_RAIL.get().withPropertiesOf(state), 1);
                } else if (this.weatherState == CopperRailBlock.WeatherState.WEATHERED) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                    }
                    level.setBlock(pos, ModBlocks.WAXED_WEATHERED_COPPER_RAIL.get().withPropertiesOf(state), 1);
                } else if (this.weatherState == CopperRailBlock.WeatherState.OXIDIZED) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, itemStack);
                    }
                    level.setBlock(pos, ModBlocks.WAXED_OXIDIZED_COPPER_RAIL.get().withPropertiesOf(state), 1);
                }
            }

            level.levelEvent(player, 3003, pos, 0);
            player.swing(interactionHand);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!itemStack.is(ItemTags.AXES)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (this.weatherState == WeatherState.UNAFFECTED) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide()) {
            if (this.weatherState == WeatherState.EXPOSED) {
                level.setBlock(pos, ModBlocks.COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.weatherState == WeatherState.WEATHERED) {
                level.setBlock(pos, ModBlocks.EXPOSED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            } else if (this.weatherState == WeatherState.OXIDIZED) {
                level.setBlock(pos, ModBlocks.WEATHERED_COPPER_RAIL.get().withPropertiesOf(state), 1);
            }

            if (!player.isCreative() && !player.isSpectator()) {
                itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            }
        }

        player.swing(interactionHand);
        level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(player, 3005, pos, 0);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return true;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        String currentAge = String.valueOf(getAge());

        float finalSpeed = 0.4f;

        if (currentAge == "UNAFFECTED") {
            finalSpeed = ModernMinecartsConfig.copper_speed;
        } else if (currentAge == "EXPOSED") {
            finalSpeed = ModernMinecartsConfig.exposed_copper_speed;
        } else if (currentAge == "WEATHERED") {
            finalSpeed = ModernMinecartsConfig.weathered_copper_speed;
        } else if (currentAge == "OXIDIZED") {
            finalSpeed = ModernMinecartsConfig.oxidized_copper_speed;
        }

        if (getRailDirection(state, level, pos, null).isAscending() && finalSpeed >= ModernMinecartsConfig.max_ascending_speed) {
            return ModernMinecartsConfig.max_ascending_speed;
        }

        return finalSpeed;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.changeOverTime(state, level, pos, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return WeatheringRailBlock.getNext(state.getBlock()).isPresent();
    }

    @Override
    public WeatheringRailBlock.WeatherState getAge() {
        return this.weatherState;
    }
}
