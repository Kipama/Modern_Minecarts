package net.lordkipama.modernminecarts;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import java.lang.reflect.Method;
import net.lordkipama.modernminecarts.client.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.client.MinecartChainRenderer;
import net.lordkipama.modernminecarts.screen.ModScreenHandlers;
import net.minecraft.client.gui.screens.MenuScreens;

@Environment(EnvType.CLIENT)
public class ModernMinecartsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerMenuScreen();
        ClientPlayNetworking.registerGlobalReceiver(
                SyncChainedMinecartPacket.TYPE,
                (payload, context) -> context.client().execute(() -> {
                    if (context.client().level != null) {
                        payload.sync().apply(context.client().level);
                    }
                })
        );

        MinecartChainRenderer.register();
    }

    private static void registerMenuScreen() {
        try {
            Method register = MenuScreens.class.getDeclaredMethod(
                    "register",
                    net.minecraft.world.inventory.MenuType.class,
                    MenuScreens.ScreenConstructor.class
            );
            register.setAccessible(true);
            MenuScreens.ScreenConstructor<
                    net.lordkipama.modernminecarts.screen.FurnaceMinecartScreenHandler,
                    FurnaceMinecartScreen
                    > constructor = FurnaceMinecartScreen::new;
            register.invoke(null, ModScreenHandlers.FURNACE_MINECART, constructor);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register furnace minecart screen", exception);
        }
    }
}
