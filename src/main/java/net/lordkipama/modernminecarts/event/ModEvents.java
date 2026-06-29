package net.lordkipama.modernminecarts.event;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = ModernMinecarts.MOD_ID)
public final class ModEvents {
    private static final String PARENT_ENTITY_TAG = "ParentEntity";

    private ModEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());

        if (!stack.is(Items.STICK)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(Blocks.RAIL)) {
            return;
        }

        BlockState replacementState = getSlopedRailReplacementState(state, level, pos);
        if (replacementState == null) {
            return;
        }

        if (!level.isClientSide()) {
            level.setBlock(pos, replacementState, 3);
            if (!player.isCreative() && !player.isSpectator()) {
                stack.shrink(1);
            }
        }

        player.swing(event.getHand());
        level.playSound(player, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1F, 1F);
        event.setCancellationResult(level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());

        if (!event.getLevel().isClientSide()
                && player.isCrouching()
                && stack.getItem() == Items.CHAIN
                && stack.has(DataComponents.CUSTOM_DATA)
                && isRightClickingAir(player, event.getLevel())) {
            CompoundTag nbt = getCustomDataTag(stack);

            if (nbt.contains(PARENT_ENTITY_TAG)) {
                nbt.remove(PARENT_ENTITY_TAG);
                saveCustomDataTag(stack, nbt);

                event.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHAIN_BREAK, SoundSource.NEUTRAL, 1F, 1F);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());
        Entity entity = event.getTarget();

        if (entity instanceof MinecartFurnace furnaceCart && !player.isCrouching()) {
            if (!event.getLevel().isClientSide()) {
                player.openMenu(FurnaceMinecartHelper.createMenuProvider(furnaceCart));
            }
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!(entity instanceof AbstractMinecart cart) || stack.getItem() != Items.CHAIN || !player.isCrouching() || !(event.getLevel() instanceof ServerLevel server)) {
            return;
        }

        CompoundTag nbt = getCustomDataTag(stack);
        if (nbt.contains(PARENT_ENTITY_TAG)) {
            if (!nbt.getUUID(PARENT_ENTITY_TAG).equals(cart.getUUID())
                    && server.getEntity(nbt.getUUID(PARENT_ENTITY_TAG)) instanceof AbstractMinecart parent) {
                if (MinecartLinkHelper.isTrainCircular(parent, cart)) {
                    if (MinecartLinkHelper.getLinkedParent(parent) == cart) {
                        if (MinecartLinkHelper.getLinkedParent(cart) != null) {
                            BlockPos pos = MinecartLinkHelper.getLinkedParent(cart).blockPosition();
                            event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                            MinecartLinkHelper.unsetParentChild(MinecartLinkHelper.getLinkedParent(cart), cart);
                        }
                        if (MinecartLinkHelper.getLinkedChild(parent) != null) {
                            BlockPos pos = MinecartLinkHelper.getLinkedChild(parent).blockPosition();
                            event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                            MinecartLinkHelper.unsetParentChild(parent, MinecartLinkHelper.getLinkedChild(parent));
                        }

                        MinecartLinkHelper.unsetParentChild(cart, parent);
                        MinecartLinkHelper.setParentChild(parent, cart);
                    }
                } else if (MinecartLinkHelper.getLinkedParent(cart) != parent) {
                    if (MinecartLinkHelper.getLinkedParent(cart) != null) {
                        MinecartLinkHelper.unsetParentChild(MinecartLinkHelper.getLinkedParent(cart), cart);
                        BlockPos pos = entity.blockPosition();
                        event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                    }
                    if (MinecartLinkHelper.getLinkedChild(parent) != null) {
                        BlockPos pos = MinecartLinkHelper.getLinkedChild(parent).blockPosition();
                        event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                        MinecartLinkHelper.unsetParentChild(parent, MinecartLinkHelper.getLinkedChild(parent));
                    }
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    MinecartLinkHelper.setParentChild(parent, cart);
                }
            }

            event.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.NEUTRAL, 1F, 1F);
            nbt.remove(PARENT_ENTITY_TAG);
            saveCustomDataTag(stack, nbt);
        } else {
            nbt.putUUID(PARENT_ENTITY_TAG, cart.getUUID());
            saveCustomDataTag(stack, nbt);
            event.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 1F, 1F);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractMinecart minecart) || event.getEntity().level().isClientSide()) {
            return;
        }

        MinecartLinkHelper.tickFollower(minecart);
        MinecartLinkHelper.unlinkRemovedNeighbor(minecart);
    }

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof MinecartFurnace furnaceMinecart) || event.getEntity().level().isClientSide()) {
            return;
        }

        FurnaceMinecartHelper.tick(furnaceMinecart);
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof AbstractMinecart minecart) || event.getLevel().isClientSide()) {
            return;
        }

        AbstractMinecart parent = MinecartLinkHelper.getLinkedParent(minecart);
        AbstractMinecart child = MinecartLinkHelper.getLinkedChild(minecart);
        if (parent != null) {
            MinecartLinkHelper.dropChainItem(minecart);
            MinecartLinkHelper.unsetParentChild(parent, minecart);
        }
        if (child != null) {
            MinecartLinkHelper.dropChainItem(minecart);
            MinecartLinkHelper.unsetParentChild(minecart, child);
        }
    }

    private static boolean isRightClickingAir(Player player, Level level) {
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

        AABB searchBox = player.getBoundingBox()
                .expandTowards(lookDirection.scale(reachDistance))
                .inflate(1.0D);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                player,
                eyePosition,
                endPosition,
                searchBox,
                entity -> !entity.isSpectator() && entity.isPickable()
        );

        return entityHit == null;
    }

    private static CompoundTag getCustomDataTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void saveCustomDataTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    private static BlockState getSlopedRailReplacementState(BlockState state, Level level, BlockPos pos) {
        RailShape shape = state.getValue(BlockStateProperties.RAIL_SHAPE);
        if (shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH || shape == RailShape.ASCENDING_EAST || shape == RailShape.ASCENDING_WEST) {
            return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state);
        }

        if (shape == RailShape.NORTH_SOUTH) {
            BlockState north = level.getBlockState(pos.north());
            BlockState northBelow = level.getBlockState(pos.north().below());
            BlockState south = level.getBlockState(pos.south());
            BlockState southBelow = level.getBlockState(pos.south().below());

            if ((south.is(net.minecraft.tags.BlockTags.RAILS) || southBelow.is(net.minecraft.tags.BlockTags.RAILS))
                    && !(north.is(net.minecraft.tags.BlockTags.RAILS) || northBelow.is(net.minecraft.tags.BlockTags.RAILS))) {
                return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.ASCENDING_NORTH));
            }
            if (!(south.is(net.minecraft.tags.BlockTags.RAILS) || southBelow.is(net.minecraft.tags.BlockTags.RAILS))
                    && (north.is(net.minecraft.tags.BlockTags.RAILS) || northBelow.is(net.minecraft.tags.BlockTags.RAILS))) {
                return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.ASCENDING_SOUTH));
            }
            return null;
        }

        if (shape == RailShape.EAST_WEST) {
            BlockState west = level.getBlockState(pos.west());
            BlockState westBelow = level.getBlockState(pos.west().below());
            BlockState east = level.getBlockState(pos.east());
            BlockState eastBelow = level.getBlockState(pos.east().below());

            if ((east.is(net.minecraft.tags.BlockTags.RAILS) || eastBelow.is(net.minecraft.tags.BlockTags.RAILS))
                    && !(west.is(net.minecraft.tags.BlockTags.RAILS) || westBelow.is(net.minecraft.tags.BlockTags.RAILS))) {
                return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.ASCENDING_WEST));
            }
            if (!(east.is(net.minecraft.tags.BlockTags.RAILS) || eastBelow.is(net.minecraft.tags.BlockTags.RAILS))
                    && (west.is(net.minecraft.tags.BlockTags.RAILS) || westBelow.is(net.minecraft.tags.BlockTags.RAILS))) {
                return ModBlocks.SLOPED_RAIL.get().withPropertiesOf(state.setValue(BlockStateProperties.RAIL_SHAPE, RailShape.ASCENDING_EAST));
            }
        }

        return null;
    }
}
