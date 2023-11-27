package net.lordkipama.modernminecarts.event;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.entity.ChainMinecartInterface;
import net.lordkipama.modernminecarts.entity.CustomAbstractMinecartEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = ModernMinecarts.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void PlayerInteractEvent(PlayerInteractEvent.RightClickBlock event) {
                BlockState pBlockstate =  event.getLevel().getBlockState(event.getPos());
                Block targetedBlock = pBlockstate.getBlock();

                //Wax/Deage Rail
                if (targetedBlock.equals(ModBlocks.COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.EXPOSED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.WEATHERED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.OXIDIZED_COPPER_RAIL.get())) {
                    if (event.getItemStack().getItem() == Items.HONEYCOMB || event.getItemStack().is(ItemTags.AXES)) {
                        event.setUseBlock(Event.Result.ALLOW);
                    } else {
                        event.setUseBlock(Event.Result.DENY);
                    }
                }
                //Dewax Rail
                else if (targetedBlock.equals(ModBlocks.WAXED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_EXPOSED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_WEATHERED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL.get())) {
                    if (event.getItemStack().is(ItemTags.AXES)) {
                        event.setUseBlock(Event.Result.ALLOW);
                    } else {
                        event.setUseBlock(Event.Result.DENY);
                    }
                }
                //Place sloped rail.
                else if (targetedBlock.equals(Blocks.RAIL)) {
                    if (event.getItemStack().getItem() == Items.STICK) {
                        event.setUseBlock(Event.Result.ALLOW);
                        event.setUseItem(Event.Result.DENY);
                    } else {
                        event.setUseBlock(Event.Result.DENY);
                    }
                } else if (targetedBlock.equals(ModBlocks.POWERED_DETECTOR_RAIL.get())) {
                    if (event.getHand() == InteractionHand.MAIN_HAND) {
                        event.setUseBlock(Event.Result.ALLOW);
                    }
                }
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

                            if (nbt.contains("ParentEntity") && !cart.getUUID().equals(nbt.getUUID("ParentEntity"))) {
                                if (server.getEntity(nbt.getUUID("ParentEntity")) instanceof CustomAbstractMinecartEntity parent) {
                                    Set<ChainMinecartInterface> train = new HashSet<>();
                                    train.add(parent);

                                    ChainMinecartInterface nextParent;
                                    while ((nextParent = parent.getLinkedParent()) instanceof ChainMinecartInterface && !train.contains(nextParent)) {
                                        train.add(nextParent);
                                    }

                                    if (train.contains(cart) || parent.getLinkedChild() != null) {
                                        // player.sendMessage(Text.translatable(MinecartTweaks.MOD_ID + ".cant_link_to_engine").formatted(Formatting.RED), true);
                                    } else {
                                        if (cart.getLinkedParent() != null) {
                                            ChainMinecartInterface.unsetParentChild(cart.getLinkedParent(), cart);
                                        }
                                        ChainMinecartInterface.setParentChild(parent, cart);
                                    }
                                } else {
                                    nbt.remove("ParentEntity");

                                    if (nbt.isEmpty()) {
                                        stack.setTag(null);
                                    }
                                }

                                event.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.NEUTRAL, 1F, 1F);

                                if (!player.isCreative())
                                    stack.shrink(1);

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


