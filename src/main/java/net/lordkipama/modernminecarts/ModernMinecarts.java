package net.lordkipama.modernminecarts;

import com.mojang.logging.LogUtils;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.attachment.MinecartAttachmentTypes;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.inventory.ModMenus;
import net.lordkipama.modernminecarts.inventory.ModernMinecartsConfigScreen;
import net.lordkipama.modernminecarts.recipe.ModConditionSerializers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.slf4j.Logger;

@Mod(ModernMinecarts.MOD_ID)
public class ModernMinecarts {
    public static final String MOD_ID = "modernminecarts";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ModernMinecarts(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ModernMinecartsConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (IConfigScreenFactory) (container, modListScreen) -> new ModernMinecartsConfigScreen(modListScreen));

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        MinecartAttachmentTypes.register(modEventBus);
        ModMenus.register(modEventBus);
        ModConditionSerializers.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(ModernMinecartsPacketHandler::register);

        LOGGER.info("Modern Minecarts is running on the NeoForge 1.21 architecture without legacy vanilla namespace minecart overrides.");

        modEventBus.addListener(ClientModEvents::registerMenuScreens);
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

    private static void acceptIfEnabled(BuildCreativeModeTabContentsEvent event, DeferredBlock<? extends Block> block, boolean enabled) {
        if (enabled) {
            event.accept(block);
        }
    }

    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenus.FURNACE_MINECART_MENU.get(), FurnaceMinecartScreen::new);
        }
    }
}
