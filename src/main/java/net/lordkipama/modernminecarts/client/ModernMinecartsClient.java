package net.lordkipama.modernminecarts.client;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.entity.*;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.inventory.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens; net.lordkipama.modernminecarts.renderer.CustomMinecartRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ModernMinecartsClient {
    private ModernMinecartsClient() {
    }
    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new ModernMinecartsConfigScreen(parent)));
    }

    @Mod.EventBusSubscriber(modid = ModernMinecarts.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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