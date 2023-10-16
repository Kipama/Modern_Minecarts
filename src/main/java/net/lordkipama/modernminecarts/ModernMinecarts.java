package net.lordkipama.modernminecarts;

import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.block.VanillaBlocks;
import net.lordkipama.modernminecarts.entity.CustomMinecartEntity;
import net.lordkipama.modernminecarts.entity.ModEntities;
import net.lordkipama.modernminecarts.entity.VanillaEntities;
import net.lordkipama.modernminecarts.renderer.CustomMinecartRenderer;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(net.lordkipama.modernminecarts.ModernMinecarts.MOD_ID)
public class ModernMinecarts {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "modernminecarts";
    // Directly reference a slf4j logger

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
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(VanillaEntities.MINECART_ENTITY.get(), new CustomMinecartEntityRenderFactory());
        }

        private static class CustomMinecartEntityRenderFactory implements EntityRendererProvider<CustomMinecartEntity> {
            @Override
            public EntityRenderer<CustomMinecartEntity> create(Context context) {
                return new CustomMinecartRenderer(context, ModelLayers.MINECART);
            }
        }

    }
}