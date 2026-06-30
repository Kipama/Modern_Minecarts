package net.lordkipama.modernminecarts;

import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.Proxy.IProxy;
import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.Proxy.ClientProxy;
import net.lordkipama.modernminecarts.Proxy.ServerProxy;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.block.VanillaBlocks;
import net.lordkipama.modernminecarts.entity.*;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.inventory.ModMenus;
import net.lordkipama.modernminecarts.inventory.ModernMinecartsConfigScreen;
import net.lordkipama.modernminecarts.recipe.FeatureEnabledCondition;
import net.lordkipama.modernminecarts.recipe.ModRecipeSerializers;
import net.lordkipama.modernminecarts.renderer.CustomMinecartRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

@Mod(ModernMinecarts.MOD_ID)
public class ModernMinecarts {
    public static final String MOD_ID = "modernminecarts";
    public static IProxy PROXY = FMLEnvironment.dist == Dist.CLIENT ? new ClientProxy() : new ServerProxy();

    public ModernMinecarts() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModernMinecartsConfig.SPEC);
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new ModernMinecartsConfigScreen(parent))
        );

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModEntities.register(modEventBus);
        VanillaEntities.register(modEventBus);
        VanillaItems.register(modEventBus);
        VanillaBlocks.register(modEventBus);
        ModMenus.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);

        ModernMinecartsPacketHandler.Init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CraftingHelper.register(FeatureEnabledCondition.SERIALIZER));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            acceptCopperRailsIfEnabled(event);
            acceptIfEnabled(event, ModBlocks.RAIL_CROSSING, ModernMinecartsConfig.enableRailCrossing());
            acceptIfEnabled(event, ModBlocks.POWERED_DETECTOR_RAIL, ModernMinecartsConfig.enablePoweredDetectorRail());
            acceptIfEnabled(event, ModBlocks.SLOPED_RAIL, ModernMinecartsConfig.enableRailJump());
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            acceptCopperRailsIfEnabled(event);
            acceptIfEnabled(event, ModBlocks.RAIL_CROSSING, ModernMinecartsConfig.enableRailCrossing());
            acceptIfEnabled(event, ModBlocks.POWERED_DETECTOR_RAIL, ModernMinecartsConfig.enablePoweredDetectorRail());
        }
    }

    private static void acceptCopperRailsIfEnabled(BuildCreativeModeTabContentsEvent event) {
        boolean enabled = ModernMinecartsConfig.enableCopperRails();
        acceptIfEnabled(event, ModBlocks.COPPER_RAIL, enabled);
        acceptIfEnabled(event, ModBlocks.EXPOSED_COPPER_RAIL, enabled);
        acceptIfEnabled(event, ModBlocks.WEATHERED_COPPER_RAIL, enabled);
        acceptIfEnabled(event, ModBlocks.OXIDIZED_COPPER_RAIL, enabled);
        acceptIfEnabled(event, ModBlocks.WAXED_COPPER_RAIL, enabled);
        acceptIfEnabled(event, ModBlocks.WAXED_EXPOSED_COPPER_RAIL, enabled);
        acceptIfEnabled(event, ModBlocks.WAXED_WEATHERED_COPPER_RAIL, enabled);
        acceptIfEnabled(event, ModBlocks.WAXED_OXIDIZED_COPPER_RAIL, enabled);
    }

    private static void acceptIfEnabled(BuildCreativeModeTabContentsEvent event, RegistryObject<? extends Block> block, boolean enabled) {
        if (enabled) {
            event.accept(block);
        }
    }

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
            event.enqueueWork(() -> MenuScreens.register(ModMenus.FURNACE_MINECART_MENU.get(), FurnaceMinecartScreen::new));
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
