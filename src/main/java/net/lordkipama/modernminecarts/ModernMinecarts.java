package net.lordkipama.modernminecarts;

import com.mojang.logging.LogUtils;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.attachment.MinecartAttachmentTypes;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.entity.CustomMinecartChestEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartCommandBlockEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartFurnaceEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartHopperEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartSpawnerEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartTNTEntity;
import net.lordkipama.modernminecarts.entity.ModEntities;
import net.lordkipama.modernminecarts.entity.VanillaEntities;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.inventory.ModMenus;
import net.lordkipama.modernminecarts.renderer.CustomMinecartRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(ModernMinecarts.MOD_ID)
public class ModernMinecarts {
    public static final String MOD_ID = "modernminecarts";
    private static final Logger LOGGER = LogUtils.getLogger();
    /**
     * NeoForge 1.21 iterates built-in registries through dense ID-backed arrays during creative tab rebuilds and
     * registry validation. Replacing vanilla minecart entries in the minecraft namespace currently leaves null holes
     * there, so keep those overrides disabled until the port switches to a 1.21-safe replacement strategy.
     */
    public static final boolean ENABLE_VANILLA_NAMESPACE_OVERRIDES = false;

    public ModernMinecarts(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        MinecartAttachmentTypes.register(modEventBus);
        if (ENABLE_VANILLA_NAMESPACE_OVERRIDES) {
            VanillaEntities.register(modEventBus);
            VanillaItems.register(modEventBus);
        }
        ModMenus.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModernMinecartsPacketHandler::register);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        if (!ENABLE_VANILLA_NAMESPACE_OVERRIDES) {
            LOGGER.warn("Vanilla minecart item/entity overrides are disabled on the 1.21 port because minecraft namespace registry replacements currently crash creative tab rebuilding.");
        }
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
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
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

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            if (!ENABLE_VANILLA_NAMESPACE_OVERRIDES) {
                return;
            }

            EntityRenderers.register(VanillaEntities.MINECART_ENTITY.get(), new CustomMinecartEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.CHEST_MINECART_ENTITY.get(), new CustomMinecartChestEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.COMMAND_BLOCK_MINECART_ENTITY.get(), new CustomMinecartCommandBlockEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.FURNACE_MINECART_ENTITY.get(), new CustomMinecartFurnaceEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.HOPPER_MINECART_ENTITY.get(), new CustomMinecartHopperEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.SPAWNER_MINECART_ENTITY.get(), new CustomMinecartSpawnerEntityRenderFactory());
            EntityRenderers.register(VanillaEntities.TNT_MINECART_ENTITY.get(), new CustomMinecartTNTEntityRenderFactory());
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.FURNACE_MINECART_MENU.get(), FurnaceMinecartScreen::new);
        }

        private static class CustomMinecartEntityRenderFactory implements EntityRendererProvider<CustomMinecartEntity> {
            @Override
            public EntityRenderer<CustomMinecartEntity> create(Context context) {
                return new CustomMinecartRenderer<>(context, ModelLayers.MINECART);
            }
        }

        private static class CustomMinecartChestEntityRenderFactory implements EntityRendererProvider<CustomMinecartChestEntity> {
            @Override
            public EntityRenderer<CustomMinecartChestEntity> create(Context context) {
                return new CustomMinecartRenderer<>(context, ModelLayers.CHEST_MINECART);
            }
        }

        private static class CustomMinecartCommandBlockEntityRenderFactory implements EntityRendererProvider<CustomMinecartCommandBlockEntity> {
            @Override
            public EntityRenderer<CustomMinecartCommandBlockEntity> create(Context context) {
                return new CustomMinecartRenderer<>(context, ModelLayers.COMMAND_BLOCK_MINECART);
            }
        }

        private static class CustomMinecartFurnaceEntityRenderFactory implements EntityRendererProvider<CustomMinecartFurnaceEntity> {
            @Override
            public EntityRenderer<CustomMinecartFurnaceEntity> create(Context context) {
                return new CustomMinecartRenderer<>(context, ModelLayers.FURNACE_MINECART);
            }
        }

        private static class CustomMinecartHopperEntityRenderFactory implements EntityRendererProvider<CustomMinecartHopperEntity> {
            @Override
            public EntityRenderer<CustomMinecartHopperEntity> create(Context context) {
                return new CustomMinecartRenderer<>(context, ModelLayers.HOPPER_MINECART);
            }
        }

        private static class CustomMinecartSpawnerEntityRenderFactory implements EntityRendererProvider<CustomMinecartSpawnerEntity> {
            @Override
            public EntityRenderer<CustomMinecartSpawnerEntity> create(Context context) {
                return new CustomMinecartRenderer<>(context, ModelLayers.SPAWNER_MINECART);
            }
        }

        private static class CustomMinecartTNTEntityRenderFactory implements EntityRendererProvider<CustomMinecartTNTEntity> {
            @Override
            public EntityRenderer<CustomMinecartTNTEntity> create(Context context) {
                return new CustomMinecartRenderer<>(context, ModelLayers.TNT_MINECART);
            }
        }
    }
}
