package net.lordkipama.modernminecarts.inventory;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModScreens {

    public static void register() {
        MenuScreens.register(ModMenus.CUSTOM_SMITHING_MENU.get(), CustomSmithingScreen::new);
        // Doesn't work because ModMenus.REFINERY_FURNACE is not initialized until registry.
    }

}
