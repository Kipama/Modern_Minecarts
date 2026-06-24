package net.lordkipama.modernminecarts.block.Custom;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.llamalad7.mixinextras.sugar.Local;
import jdk.jfr.Percentage;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Degradable;
import net.minecraft.world.World;

public class CopperRailBlock extends PoweredRailBlock implements CustomOxidizable {

    private final Oxidizable.OxidationLevel oxidationLevel;

    public CopperRailBlock(Oxidizable.OxidationLevel oxidationLevel, AbstractBlock.Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tickDegradation(state, world, pos, random);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return CustomOxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    @Override
    public Oxidizable.OxidationLevel getDegradationLevel() {
        return this.oxidationLevel;
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack itemstack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){

        if (itemstack.getItem() == Items.HONEYCOMB) {

            //Decrement honeycomb
            if (!player.isCreative() && !player.isSpectator()) {
                itemstack.decrement(1);
            }

            if(!world.isClient()) {
                //Replace block with waxed version
                if (this.oxidationLevel == Oxidizable.OxidationLevel.UNAFFECTED) {
                    if (player instanceof ServerPlayerEntity) {
                        Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, itemstack);
                    }
                    world.setBlockState(pos, ModBlocks.WAXED_COPPER_RAIL.getStateWithProperties(state), 1);
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.EXPOSED) {
                    if (player instanceof ServerPlayerEntity) {
                        Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, itemstack);
                    }
                    world.setBlockState(pos, ModBlocks.WAXED_EXPOSED_COPPER_RAIL.getStateWithProperties(state), 1);
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.WEATHERED) {
                    if (player instanceof ServerPlayerEntity) {
                        Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, itemstack);
                    }
                    world.setBlockState(pos, ModBlocks.WAXED_WEATHERED_COPPER_RAIL.getStateWithProperties(state), 1);
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.OXIDIZED) {
                    if (player instanceof ServerPlayerEntity) {
                        Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, itemstack);
                    }
                    world.setBlockState(pos, ModBlocks.WAXED_OXIDIZED_COPPER_RAIL.getStateWithProperties(state), 1);
                }


            }

            //Play sound and particle events
            world.syncWorldEvent(player, 3003, pos, 0);
            player.swingHand(hand);
            return ItemActionResult.success(world.isClient());

        } else if (itemstack.isIn(ItemTags.AXES)) {

            //Serverside
            if(!world.isClient()) {
                //Replace block with younger version
                if (this.oxidationLevel == Oxidizable.OxidationLevel.UNAFFECTED) {
                    return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.EXPOSED) {
                    world.setBlockState(pos, ModBlocks.COPPER_RAIL.getStateWithProperties(state), 1);
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.WEATHERED) {
                    world.setBlockState(pos, ModBlocks.EXPOSED_COPPER_RAIL.getStateWithProperties(state), 1);
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.OXIDIZED) {
                    world.setBlockState(pos, ModBlocks.WEATHERED_COPPER_RAIL.getStateWithProperties(state), 1);
                }
            }
            //ClientSide
            else{
                if (this.oxidationLevel == Oxidizable.OxidationLevel.UNAFFECTED) {
                    return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
                else {
                    player.swingHand(hand);
                    //Play wax off sound event
                    world.playSound(player, pos, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    world.syncWorldEvent(player, 3005, pos, 0);

                    if (!player.isCreative() && !player.isSpectator()) {
                        player.getStackInHand(hand).setDamage(player.getStackInHand(hand).getDamage()+1);
                    }
                }
            }
        }
        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected boolean isPoweredByOtherRails(World world, BlockPos pos, boolean bl, int distance, RailShape shape) {
        BlockState blockState = world.getBlockState(pos);
        if (!(blockState.getBlock() instanceof CopperRailBlock) && !(blockState.getBlock() instanceof WaxedCopperRailBlock)) {
            return false;
        }
        RailShape railShape = blockState.get(SHAPE);
        if (shape == RailShape.EAST_WEST && (railShape == RailShape.NORTH_SOUTH || railShape == RailShape.ASCENDING_NORTH || railShape == RailShape.ASCENDING_SOUTH)) {
            return false;
        }
        if (shape == RailShape.NORTH_SOUTH && (railShape == RailShape.EAST_WEST || railShape == RailShape.ASCENDING_EAST || railShape == RailShape.ASCENDING_WEST)) {
            return false;
        }
        if (blockState.get(POWERED).booleanValue()) {
            if (world.isReceivingRedstonePower(pos)) {
                return true;
            }
            return this.isPoweredByOtherRails(world, pos, blockState, bl, distance + 1);
        }
        return false;
    }
}
