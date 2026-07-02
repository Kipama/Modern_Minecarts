package net.lordkipama.modernminecarts;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.recipe.ModRecipeSerializers;
import net.lordkipama.modernminecarts.resource.ModResourceConditions;
import net.lordkipama.modernminecarts.screen.ModScreenHandlers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.RailShape;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModernMinecarts implements ModInitializer {
    public static final String MOD_ID = "modernminecarts";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SyncChainedMinecartPacket.registerPayload();
        ModernMinecartsConfig.load(LOGGER);
        ModResourceConditions.register();
        ModRecipeSerializers.register();
        ModBlocks.registerModBlocks();
        ModScreenHandlers.register();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
            if (ModernMinecartsConfig.enableCopperRails()) {
                entries.add(ModBlocks.COPPER_RAIL);
                entries.add(ModBlocks.EXPOSED_COPPER_RAIL);
                entries.add(ModBlocks.WEATHERED_COPPER_RAIL);
                entries.add(ModBlocks.OXIDIZED_COPPER_RAIL);
                entries.add(ModBlocks.WAXED_COPPER_RAIL);
                entries.add(ModBlocks.WAXED_EXPOSED_COPPER_RAIL);
                entries.add(ModBlocks.WAXED_WEATHERED_COPPER_RAIL);
                entries.add(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL);
            }
            if (ModernMinecartsConfig.enableRailCrossing()) {
                entries.add(ModBlocks.RAIL_CROSSING);
            }
            if (ModernMinecartsConfig.enableRailJump()) {
                entries.add(ModBlocks.RAIL_JUMP);
            }
            if (ModernMinecartsConfig.enablePoweredDetectorRail()) {
                entries.add(ModBlocks.POWERED_DETECTOR_RAIL);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!player.isSpectator() && hand == Hand.MAIN_HAND && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockState blockState = world.getBlockState(blockHitResult.getBlockPos());
                BlockPos pos = blockHitResult.getBlockPos();
                Block block = blockState.getBlock();

                ItemStack heldItem = player.getStackInHand(hand);
                if (heldItem.getItem() == Items.STICK && ModernMinecartsConfig.enableRailJump() && block.equals(Blocks.RAIL)) {
                    if (!world.isClient()) {
                        boolean gotReplaced = false;
                        EnumProperty<RailShape> shapeProperty = Properties.RAIL_SHAPE;
                        if (blockState.get(shapeProperty) == RailShape.ASCENDING_NORTH
                                || blockState.get(shapeProperty) == RailShape.ASCENDING_SOUTH
                                || blockState.get(shapeProperty) == RailShape.ASCENDING_EAST
                                || blockState.get(shapeProperty) == RailShape.ASCENDING_WEST) {
                            world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState), 3);
                            gotReplaced = true;
                        } else if (blockState.get(shapeProperty) == RailShape.NORTH_SOUTH) {
                            BlockState northernBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1));
                            BlockState northernBelowBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() - 1));
                            BlockState southernBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1));
                            BlockState southernBelowBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() + 1));

                            if ((southernBlockState.isIn(BlockTags.RAILS) || southernBelowBlockState.isIn(BlockTags.RAILS))
                                    && !(northernBlockState.isIn(BlockTags.RAILS) || northernBelowBlockState.isIn(BlockTags.RAILS))) {
                                world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState.with(shapeProperty, RailShape.ASCENDING_NORTH)), 3);
                                gotReplaced = true;
                            } else if (!(southernBlockState.isIn(BlockTags.RAILS) || southernBelowBlockState.isIn(BlockTags.RAILS))
                                    && (northernBlockState.isIn(BlockTags.RAILS) || northernBelowBlockState.isIn(BlockTags.RAILS))) {
                                world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState.with(shapeProperty, RailShape.ASCENDING_SOUTH)), 3);
                                gotReplaced = true;
                            }
                        } else if (blockState.get(shapeProperty) == RailShape.EAST_WEST) {
                            BlockState westernBlockState = world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()));
                            BlockState westernBelowBlockState = world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY() - 1, pos.getZ()));
                            BlockState easternBlockState = world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()));
                            BlockState easternBelowBlockState = world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY() - 1, pos.getZ()));

                            if ((easternBlockState.isIn(BlockTags.RAILS) || easternBelowBlockState.isIn(BlockTags.RAILS))
                                    && !(westernBlockState.isIn(BlockTags.RAILS) || westernBelowBlockState.isIn(BlockTags.RAILS))) {
                                world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState.with(shapeProperty, RailShape.ASCENDING_WEST)), 3);
                                gotReplaced = true;
                            } else if (!(easternBlockState.isIn(BlockTags.RAILS) || easternBelowBlockState.isIn(BlockTags.RAILS))
                                    && (westernBlockState.isIn(BlockTags.RAILS) || westernBelowBlockState.isIn(BlockTags.RAILS))) {
                                world.setBlockState(pos, ModBlocks.RAIL_JUMP.getStateWithProperties(blockState.with(shapeProperty, RailShape.ASCENDING_EAST)), 3);
                                gotReplaced = true;
                            }
                        }

                        if (gotReplaced) {
                            if (!player.isCreative() && !player.isSpectator()) {
                                heldItem.decrement(1);
                            }
                            player.swingHand(hand);
                            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    } else {
                        boolean gotReplaced = false;
                        EnumProperty<RailShape> shapeProperty = Properties.RAIL_SHAPE;
                        if (blockState.get(shapeProperty) == RailShape.ASCENDING_NORTH
                                || blockState.get(shapeProperty) == RailShape.ASCENDING_SOUTH
                                || blockState.get(shapeProperty) == RailShape.ASCENDING_EAST
                                || blockState.get(shapeProperty) == RailShape.ASCENDING_WEST) {
                            gotReplaced = true;
                        } else if (blockState.get(shapeProperty) == RailShape.NORTH_SOUTH) {
                            BlockState northernBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1));
                            BlockState northernBelowBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() - 1));
                            BlockState southernBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1));
                            BlockState southernBelowBlockState = world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ() + 1));

                            if ((southernBlockState.isIn(BlockTags.RAILS) || southernBelowBlockState.isIn(BlockTags.RAILS))
                                    && !(northernBlockState.isIn(BlockTags.RAILS) || northernBelowBlockState.isIn(BlockTags.RAILS))) {
                                gotReplaced = true;
                            } else if (!(southernBlockState.isIn(BlockTags.RAILS) || southernBelowBlockState.isIn(BlockTags.RAILS))
                                    && (northernBlockState.isIn(BlockTags.RAILS) || northernBelowBlockState.isIn(BlockTags.RAILS))) {
                                gotReplaced = true;
                            }
                        } else if (blockState.get(shapeProperty) == RailShape.EAST_WEST) {
                            BlockState westernBlockState = world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ()));
                            BlockState westernBelowBlockState = world.getBlockState(new BlockPos(pos.getX() - 1, pos.getY() - 1, pos.getZ()));
                            BlockState easternBlockState = world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ()));
                            BlockState easternBelowBlockState = world.getBlockState(new BlockPos(pos.getX() + 1, pos.getY() - 1, pos.getZ()));

                            if ((easternBlockState.isIn(BlockTags.RAILS) || easternBelowBlockState.isIn(BlockTags.RAILS))
                                    && !(westernBlockState.isIn(BlockTags.RAILS) || westernBelowBlockState.isIn(BlockTags.RAILS))) {
                                gotReplaced = true;
                            } else if (!(easternBlockState.isIn(BlockTags.RAILS) || easternBelowBlockState.isIn(BlockTags.RAILS))
                                    && (westernBlockState.isIn(BlockTags.RAILS) || westernBelowBlockState.isIn(BlockTags.RAILS))) {
                                gotReplaced = true;
                            }
                        }

                        if (gotReplaced) {
                            if (!player.isCreative() && !player.isSpectator()) {
                                heldItem.decrement(1);
                            }
                            player.swingHand(hand);
                            world.playSound(player, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient() || player.isSpectator() || !player.isSneaking()) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isOf(Items.IRON_CHAIN) || !modernminecarts$isRightClickingAir(player, world)) {
                return ActionResult.PASS;
            }

            NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
            if (!nbt.contains("ParentEntity")) {
                return ActionResult.PASS;
            }

            nbt.remove("ParentEntity");
            if (nbt.isEmpty()) {
                stack.remove(DataComponentTypes.CUSTOM_DATA);
            } else {
                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_CHAIN_BREAK, SoundCategory.NEUTRAL, 1F, 1F);
            return ActionResult.SUCCESS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!ModernMinecartsConfig.enableMinecartChaining()) {
                return ActionResult.PASS;
            }
            if (entity instanceof AbstractMinecartEntity cart) {
                ItemStack stack = player.getStackInHand(hand);

                if (player.isSneaking() && stack.isOf(Items.IRON_CHAIN)) {
                    if (world instanceof ServerWorld server) {
                        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
                        UUID parentEntityUuid = nbt.get("ParentEntity", Uuids.CODEC).orElse(null);

                        if (parentEntityUuid != null && !cart.getUuid().equals(parentEntityUuid)) {
                            if (server.getEntity(parentEntityUuid) instanceof AbstractMinecartEntity parent) {
                                Set<ChainMinecartInterface> train = new HashSet<>();
                                train.add((ChainMinecartInterface) parent);

                                ChainMinecartInterface nextParent;
                                while ((nextParent = (ChainMinecartInterface) ((ChainMinecartInterface) parent).getLinkedParent()) instanceof ChainMinecartInterface
                                        && !train.contains(nextParent)) {
                                    train.add(nextParent);
                                }

                                if (train.contains(cart)) {
                                    if (((ChainMinecartInterface) parent).getLinkedParent() == cart) {
                                        if (((ChainMinecartInterface) cart).getLinkedParent() != null) {
                                            cart.dropStack(server, new ItemStack(Items.IRON_CHAIN));
                                            ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) ((ChainMinecartInterface) cart).getLinkedParent(), (ChainMinecartInterface) cart);
                                        }
                                        if (((ChainMinecartInterface) parent).getLinkedChild() != null) {
                                            parent.dropStack(server, new ItemStack(Items.IRON_CHAIN));
                                            ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) ((ChainMinecartInterface) parent).getLinkedChild());
                                        }

                                        ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) cart, (ChainMinecartInterface) parent);
                                        ChainMinecartInterface.setParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) cart);
                                    }
                                } else if (((ChainMinecartInterface) cart).getLinkedParent() != parent) {
                                    if (((ChainMinecartInterface) cart).getLinkedParent() != null) {
                                        ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) ((ChainMinecartInterface) cart).getLinkedParent(), (ChainMinecartInterface) cart);
                                        cart.dropStack(server, new ItemStack(Items.IRON_CHAIN));
                                    }
                                    if (((ChainMinecartInterface) parent).getLinkedChild() != null) {
                                        ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) ((ChainMinecartInterface) parent).getLinkedChild());
                                        parent.dropStack(server, new ItemStack(Items.IRON_CHAIN));
                                    }
                                    if (!player.isCreative()) {
                                        stack.decrement(1);
                                    }
                                    ChainMinecartInterface.setParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) cart);
                                }
                            } else {
                                nbt.remove("ParentEntity");

                                if (nbt.isEmpty()) {
                                    stack.remove(DataComponentTypes.CUSTOM_DATA);
                                }
                            }

                            world.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.NEUTRAL, 1F, 1F);
                            nbt.remove("ParentEntity");

                            if (nbt.isEmpty()) {
                                stack.remove(DataComponentTypes.CUSTOM_DATA);
                            }
                        } else {
                            if (parentEntityUuid != null && cart.getUuid().equals(parentEntityUuid)) {
                                nbt.remove("ParentEntity");
                                world.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.BLOCK_CHAIN_HIT, SoundCategory.NEUTRAL, 1F, 1F);
                                if (nbt.isEmpty()) {
                                    stack.remove(DataComponentTypes.CUSTOM_DATA);
                                }
                            } else {
                                nbt.put("ParentEntity", Uuids.CODEC, cart.getUuid());
                                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                                world.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.BLOCK_CHAIN_HIT, SoundCategory.NEUTRAL, 1F, 1F);
                            }
                        }
                        if (nbt.isEmpty()) {
                            stack.remove(DataComponentTypes.CUSTOM_DATA);
                        } else {
                            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                        }
                    }
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        });
    }

    private static boolean modernminecarts$isRightClickingAir(net.minecraft.entity.player.PlayerEntity player, World world) {
        double reachDistance = 5.0D;
        Vec3d eyePosition = player.getEyePos();
        Vec3d lookDirection = player.getRotationVec(1.0F);
        Vec3d endPosition = eyePosition.add(lookDirection.multiply(reachDistance));

        HitResult blockHit = world.raycast(new RaycastContext(
                eyePosition,
                endPosition,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));
        if (blockHit.getType() != HitResult.Type.MISS) {
            return false;
        }

        Box searchBox = player.getBoundingBox().stretch(lookDirection.multiply(reachDistance)).expand(1.0D);
        EntityHitResult entityHit = ProjectileUtil.raycast(
                player,
                eyePosition,
                endPosition,
                searchBox,
                entity -> !entity.isSpectator() && entity.canHit(),
                reachDistance * reachDistance
        );
        return entityHit == null;
    }

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }
}
