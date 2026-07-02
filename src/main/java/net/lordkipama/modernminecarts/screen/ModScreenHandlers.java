package net.lordkipama.modernminecarts.screen;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModScreenHandlers {
    public static final MenuType<FurnaceMinecartScreenHandler> FURNACE_MINECART = Registry.register(
            BuiltInRegistries.MENU,
            ModernMinecarts.id("furnace_minecart"),
            new MenuType<>(FurnaceMinecartScreenHandler::new, FeatureFlags.VANILLA_SET)
    );

    private ModScreenHandlers() {
    }

    public static void register() {
        ModernMinecarts.LOGGER.info("Registering screen handlers for {}", ModernMinecarts.MOD_ID);
    }
}
