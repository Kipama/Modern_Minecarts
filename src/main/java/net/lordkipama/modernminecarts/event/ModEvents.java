package net.lordkipama.modernminecarts.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.Custom.SwiftPoweredRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.Item;

import javax.xml.transform.Result;
import java.util.List;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = ModernMinecarts.MOD_ID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void PlayerInteractEvent(PlayerInteractEvent.RightClickBlock event) {
            if(event.getItemStack().getItem() == Items.HONEYCOMB) {
                BlockPos pos = event.getPos();
                Minecraft instance = Minecraft.getInstance();
                Block targetedBlock = instance.level.getBlockState(pos).getBlock();



                if(targetedBlock.equals(ModBlocks.SWIFT_POWERED_RAIL.get())){

                    /* CODE FOR DECREMENTING STACK IN SURVIVAL
                    if(!event.getEntity().isCreative() && !event.getEntity().isSpectator()){
                        int StackAmount = event.getItemStack().getCount();
                        ItemStack newItemStack = new ItemStack(Items.HONEYCOMB ,StackAmount-1);
                        player.setItemInHand(InteractionHand.MAIN_HAND, newItemStack);
                    }*/

                    //instance.level.getBlockState(pos).getBlock().use(instance.level.getBlockState(event.getPos()), level, event.getPos(), player, event.getHand(),event.getHitVec());

                    event.setUseBlock(Event.Result.ALLOW);
                    event.setUseItem(Event.Result.DENY);
                }
            }
        }
    }
}
