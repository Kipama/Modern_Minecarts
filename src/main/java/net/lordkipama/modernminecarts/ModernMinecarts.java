package net.lordkipama.modernminecarts;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.recipe.ModRecipeSerializers;
import net.lordkipama.modernminecarts.resource.ModResourceConditions;
import net.lordkipama.modernminecarts.screen.ModScreenHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> {
            if (ModernMinecartsConfig.enableCopperRails()) {
                output.accept(ModBlocks.COPPER_RAIL);
                output.accept(ModBlocks.EXPOSED_COPPER_RAIL);
                output.accept(ModBlocks.WEATHERED_COPPER_RAIL);
                output.accept(ModBlocks.OXIDIZED_COPPER_RAIL);
                output.accept(ModBlocks.WAXED_COPPER_RAIL);
                output.accept(ModBlocks.WAXED_EXPOSED_COPPER_RAIL);
                output.accept(ModBlocks.WAXED_WEATHERED_COPPER_RAIL);
                output.accept(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL);
            }
            if (ModernMinecartsConfig.enableRailCrossing()) {
                output.accept(ModBlocks.RAIL_CROSSING);
            }
            if (ModernMinecartsConfig.enableRailJump()) {
                output.accept(ModBlocks.RAIL_JUMP);
            }
            if (ModernMinecartsConfig.enablePoweredDetectorRail()) {
                output.accept(ModBlocks.POWERED_DETECTOR_RAIL);
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator() || hand != InteractionHand.MAIN_HAND || hitResult.getType() != HitResult.Type.BLOCK) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            ItemStack heldItem = player.getItemInHand(hand);
            if (!heldItem.is(Items.STICK) || !ModernMinecartsConfig.enableRailJump() || !block.equals(Blocks.RAIL)) {
                return InteractionResult.PASS;
            }

            boolean replaced = modernminecarts$tryCreateRailJump(world, pos, blockState);
            if (replaced) {
                if (!world.isClientSide() && !player.getAbilities().instabuild) {
                    heldItem.consume(1, player);
                }
                player.swing(hand);
                world.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide() || player.isSpectator() || !player.isCrouching()) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.IRON_CHAIN) || !modernminecarts$isRightClickingAir(player, world)) {
                return InteractionResult.PASS;
            }

            CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (!nbt.contains("ParentEntity")) {
                return InteractionResult.PASS;
            }

            nbt.remove("ParentEntity");
            if (nbt.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
            } else {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHAIN_BREAK, SoundSource.NEUTRAL, 1F, 1F);
            return InteractionResult.SUCCESS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!ModernMinecartsConfig.enableMinecartChaining()) {
                return InteractionResult.PASS;
            }
            if (!(entity instanceof AbstractMinecart cart)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(hand);
            if (!player.isCrouching() || !stack.is(Items.IRON_CHAIN)) {
                return InteractionResult.PASS;
            }
            if (!(world instanceof ServerLevel serverLevel)) {
                return InteractionResult.SUCCESS;
            }

            CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            UUID parentEntityUuid = nbt.read("ParentEntity", UUIDUtil.CODEC).orElse(null);
            if (parentEntityUuid != null && !cart.getUUID().equals(parentEntityUuid)) {
                if (serverLevel.getEntity(parentEntityUuid) instanceof AbstractMinecart parent) {
                    Set<ChainMinecartInterface> train = new HashSet<>();
                    train.add((ChainMinecartInterface) parent);

                    ChainMinecartInterface nextParent;
                    while ((nextParent = (ChainMinecartInterface) ((ChainMinecartInterface) parent).getLinkedParent()) instanceof ChainMinecartInterface && !train.contains(nextParent)) {
                        train.add(nextParent);
                    }

                    if (train.contains(cart)) {
                        if (((ChainMinecartInterface) parent).getLinkedParent() == cart) {
                            if (((ChainMinecartInterface) cart).getLinkedParent() != null) {
                                cart.spawnAtLocation(serverLevel, new ItemStack(Items.IRON_CHAIN));
                                ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) ((ChainMinecartInterface) cart).getLinkedParent(), (ChainMinecartInterface) cart);
                            }
                            if (((ChainMinecartInterface) parent).getLinkedChild() != null) {
                                parent.spawnAtLocation(serverLevel, new ItemStack(Items.IRON_CHAIN));
                                ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) ((ChainMinecartInterface) parent).getLinkedChild());
                            }
                            ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) cart, (ChainMinecartInterface) parent);
                            ChainMinecartInterface.setParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) cart);
                        }
                    } else if (((ChainMinecartInterface) cart).getLinkedParent() != parent) {
                        if (((ChainMinecartInterface) cart).getLinkedParent() != null) {
                            ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) ((ChainMinecartInterface) cart).getLinkedParent(), (ChainMinecartInterface) cart);
                            cart.spawnAtLocation(serverLevel, new ItemStack(Items.IRON_CHAIN));
                        }
                        if (((ChainMinecartInterface) parent).getLinkedChild() != null) {
                            ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) ((ChainMinecartInterface) parent).getLinkedChild());
                            parent.spawnAtLocation(serverLevel, new ItemStack(Items.IRON_CHAIN));
                        }
                        if (!player.getAbilities().instabuild) {
                            stack.consume(1, player);
                        }
                        ChainMinecartInterface.setParentChild((ChainMinecartInterface) parent, (ChainMinecartInterface) cart);
                    }
                } else {
                    nbt.remove("ParentEntity");
                    if (nbt.isEmpty()) {
                        stack.remove(DataComponents.CUSTOM_DATA);
                    }
                }

                world.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.NEUTRAL, 1F, 1F);
                nbt.remove("ParentEntity");
                if (nbt.isEmpty()) {
                    stack.remove(DataComponents.CUSTOM_DATA);
                }
            } else {
                if (parentEntityUuid != null && cart.getUUID().equals(parentEntityUuid)) {
                    nbt.remove("ParentEntity");
                    world.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 1F, 1F);
                    if (nbt.isEmpty()) {
                        stack.remove(DataComponents.CUSTOM_DATA);
                    }
                } else {
                    nbt.store("ParentEntity", UUIDUtil.CODEC, cart.getUUID());
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                    world.playSound(null, cart.getX(), cart.getY(), cart.getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 1F, 1F);
                }
            }

            if (nbt.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
            } else {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
            }
            return InteractionResult.SUCCESS;
        });
    }

    private static boolean modernminecarts$tryCreateRailJump(Level world, BlockPos pos, BlockState blockState) {
        RailShape shape = blockState.getValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty());
        if (shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST) {
            world.setBlock(pos, ModBlocks.RAIL_JUMP.withPropertiesOf(blockState), 3);
            return true;
        }

        if (shape == RailShape.NORTH_SOUTH) {
            BlockState north = world.getBlockState(pos.north());
            BlockState northBelow = world.getBlockState(pos.north().below());
            BlockState south = world.getBlockState(pos.south());
            BlockState southBelow = world.getBlockState(pos.south().below());
            if ((south.is(BlockTags.RAILS) || southBelow.is(BlockTags.RAILS)) && !(north.is(BlockTags.RAILS) || northBelow.is(BlockTags.RAILS))) {
                world.setBlock(pos, ModBlocks.RAIL_JUMP.withPropertiesOf(blockState.setValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty(), RailShape.ASCENDING_NORTH)), 3);
                return true;
            }
            if (!(south.is(BlockTags.RAILS) || southBelow.is(BlockTags.RAILS)) && (north.is(BlockTags.RAILS) || northBelow.is(BlockTags.RAILS))) {
                world.setBlock(pos, ModBlocks.RAIL_JUMP.withPropertiesOf(blockState.setValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty(), RailShape.ASCENDING_SOUTH)), 3);
                return true;
            }
        } else if (shape == RailShape.EAST_WEST) {
            BlockState west = world.getBlockState(pos.west());
            BlockState westBelow = world.getBlockState(pos.west().below());
            BlockState east = world.getBlockState(pos.east());
            BlockState eastBelow = world.getBlockState(pos.east().below());
            if ((east.is(BlockTags.RAILS) || eastBelow.is(BlockTags.RAILS)) && !(west.is(BlockTags.RAILS) || westBelow.is(BlockTags.RAILS))) {
                world.setBlock(pos, ModBlocks.RAIL_JUMP.withPropertiesOf(blockState.setValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty(), RailShape.ASCENDING_WEST)), 3);
                return true;
            }
            if (!(east.is(BlockTags.RAILS) || eastBelow.is(BlockTags.RAILS)) && (west.is(BlockTags.RAILS) || westBelow.is(BlockTags.RAILS))) {
                world.setBlock(pos, ModBlocks.RAIL_JUMP.withPropertiesOf(blockState.setValue(((BaseRailBlock) blockState.getBlock()).getShapeProperty(), RailShape.ASCENDING_EAST)), 3);
                return true;
            }
        }

        return false;
    }

    private static boolean modernminecarts$isRightClickingAir(net.minecraft.world.entity.player.Player player, Level level) {
        double reachDistance = 5.0D;
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookDirection = player.getLookAngle();
        Vec3 endPosition = eyePosition.add(lookDirection.scale(reachDistance));

        HitResult blockHit = level.clip(new ClipContext(
                eyePosition,
                endPosition,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
        if (blockHit.getType() != HitResult.Type.MISS) {
            return false;
        }

        AABB searchBox = player.getBoundingBox().expandTowards(lookDirection.scale(reachDistance)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                eyePosition,
                endPosition,
                searchBox,
                entity -> !entity.isSpectator() && entity.isPickable(),
                reachDistance * reachDistance
        );
        return entityHit == null;
    }

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
