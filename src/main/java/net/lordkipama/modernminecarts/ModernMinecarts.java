package net.lordkipama.modernminecarts;

import com.mojang.logging.LogUtils;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(net.lordkipama.modernminecarts.ModernMinecarts.MOD_ID)
public class ModernMinecarts {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "modernminecarts";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public ModernMinecarts() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        modEventBus.addListener(this::commonSetup);


        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModItems.TESTITEM);

            event.accept(ModBlocks.SWIFT_POWERED_RAIL);
            event.accept(ModBlocks.EXPOSED_SWIFT_POWERED_RAIL);
            event.accept(ModBlocks.WEATHERED_SWIFT_POWERED_RAIL);
            event.accept(ModBlocks.OXIDIZED_SWIFT_POWERED_RAIL);

            event.accept(ModBlocks.WAXED_SWIFT_POWERED_RAIL);
            event.accept(ModBlocks.WAXED_EXPOSED_SWIFT_POWERED_RAIL);
            event.accept(ModBlocks.WAXED_WEATHERED_SWIFT_POWERED_RAIL);
            event.accept(ModBlocks.WAXED_OXIDIZED_SWIFT_POWERED_RAIL);
        }

    }

    //TEMPORARY EXAMPLE METHOD
    //public static final Object2ObjectMap<Block, Block> NEXT_STAGE = Object2ObjectMaps.unmodifiable(Util.make(new Object2ObjectOpenHashMap<>(), (object2IntOpenHashMap) -> {
        //PIPE
   //     object2IntOpenHashMap.put(CopperPipe.COPPER_PIPE, CopperPipe.EXPOSED_PIPE);
    //    object2IntOpenHashMap.put(CopperPipe.EXPOSED_PIPE, CopperPipe.WEATHERED_PIPE);
    //    object2IntOpenHashMap.put(CopperPipe.WEATHERED_PIPE, CopperPipe.OXIDIZED_PIPE);
    //    object2IntOpenHashMap.put(CopperPipe.OXIDIZED_PIPE, CopperPipe.CORRODED_PIPE);
        //FITTING
    //    object2IntOpenHashMap.put(CopperFitting.COPPER_FITTING, CopperFitting.EXPOSED_FITTING);
    //    object2IntOpenHashMap.put(CopperFitting.EXPOSED_FITTING, CopperFitting.WEATHERED_FITTING);
     //   object2IntOpenHashMap.put(CopperFitting.WEATHERED_FITTING, CopperFitting.OXIDIZED_FITTING);
    //    object2IntOpenHashMap.put(CopperFitting.OXIDIZED_FITTING, CopperFitting.CORRODED_FITTING);
    //}));



    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }
}
