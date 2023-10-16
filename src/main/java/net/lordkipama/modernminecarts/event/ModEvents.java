package net.lordkipama.modernminecarts.event;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraftforge.event.entity.player.PlayerEvent;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

public class ModEvents {
    @Mod.EventBusSubscriber(modid = ModernMinecarts.MOD_ID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void PlayerInteractEvent(PlayerInteractEvent.RightClickBlock event) {
            BlockState pBlockstate = Minecraft.getInstance().level.getBlockState(event.getPos());
            Block targetedBlock = pBlockstate.getBlock();

            //Wax/Deage Rail
            if(targetedBlock.equals(ModBlocks.COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.EXPOSED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.WEATHERED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.OXIDIZED_COPPER_RAIL.get())){
                if(event.getItemStack().getItem() == Items.HONEYCOMB || event.getItemStack().is(ItemTags.AXES)) {
                    event.setUseBlock(Event.Result.ALLOW);
                }
                else{
                    event.setUseBlock(Event.Result.DENY);
                }
            }
            //Dewaxx Rail
            else if(targetedBlock.equals(ModBlocks.WAXED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_EXPOSED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_WEATHERED_COPPER_RAIL.get()) || targetedBlock.equals(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL.get())){
                if(event.getItemStack().is(ItemTags.AXES)) {
                    event.setUseBlock(Event.Result.ALLOW);
                }
                else{
                    event.setUseBlock(Event.Result.DENY);
                }
            }
            //Place sloped rail.
            else if(targetedBlock.equals(Blocks.RAIL)){
                if(event.getItemStack().getItem() == Items.STICK) {
                    event.setUseBlock(Event.Result.ALLOW);
                    event.setUseItem(Event.Result.DENY);
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
