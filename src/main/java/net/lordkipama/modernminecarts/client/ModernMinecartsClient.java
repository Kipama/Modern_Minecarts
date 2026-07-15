package net.lordkipama.modernminecarts.client;

import net.lordkipama.modernminecarts.inventory.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.inventory.ModMenus;
import net.lordkipama.modernminecarts.inventory.ModernMinecartsConfigScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class ModernMinecartsClient {
    private ModernMinecartsClient() {
    }

    public static void register(ModContainer modContainer, IEventBus modEventBus) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (container, modListScreen) -> new ModernMinecartsConfigScreen(modListScreen));
        modEventBus.addListener(ModernMinecartsClient::registerMenuScreens);
    }

    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.FURNACE_MINECART_MENU.get(), FurnaceMinecartScreen::new);
    }
}