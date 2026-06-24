package net.lordkipama.modernminecarts.screen;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public final class ModScreenHandlers {
    public static final ScreenHandlerType<FurnaceMinecartScreenHandler> FURNACE_MINECART =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    ModernMinecarts.id("furnace_minecart"),
                    new ScreenHandlerType<>(FurnaceMinecartScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
            );

    private ModScreenHandlers() {
    }

    public static void register() {
        ModernMinecarts.LOGGER.info("Registering screen handlers for {}", ModernMinecarts.MOD_ID);
    }
}
