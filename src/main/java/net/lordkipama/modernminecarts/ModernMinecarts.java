package net.lordkipama.modernminecarts;

import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.Proxy.IProxy;
import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.block.VanillaBlocks;
import net.lordkipama.modernminecarts.entity.*;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.MCRegisterPacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.lordkipama.modernminecarts.Proxy.ClientProxy;
import net.lordkipama.modernminecarts.Proxy.ServerProxy;
import org.checkerframework.checker.signature.qual.Identifier;

import java.util.List;
import java.util.UUID;
// The value here should match an entry in the META-INF/mods.toml file
@Mod(net.lordkipama.modernminecarts.ModernMinecarts.MOD_ID)
public class ModernMinecarts {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "modernminecarts";
    // Directly reference a slf4j logger

    public static Logger LOGGER = LogManager.getLogger();
    public static IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ModernMinecarts() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        VanillaEntities.register(modEventBus);
        VanillaItems.register(modEventBus);
        VanillaBlocks.register(modEventBus);

        modEventBus.addListener(this::commonSetup);


        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        ModernMinecartsPacketHandler.Init();

    }

    private void commonSetup(final FMLCommonSetupEvent event) {

    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModBlocks.COPPER_RAIL);
            event.accept(ModBlocks.EXPOSED_COPPER_RAIL);
            event.accept(ModBlocks.WEATHERED_COPPER_RAIL);
            event.accept(ModBlocks.OXIDIZED_COPPER_RAIL);

            event.accept(ModBlocks.WAXED_COPPER_RAIL);
            event.accept(ModBlocks.WAXED_EXPOSED_COPPER_RAIL);
            event.accept(ModBlocks.WAXED_WEATHERED_COPPER_RAIL);
            event.accept(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL);

            event.accept(ModBlocks.RAIL_CROSSING);

        }

    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
}
