package net.lordkipama.modernminecarts;


import net.lordkipama.modernminecarts.client.ModernMinecartsClient;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.Proxy.IProxy;
import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.block.VanillaBlocks;
import net.lordkipama.modernminecarts.entity.*;
import net.lordkipama.modernminecarts.inventory.ModMenus;
import net.lordkipama.modernminecarts.recipe.FeatureEnabledCondition;
import net.lordkipama.modernminecarts.recipe.ModRecipeSerializers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.lordkipama.modernminecarts.Proxy.ClientProxy;
import net.lordkipama.modernminecarts.Proxy.ServerProxy;

@Mod(net.lordkipama.modernminecarts.ModernMinecarts.MOD_ID)
public class ModernMinecarts {
    public static final String MOD_ID = "modernminecarts";
    public static IProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
    public ModernMinecarts() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModernMinecartsConfig.SPEC);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ModernMinecartsClient::register);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModEntities.register(modEventBus);
        VanillaEntities.register(modEventBus);
        VanillaItems.register(modEventBus);
        VanillaBlocks.register(modEventBus);
        ModMenus.register(modEventBus);

        modEventBus.addListener(this::commonSetup);


        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addItemsToTabs);

        ModernMinecartsPacketHandler.Init();

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CraftingHelper.register(FeatureEnabledCondition.SERIALIZER));
    }

    private void addItemsToTabs(CreativeModeTabEvent.BuildContents event)
    {
        if (event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS) {
            acceptCopperRailsIfEnabled(event);

            acceptIfEnabled(event, ModBlocks.RAIL_CROSSING, ModernMinecartsConfig.enableRailCrossing());
            acceptIfEnabled(event, ModBlocks.POWERED_DETECTOR_RAIL, ModernMinecartsConfig.enablePoweredDetectorRail());
            acceptIfEnabled(event, ModBlocks.SLOPED_RAIL, ModernMinecartsConfig.enableRailJump());

        }
        else if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            acceptCopperRailsIfEnabled(event);

            acceptIfEnabled(event, ModBlocks.RAIL_CROSSING, ModernMinecartsConfig.enableRailCrossing());
            acceptIfEnabled(event, ModBlocks.POWERED_DETECTOR_RAIL, ModernMinecartsConfig.enablePoweredDetectorRail());
        }
    }

    private static void acceptCopperRailsIfEnabled(CreativeModeTabEvent.BuildContents event) {
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

    private static void acceptIfEnabled(CreativeModeTabEvent.BuildContents event, net.minecraftforge.registries.RegistryObject<? extends net.minecraft.world.level.block.Block> block, boolean enabled) {
        if (enabled) {
            event.accept(block);
        }
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
}
