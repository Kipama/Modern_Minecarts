package net.lordkipama.modernminecarts.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.Custom.SwiftPoweredRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
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
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.Item;

import javax.xml.transform.Result;
import java.util.List;
import java.util.Objects;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = ModernMinecarts.MOD_ID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void PlayerInteractEvent(PlayerInteractEvent.RightClickBlock event) {
            Block targetedBlock = Minecraft.getInstance().level.getBlockState(event.getPos()).getBlock();

            if(targetedBlock.equals(ModBlocks.SWIFT_POWERED_RAIL.get()) || targetedBlock.equals(ModBlocks.EXPOSED_SWIFT_POWERED_RAIL.get()) || targetedBlock.equals(ModBlocks.WEATHERED_SWIFT_POWERED_RAIL.get()) || targetedBlock.equals(ModBlocks.OXIDIZED_SWIFT_POWERED_RAIL.get())){
                if(event.getItemStack().getItem() == Items.HONEYCOMB || event.getItemStack().is(ItemTags.AXES)) {
                    event.setUseBlock(Event.Result.ALLOW);
                }
                else{
                    event.setUseBlock(Event.Result.DENY);
                }
            }
            else if(targetedBlock.equals(ModBlocks.WAXED_SWIFT_POWERED_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_EXPOSED_SWIFT_POWERED_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_WEATHERED_SWIFT_POWERED_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_OXIDIZED_SWIFT_POWERED_RAIL.get())){
                if(event.getItemStack().is(ItemTags.AXES)) {
                    event.setUseBlock(Event.Result.ALLOW);
                }
                else{
                    event.setUseBlock(Event.Result.DENY);
                }
            }
        }
        /** Add listeners on login */
        @SubscribeEvent
        public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
        {
            System.out.println("onPlayerLogin triggers");
            Player player = event.getEntity();

            ServerAdvancementManager manager = Objects.requireNonNull(player.getServer()).getAdvancements();

            CriteriaTriggers.register(CriteriaTriggers.ITEM_USED_ON_BLOCK);
        }



    }
}
