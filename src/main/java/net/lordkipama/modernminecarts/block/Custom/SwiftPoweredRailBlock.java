package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.RailSpeeds;
import net.lordkipama.modernminecarts.block.ModBlocks;
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
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.advancements.CriteriaTriggers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;


public class SwiftPoweredRailBlock extends PoweredRailBlock implements WeatheringRailBlock {
    private final WeatheringRailBlock.WeatherState weatherState;

    public SwiftPoweredRailBlock(Properties copy,WeatheringRailBlock.WeatherState weatherState) {
        super(copy, true);
        this.weatherState = weatherState;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (itemstack.getItem() == Items.HONEYCOMB) {

            //Decrement honeycomb
            if (!player.isCreative() && !player.isSpectator()) {

                itemstack.shrink(1);
            }

            if(!level.isClientSide()) {
                //Replace block with waxed version
                if (this.weatherState == SwiftPoweredRailBlock.WeatherState.UNAFFECTED) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, itemstack);
                    }
                    level.setBlock(pos, ModBlocks.WAXED_SWIFT_POWERED_RAIL.get().withPropertiesOf(state), 1);
                } else if (this.weatherState == SwiftPoweredRailBlock.WeatherState.EXPOSED) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, itemstack);
                    }
                    level.setBlock(pos, ModBlocks.WAXED_EXPOSED_SWIFT_POWERED_RAIL.get().withPropertiesOf(state), 1);
                } else if (this.weatherState == SwiftPoweredRailBlock.WeatherState.WEATHERED) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, itemstack);
                    }
                    level.setBlock(pos, ModBlocks.WAXED_WEATHERED_SWIFT_POWERED_RAIL.get().withPropertiesOf(state), 1);
                } else if (this.weatherState == SwiftPoweredRailBlock.WeatherState.OXIDIZED) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, itemstack);
                    }
                    level.setBlock(pos, ModBlocks.WAXED_OXIDIZED_SWIFT_POWERED_RAIL.get().withPropertiesOf(state), 1);
                }


            }

            //Play sound and particle events
            level.levelEvent(player, 3003, pos, 0);
            return super.use(state, level, pos, player, interactionHand, blockHitResult);

        } else if (itemstack.is(ItemTags.AXES)) {


            //Serverside
            if(!level.isClientSide()) {
                //Replace block with younger version
                if (this.weatherState == WeatherState.UNAFFECTED) {
                    return super.use(state, level, pos, player, interactionHand, blockHitResult);
                } else if (this.weatherState == WeatherState.EXPOSED) {
                    level.setBlock(pos, ModBlocks.SWIFT_POWERED_RAIL.get().withPropertiesOf(state), 1);
                } else if (this.weatherState == WeatherState.WEATHERED) {
                    level.setBlock(pos, ModBlocks.EXPOSED_SWIFT_POWERED_RAIL.get().withPropertiesOf(state), 1);
                } else if (this.weatherState == WeatherState.OXIDIZED) {
                    level.setBlock(pos, ModBlocks.WEATHERED_SWIFT_POWERED_RAIL.get().withPropertiesOf(state), 1);
                }
            }
            //ClientSide
            else{
                if (this.weatherState == WeatherState.UNAFFECTED) {
                    return super.use(state, level, pos, player, interactionHand, blockHitResult);
                }
                else {
                    player.swing(interactionHand);
                    //Play wax off sound event
                    level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    level.levelEvent(player, 3005, pos, 0);

                    if (!player.isCreative() && !player.isSpectator()) {
                        player.getItemInHand(interactionHand).setDamageValue(player.getItemInHand(interactionHand).getDamageValue() + 1);
                    }
                }
            }
        }
        return super.use(state, level, pos, player, interactionHand, blockHitResult);
    }



    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
        String currentAge = String.valueOf(getAge());

        return switch (currentAge) {
            case "UNAFFECTED" -> RailSpeeds.default_swift_speed;
            case "EXPOSED" -> RailSpeeds.exposed_swift_speed;
            case "WEATHERED" -> RailSpeeds.weathered_swift_speed;
            default -> RailSpeeds.oxidized_swift_speed;
        };

    }



    //COPPER AGING
    public void randomTick(BlockState p_222665_, ServerLevel p_222666_, BlockPos p_222667_, RandomSource p_222668_) {
        this.onRandomTick(p_222665_, p_222666_, p_222667_, p_222668_);
    }

    public boolean isRandomlyTicking(BlockState p_154935_) {
        return WeatheringRailBlock.getNext(p_154935_.getBlock()).isPresent();
    }

    public WeatheringRailBlock.WeatherState getAge() {
        return this.weatherState;
    }


}