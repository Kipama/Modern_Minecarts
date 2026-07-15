package net.lordkipama.modernminecarts.event;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.entity.ChainMinecartInterface;
import net.lordkipama.modernminecarts.entity.CustomAbstractMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = ModernMinecarts.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            Player player = event.getEntity();
            ItemStack stack = player.getItemInHand(event.getHand());

            if (!event.getLevel().isClientSide()
                    && player.isCrouching()
                    && stack.getItem() == Items.CHAIN
                    && stack.hasTag()
                    && isRightClickingAir(player, event.getLevel())) {
                CompoundTag nbt = stack.getTag();

                if (nbt != null && nbt.contains("ParentEntity")) {
                    nbt.remove("ParentEntity");

                    if (nbt.isEmpty()) {
                        stack.setTag(null);
                    }

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
            if(event.getLevel().isClientSide()) {
                //CLIENTSIDE CHAIN LOGIC
                if (entity instanceof CustomAbstractMinecartEntity mc && mc.getMinecartType() == CustomAbstractMinecartEntity.Type.RIDEABLE) {
                    CustomAbstractMinecartEntity parent = mc.getLinkedParent();
                    CustomAbstractMinecartEntity child = mc.getLinkedChild();
                    Item item = stack.getItem();
                    CustomAbstractMinecartEntity.Type type = CustomAbstractMinecartEntity.Type.RIDEABLE;

                    if (item == Items.FURNACE)
                        type = CustomAbstractMinecartEntity.Type.FURNACE;
                    if (item == Items.CHEST)
                        type = CustomAbstractMinecartEntity.Type.CHEST;
                    if (item == Items.TNT)
                        type = CustomAbstractMinecartEntity.Type.TNT;
                    if (item == Items.HOPPER)
                        type = CustomAbstractMinecartEntity.Type.HOPPER;

                    if (type != CustomAbstractMinecartEntity.Type.RIDEABLE) {
                        CustomAbstractMinecartEntity minecart = (CustomAbstractMinecartEntity) CustomAbstractMinecartEntity.createMinecart(event.getLevel(), mc.getX(), mc.getY(), mc.getZ(), type);
                        event.getLevel().addFreshEntity(minecart);

                        if (parent != null) {
                            ChainMinecartInterface.unsetParentChild(parent, mc);
                            ChainMinecartInterface.setParentChild(parent, minecart);
                        }
                        if (child != null) {
                            ChainMinecartInterface.unsetParentChild(mc, child);
                            ChainMinecartInterface.setParentChild(minecart, child);
                        }

                        mc.remove(Entity.RemovalReason.DISCARDED);

                        if (!player.isCreative())
                            stack.shrink(1);

                    }
                }
            }
            else {
                //SERVERSIDE CHAIN LOGIC
                if (entity instanceof CustomAbstractMinecartEntity cart) {
                    if (stack.getItem() == Items.CHAIN && player.isCrouching()) {
                        if (event.getLevel() instanceof ServerLevel server) {
                            CompoundTag nbt = stack.getOrCreateTag();

                            if (nbt.contains("ParentEntity")) {
                                if(nbt.getUUID("ParentEntity")==cart.getUUID()){
                                }
                                else if (server.getEntity(nbt.getUUID("ParentEntity")) instanceof CustomAbstractMinecartEntity parent) {
                                    Set<CustomAbstractMinecartEntity> train = new HashSet<>();
                                    train.add(parent);

                                    CustomAbstractMinecartEntity nextParent;
                                    while ((nextParent = parent.getLinkedParent()) instanceof CustomAbstractMinecartEntity && !train.contains(nextParent)) {
                                        train.add(nextParent);
                                    }

                                    if (train.contains(cart)) {
                                        if(parent.getLinkedParent()==cart){
                                            if(cart.getLinkedParent()!=null){
                                                BlockPos pos = cart.getLinkedParent().blockPosition();
                                                event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                                                ChainMinecartInterface.unsetParentChild(cart.getLinkedParent(),cart);
                                            }
                                            if(parent.getLinkedChild()!=null){
                                                BlockPos pos = parent.getLinkedChild().blockPosition();
                                                event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                                                ChainMinecartInterface.unsetParentChild(parent, parent.getLinkedChild());
                                            }

                                            ChainMinecartInterface.unsetParentChild(cart,parent);
                                            ChainMinecartInterface.setParentChild(parent, cart);
                                        }
                                    }
                                    else {
                                        if(cart.getLinkedParent()!=parent) {
                                            if (cart.getLinkedParent() != null) {
                                                ChainMinecartInterface.unsetParentChild(cart.getLinkedParent(), cart);
                                                BlockPos pos = event.getPos();
                                                event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                                            }
                                            if (parent.getLinkedChild() != null) {
                                                BlockPos pos = parent.getLinkedChild().blockPosition();
                                                event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                                                ChainMinecartInterface.unsetParentChild(parent, parent.getLinkedChild());
                                            }
                                            if (!player.isCreative())
                                                stack.shrink(1);
                                            ChainMinecartInterface.setParentChild(parent, cart);
                                        }
                                    }
                                } else {
                                    nbt.remove("ParentEntity");

                                    if (nbt.isEmpty()) {
                                        stack.setTag(null);
                                    }
                                }

                                event.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.NEUTRAL, 1F, 1F);



                                nbt.remove("ParentEntity");

                                if (nbt.isEmpty())
                                    stack.setTag(null);
                            } else {
                                nbt.putUUID("ParentEntity", cart.getUUID());
                                event.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 1F, 1F);
                            }

                        }
                    }
                }
            }
        }
    }
}


