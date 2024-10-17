package net.lordkipama.modernminecarts;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ModernMinecarts implements ModInitializer {
	public static final String MOD_ID = "modernminecarts";
	//public static final Block COPPER_RAIL = new CopperRailBlock(FabricBlockSettings.copyOf(Blocks.POWERED_RAIL));
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.COPPER_RAIL));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.EXPOSED_COPPER_RAIL));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.WEATHERED_COPPER_RAIL));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.OXIDIZED_COPPER_RAIL));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.WAXED_COPPER_RAIL));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.WAXED_EXPOSED_COPPER_RAIL));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.WAXED_WEATHERED_COPPER_RAIL));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL));

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.RAIL_CROSSING));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.add(ModBlocks.RAIL_JUMP));

		//Event handler stick
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			// Check if the player right-clicked
			if (!player.isSpectator()) {
				if (hand == Hand.MAIN_HAND && hitResult.getType() == HitResult.Type.BLOCK) {
					BlockHitResult blockHitResult = (BlockHitResult) hitResult;
					BlockState blockState = world.getBlockState(blockHitResult.getBlockPos());
					BlockPos pos = blockHitResult.getBlockPos();
					Block block = blockState.getBlock();

					// Check if the player is holding a stick
					ItemStack heldItem = player.getStackInHand(hand);
					if (heldItem.getItem() == Items.STICK) {
						// Check if the right-clicked block is a rail
						if (block.equals(Blocks.RAIL)) {
							if (!world.isClient()) {
								//LOGIC
								boolean gotReplaced = false;
								EnumProperty<RailShape> SHAPE = Properties.RAIL_SHAPE;
								EnumProperty<RailShape> CONST_SHAPE = EnumProperty.of("const_shape", RailShape.class);
								if (blockState.get(SHAPE) == RailShape.ASCENDING_NORTH) {
									world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState).with(CONST_SHAPE, RailShape.ASCENDING_NORTH), 3);
									gotReplaced = true;

								} else if (blockState.get(SHAPE) == RailShape.ASCENDING_SOUTH) {
									world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState).with(CONST_SHAPE, RailShape.ASCENDING_SOUTH), 3);
									gotReplaced = true;

								} else if (blockState.get(SHAPE) == RailShape.ASCENDING_EAST) {
									world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState).with(CONST_SHAPE, RailShape.ASCENDING_EAST), 3);
									gotReplaced = true;

								} else if (blockState.get(SHAPE) == RailShape.ASCENDING_WEST) {
									world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState).with(CONST_SHAPE, RailShape.ASCENDING_WEST), 3);
									gotReplaced = true;
								} else if (blockState.get(SHAPE) == RailShape.NORTH_SOUTH) {
									BlockState northernBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1));
									BlockState northernBelowBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() - 1));
									BlockState southernBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1));
									BlockState southernBelowBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() + 1));

									if ((southernBlockState.isIn(BlockTags.RAILS) || southernBelowBlockState.isIn(BlockTags.RAILS)) && !(northernBlockState.isIn(BlockTags.RAILS) || northernBelowBlockState.isIn(BlockTags.RAILS))) {
										world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState.with(SHAPE, RailShape.ASCENDING_NORTH)).with(CONST_SHAPE, RailShape.ASCENDING_NORTH), 3);
										gotReplaced = true;
									} else if (!(southernBlockState.isIn(BlockTags.RAILS) || southernBelowBlockState.isIn(BlockTags.RAILS)) && (northernBlockState.isIn(BlockTags.RAILS) || northernBelowBlockState.isIn(BlockTags.RAILS))) {
										world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState.with(SHAPE, RailShape.ASCENDING_SOUTH)).with(CONST_SHAPE, RailShape.ASCENDING_SOUTH), 3);
										gotReplaced = true;
									}
								} else if (blockState.get(SHAPE) == RailShape.EAST_WEST) {
									BlockState westernBlockState = world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()));
									BlockState westernBelowBlockState = world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY() - 1, pos.getZ()));
									BlockState easternBlockState = world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()));
									BlockState easternBelowBlockState = world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY() - 1, pos.getZ()));

									if ((easternBlockState.isIn(BlockTags.RAILS) || easternBelowBlockState.isIn(BlockTags.RAILS)) && !(westernBlockState.isIn(BlockTags.RAILS) || westernBelowBlockState.isIn(BlockTags.RAILS))) {
										world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState.with(SHAPE, RailShape.ASCENDING_WEST)).with(CONST_SHAPE, RailShape.ASCENDING_WEST), 3);
										gotReplaced = true;
									} else if (!(easternBlockState.isIn(BlockTags.RAILS) || easternBelowBlockState.isIn(BlockTags.RAILS)) && (westernBlockState.isIn(BlockTags.RAILS) || westernBelowBlockState.isIn(BlockTags.RAILS))) {
										world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState.with(SHAPE, RailShape.ASCENDING_EAST)).with(CONST_SHAPE, RailShape.ASCENDING_EAST), 3);
										gotReplaced = true;
									}
								}
								//Unchanged Shapes:
								//else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.NORTH_EAST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.NORTH_WEST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.SOUTH_EAST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.SOUTH_WEST) {}

								if (gotReplaced == true) {
									if (!player.isCreative() && !player.isSpectator()) {
										heldItem.decrement(1);
									}
									player.swingHand(hand);
									world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
								}
							} else {
								ItemStack itemstack = player.getStackInHand(hand);
								boolean gotReplaced = false;
								EnumProperty<RailShape> SHAPE = Properties.RAIL_SHAPE;
								if (blockState.get(SHAPE) == RailShape.ASCENDING_NORTH) {
									gotReplaced = true;

								} else if (blockState.get(SHAPE) == RailShape.ASCENDING_SOUTH) {
									gotReplaced = true;
								} else if (blockState.get(SHAPE) == RailShape.ASCENDING_EAST) {
									gotReplaced = true;
								} else if (blockState.get(SHAPE) == RailShape.ASCENDING_WEST) {
									gotReplaced = true;
								} else if (blockState.get(SHAPE) == RailShape.NORTH_SOUTH) {
									BlockState northernBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1));
									BlockState northernBelowBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() - 1));
									BlockState southernBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1));
									BlockState southernBelowBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() + 1));

									if ((southernBlockState.isIn(BlockTags.RAILS) || southernBelowBlockState.isIn(BlockTags.RAILS)) && !(northernBlockState.isIn(BlockTags.RAILS) || northernBelowBlockState.isIn(BlockTags.RAILS))) {
										gotReplaced = true;
									} else if (!(southernBlockState.isIn(BlockTags.RAILS) || southernBelowBlockState.isIn(BlockTags.RAILS)) && (northernBlockState.isIn(BlockTags.RAILS) || northernBelowBlockState.isIn(BlockTags.RAILS))) {
										gotReplaced = true;
									}
								} else if (blockState.get(SHAPE) == RailShape.EAST_WEST) {
									BlockState westernBlockState = world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()));
									BlockState westernBelowBlockState = world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY() - 1, pos.getZ()));
									BlockState easternBlockState = world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()));
									BlockState easternBelowBlockState = world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY() - 1, pos.getZ()));

									if ((easternBlockState.isIn(BlockTags.RAILS) || easternBelowBlockState.isIn(BlockTags.RAILS)) && !(westernBlockState.isIn(BlockTags.RAILS) || westernBelowBlockState.isIn(BlockTags.RAILS))) {
										gotReplaced = true;
									} else if (!(easternBlockState.isIn(BlockTags.RAILS) || easternBelowBlockState.isIn(BlockTags.RAILS)) && (westernBlockState.isIn(BlockTags.RAILS) || westernBelowBlockState.isIn(BlockTags.RAILS))) {
										gotReplaced = true;
									}
								}
								//Unchanged Shapes:
								//else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.NORTH_EAST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.NORTH_WEST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.SOUTH_EAST) {} else if (state.getValue(BlockStateProperties.RAIL_SHAPE) == RailShape.SOUTH_WEST) {}

								if (gotReplaced == true) {
									if (!player.isCreative() && !player.isSpectator()) {
										itemstack.decrement(1);
									}
									player.swingHand(hand);
									world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
								}
							}

							return ActionResult.PASS;
						}
					}
				}
			}
			return ActionResult.PASS;

		});
		/*Copyright (C) 2022 Cammie

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
    associated documentation files (the "Software"), to use, copy, modify, and/or merge copies of the
    Software, and to permit persons to whom the Software is furnished to do so, subject to the following
    restrictions:

     1) The above copyright notice and this permission notice shall be included in all copies or substantial
        portions of the Software.
     2) You include attribution to the copyright holder(s) in public display of any project that uses any
        portion of the Software.
     3) You may not publish or distribute substantial portions of the Software in its compiled or uncompiled
        forms without prior permission from the copyright holder.
     4) The Software does not make up a substantial portion of your own projects.
    *
    * */
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(entity instanceof AbstractMinecartEntity cart) {
				ItemStack stack = player.getStackInHand(hand);

				if(player.isSneaking() && stack.isOf(Items.CHAIN)) {
					if (world instanceof ServerWorld server) {
						NbtCompound nbt = stack.getOrCreateNbt();

						if (nbt.contains("ParentEntity") && !cart.getUuid().equals(nbt.getUuid("ParentEntity"))) {
							if (server.getEntity(nbt.getUuid("ParentEntity")) instanceof AbstractMinecartEntity parent) {
								Set<ChainMinecartInterface> train = new HashSet<>();
								train.add((ChainMinecartInterface) parent);

								ChainMinecartInterface nextParent;
								while ((nextParent = (ChainMinecartInterface) ((ChainMinecartInterface) parent).getLinkedParent()) instanceof ChainMinecartInterface && !train.contains(nextParent)) {
									train.add(nextParent);
								}

								if (train.contains(cart) || ((ChainMinecartInterface) parent).getLinkedChild() != null) {
									//player.sendMessage(Text.translatable(MinecartTweaks.MOD_ID + ".cant_link_to_engine").formatted(Formatting.RED), true);
								} else {
									if (((ChainMinecartInterface) cart).getLinkedParent() != null) {
										ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) cart, (ChainMinecartInterface) ((ChainMinecartInterface) cart).getLinkedParent());
									}

									ChainMinecartInterface.setParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) cart);
								}
							} else {
								nbt.remove("ParentEntity");

								if (nbt.isEmpty())
									stack.setNbt(null);
							}

							world.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.NEUTRAL, 1F, 1F);

							if (!player.isCreative())
								stack.decrement(1);

							nbt.remove("ParentEntity");

							if (nbt.isEmpty())
								stack.setNbt(null);
						} else {
							nbt.putUuid("ParentEntity", cart.getUuid());
							world.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.BLOCK_CHAIN_HIT, SoundCategory.NEUTRAL, 1F, 1F);
						}
					}
					return ActionResult.success(true);
				}
			}

			return ActionResult.PASS;
		});
	}




	public static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
	}
}


