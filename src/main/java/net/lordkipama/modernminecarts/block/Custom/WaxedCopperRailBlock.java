package net.lordkipama.modernminecarts.block.Custom;


import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Properties;

public class WaxedCopperRailBlock extends PoweredRailBlock {
    private final Oxidizable.OxidationLevel oxidationLevel;
    public WaxedCopperRailBlock(Oxidizable.OxidationLevel oxidationLevel, AbstractBlock.Settings settings) {
        super(settings);
        this.oxidationLevel = oxidationLevel;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit){
        ItemStack itemstack = player.getStackInHand(hand);

        if (itemstack.isIn(ItemTags.AXES)) {

            //Serverside
            if(!world.isClient()) {
                //Replace block with unwaxed version
                if (this.oxidationLevel == Oxidizable.OxidationLevel.UNAFFECTED) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, itemstack);
                    world.setBlockState(pos, ModBlocks.COPPER_RAIL.getStateWithProperties(state), 1);
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.EXPOSED) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, itemstack);
                    world.setBlockState(pos, ModBlocks.EXPOSED_COPPER_RAIL.getStateWithProperties(state), 1);
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.WEATHERED) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, itemstack);
                    world.setBlockState(pos, ModBlocks.WEATHERED_COPPER_RAIL.getStateWithProperties(state), 1);
                } else if (this.oxidationLevel == Oxidizable.OxidationLevel.OXIDIZED) {
                    Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)player, pos, itemstack);
                    world.setBlockState(pos, ModBlocks.OXIDIZED_COPPER_RAIL.getStateWithProperties(state), 1);
                }
            }
            //ClientSide
            else{
                player.swingHand(hand);
                //Play wax off sound event
                world.playSound(player, pos, SoundEvents.ITEM_AXE_WAX_OFF, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.syncWorldEvent(player, 3004, pos, 0);
                if (!player.isCreative() && !player.isSpectator()) {
                    player.getStackInHand(hand).setDamage(player.getStackInHand(hand).getDamage()+1);
                }

            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
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