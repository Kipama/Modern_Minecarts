package net.lordkipama.modernminecarts;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.lordkipama.modernminecarts.client.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class ModernMinecartsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.FURNACE_MINECART, FurnaceMinecartScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(
                SyncChainedMinecartPacket.ID,
                (payload, context) -> context.client().execute(() -> {
                    if (context.client().world != null) {
                        payload.sync().apply(context.client().world);
                    }
                })
        );

    }
}
