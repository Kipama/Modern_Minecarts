package net.lordkipama.modernminecarts.inventory;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ModernMinecarts.MOD_ID);

    public static final RegistryObject<MenuType<FurnaceMinecartMenu>> FURNACE_MINECART_MENU =
            MENU_TYPES.register("furnace_minecart_menu", ()-> new MenuType(FurnaceMinecartMenu::new));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
