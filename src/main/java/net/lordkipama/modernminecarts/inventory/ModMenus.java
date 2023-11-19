package net.lordkipama.modernminecarts.inventory;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.entity.CustomMinecartTNTEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ModernMinecarts.MOD_ID);

    public static final RegistryObject<MenuType<CustomSmithingMenu>> CUSTOM_SMITHING_MENU =
            MENU_TYPES.register("custom_smithing_menu", ()-> new MenuType(CustomSmithingMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final RegistryObject<MenuType<FurnaceMinecartMenu>> FURNACE_MINECART_MENU =
            MENU_TYPES.register("furnace_minecart_menu", ()-> new MenuType(FurnaceMinecartMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
