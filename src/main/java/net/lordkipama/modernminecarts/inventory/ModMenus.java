package net.lordkipama.modernminecarts.inventory;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, ModernMinecarts.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<FurnaceMinecartMenu>> FURNACE_MINECART_MENU =
            MENU_TYPES.register("furnace_minecart_menu", () -> new MenuType<>(FurnaceMinecartMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
