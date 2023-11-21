package net.lordkipama.modernminecarts;

import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.Proxy.IProxy;
import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.block.VanillaBlocks;
import net.lordkipama.modernminecarts.entity.*;
import net.lordkipama.modernminecarts.inventory.CustomSmithingScreen;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.inventory.ModMenus;
import net.lordkipama.modernminecarts.renderer.CustomMinecartRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.lordkipama.modernminecarts.Proxy.ClientProxy;
import net.lordkipama.modernminecarts.Proxy.ServerProxy;

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
        ModMenus.register(modEventBus);

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

            event.accept(ModBlocks.POWERED_DETECTOR_RAIL);

            event.accept(ModBlocks.SLOPED_RAIL);

        }
        else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.COPPER_UPGRADE_SMITHING_TEMPLATE);
            event.accept(ModItems.CHIPPED_COPPER_UPGRADE_SMITHING_TEMPLATE);
            event.accept(ModItems.DAMAGED_COPPER_UPGRADE_SMITHING_TEMPLATE);
        }
        else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModBlocks.COPPER_RAIL);
            event.accept(ModBlocks.EXPOSED_COPPER_RAIL);
            event.accept(ModBlocks.WEATHERED_COPPER_RAIL);
            event.accept(ModBlocks.OXIDIZED_COPPER_RAIL);

            event.accept(ModBlocks.WAXED_COPPER_RAIL);
            event.accept(ModBlocks.WAXED_EXPOSED_COPPER_RAIL);
            event.accept(ModBlocks.WAXED_WEATHERED_COPPER_RAIL);
            event.accept(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL);

            event.accept(ModBlocks.RAIL_CROSSING);

            event.accept(ModBlocks.POWERED_DETECTOR_RAIL);
        }
    }


    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(VanillaEntities.MINECART_ENTITY.get(), new CustomMinecartEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.CHEST_MINECART_ENTITY.get(), new CustomMinecartChestEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.COMMAND_BLOCK_MINECART_ENTITY.get(), new CustomMinecartCommandBlockEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.FURNACE_MINECART_ENTITY.get(), new CustomMinecartFurnaceEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.HOPPER_MINECART_ENTITY.get(), new CustomMinecartHopperEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.SPAWNER_MINECART_ENTITY.get(), new CustomMinecartSpawnerEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.TNT_MINECART_ENTITY.get(), new CustomMinecartTNTEntityRenderFactory());
            event.enqueueWork(
                    () -> MenuScreens.register(ModMenus.CUSTOM_SMITHING_MENU.get(), CustomSmithingScreen::new)
            );
            event.enqueueWork(
                    () -> MenuScreens.register(ModMenus.FURNACE_MINECART_MENU.get(), FurnaceMinecartScreen::new)
            );

        }

        private static class CustomMinecartEntityRenderFactory implements EntityRendererProvider<CustomMinecartEntity> {
            @Override
            public EntityRenderer<CustomMinecartEntity> create(Context context) {
                return new CustomMinecartRenderer(context, ModelLayers.MINECART);
            }
        }

        private static class CustomMinecartChestEntityRenderFactory implements EntityRendererProvider<CustomMinecartChestEntity> {
            @Override
            public EntityRenderer<CustomMinecartChestEntity> create(Context context) {
                return new CustomMinecartRenderer(context, ModelLayers.CHEST_MINECART);
            }
        }

        private static class CustomMinecartCommandBlockEntityRenderFactory implements EntityRendererProvider<CustomMinecartCommandBlockEntity> {
            @Override
            public EntityRenderer<CustomMinecartCommandBlockEntity> create(Context context) {
                return new CustomMinecartRenderer(context, ModelLayers.COMMAND_BLOCK_MINECART);
            }
        }

        private static class CustomMinecartFurnaceEntityRenderFactory implements EntityRendererProvider<CustomMinecartFurnaceEntity> {
            @Override
            public EntityRenderer<CustomMinecartFurnaceEntity> create(Context context) {
                return new CustomMinecartRenderer(context, ModelLayers.FURNACE_MINECART);
            }
        }

        private static class CustomMinecartHopperEntityRenderFactory implements EntityRendererProvider<CustomMinecartHopperEntity> {
            @Override
            public EntityRenderer<CustomMinecartHopperEntity> create(Context context) {
                return new CustomMinecartRenderer(context, ModelLayers.HOPPER_MINECART);
            }
        }

        private static class CustomMinecartSpawnerEntityRenderFactory implements EntityRendererProvider<CustomMinecartSpawnerEntity> {
            @Override
            public EntityRenderer<CustomMinecartSpawnerEntity> create(Context context) {
                return new CustomMinecartRenderer(context, ModelLayers.SPAWNER_MINECART);
            }
        }

        private static class CustomMinecartTNTEntityRenderFactory implements EntityRendererProvider<CustomMinecartTNTEntity> {
            @Override
            public EntityRenderer<CustomMinecartTNTEntity> create(Context context) {
                return new CustomMinecartRenderer(context, ModelLayers.TNT_MINECART);
            }
        }
    }
}