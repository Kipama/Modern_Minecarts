package net.lordkipama.modernminecarts.event;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.entity.ChainMinecartInterface;
import net.lordkipama.modernminecarts.entity.CustomAbstractMinecartEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartChestEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartFurnaceEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartHopperEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartTNTEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;

public class ModEvents {
    @EventBusSubscriber(modid = ModernMinecarts.MOD_ID)
    public static class ForgeEvents {
        private static final String PARENT_ENTITY_TAG = "ParentEntity";

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

        @SubscribeEvent
        public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
            InteractionHand hand = event.getHand();
            Player player = event.getEntity();
            ItemStack stack = player.getItemInHand(hand);
            Entity entity = event.getTarget();

            if (event.getLevel().isClientSide()) {
                if (entity instanceof CustomAbstractMinecartEntity cart && cart.getMinecartType() == CustomAbstractMinecartEntity.Type.RIDEABLE) {
                    CustomAbstractMinecartEntity parent = cart.getLinkedParent();
                    CustomAbstractMinecartEntity child = cart.getLinkedChild();
                    Item item = stack.getItem();
                    CustomAbstractMinecartEntity.Type type = CustomAbstractMinecartEntity.Type.RIDEABLE;

                    if (item == Items.FURNACE) {
                        type = CustomAbstractMinecartEntity.Type.FURNACE;
                    }
                    if (item == Items.CHEST) {
                        type = CustomAbstractMinecartEntity.Type.CHEST;
                    }
                    if (item == Items.TNT) {
                        type = CustomAbstractMinecartEntity.Type.TNT;
                    }
                    if (item == Items.HOPPER) {
                        type = CustomAbstractMinecartEntity.Type.HOPPER;
                    }

                    if (type != CustomAbstractMinecartEntity.Type.RIDEABLE) {
                        CustomAbstractMinecartEntity minecart = createMinecart(event.getLevel(), cart.getX(), cart.getY(), cart.getZ(), type);
                        minecart.moveTo(cart.getX(), cart.getY(), cart.getZ(), cart.getYRot(), cart.getXRot());
                        minecart.setDeltaMovement(cart.getDeltaMovement());
                        if (cart.hasCustomName()) {
                            minecart.setCustomName(cart.getCustomName());
                            minecart.setCustomNameVisible(cart.isCustomNameVisible());
                        }
                        event.getLevel().addFreshEntity(minecart);

                        if (parent != null) {
                            ChainMinecartInterface.unsetParentChild(parent, cart);
                            ChainMinecartInterface.setParentChild(parent, minecart);
                        }
                        if (child != null) {
                            ChainMinecartInterface.unsetParentChild(cart, child);
                            ChainMinecartInterface.setParentChild(minecart, child);
                        }

                        cart.remove(Entity.RemovalReason.DISCARDED);

                        if (!player.isCreative()) {
                            stack.shrink(1);
                        }
                    }
                }
                return;
            }

            if (entity instanceof CustomAbstractMinecartEntity cart && stack.getItem() == Items.CHAIN && player.isCrouching() && event.getLevel() instanceof ServerLevel server) {
                CompoundTag nbt = getCustomDataTag(stack);

                if (nbt.contains(PARENT_ENTITY_TAG)) {
                    if (!nbt.getUUID(PARENT_ENTITY_TAG).equals(cart.getUUID())
                            && server.getEntity(nbt.getUUID(PARENT_ENTITY_TAG)) instanceof CustomAbstractMinecartEntity parent) {
                        Set<CustomAbstractMinecartEntity> train = new HashSet<>();
                        train.add(parent);

                        CustomAbstractMinecartEntity nextParent;
                        while ((nextParent = parent.getLinkedParent()) instanceof CustomAbstractMinecartEntity && !train.contains(nextParent)) {
                            train.add(nextParent);
                        }

                        if (train.contains(cart)) {
                            if (parent.getLinkedParent() == cart) {
                                if (cart.getLinkedParent() != null) {
                                    BlockPos pos = cart.getLinkedParent().blockPosition();
                                    event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                                    ChainMinecartInterface.unsetParentChild(cart.getLinkedParent(), cart);
                                }
                                if (parent.getLinkedChild() != null) {
                                    BlockPos pos = parent.getLinkedChild().blockPosition();
                                    event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                                    ChainMinecartInterface.unsetParentChild(parent, parent.getLinkedChild());
                                }

                                ChainMinecartInterface.unsetParentChild(cart, parent);
                                ChainMinecartInterface.setParentChild(parent, cart);
                            }
                        } else if (cart.getLinkedParent() != parent) {
                            if (cart.getLinkedParent() != null) {
                                ChainMinecartInterface.unsetParentChild(cart.getLinkedParent(), cart);
                                BlockPos pos = entity.blockPosition();
                                event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                            }
                            if (parent.getLinkedChild() != null) {
                                BlockPos pos = parent.getLinkedChild().blockPosition();
                                event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                                ChainMinecartInterface.unsetParentChild(parent, parent.getLinkedChild());
                            }
                            if (!player.isCreative()) {
                                stack.shrink(1);
                            }
                            ChainMinecartInterface.setParentChild(parent, cart);
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
        }

        private static CustomAbstractMinecartEntity createMinecart(Level level, double x, double y, double z, CustomAbstractMinecartEntity.Type type) {
            return switch (type) {
                case CHEST -> new CustomMinecartChestEntity(level, x, y, z);
                case FURNACE -> new CustomMinecartFurnaceEntity(level, x, y, z);
                case HOPPER -> new CustomMinecartHopperEntity(level, x, y, z);
                case TNT -> new CustomMinecartTNTEntity(level, x, y, z);
                case RIDEABLE -> new CustomMinecartEntity(level, x, y, z);
                default -> new CustomMinecartEntity(level, x, y, z);
            };
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
    }
}
