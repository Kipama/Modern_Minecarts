package net.lordkipama.modernminecarts;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.client.FurnaceMinecartScreen;
import net.lordkipama.modernminecarts.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;

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

        BlockRenderLayerMap.putBlocks(
                BlockRenderLayer.CUTOUT,
                ModBlocks.COPPER_RAIL,
                ModBlocks.EXPOSED_COPPER_RAIL,
                ModBlocks.WEATHERED_COPPER_RAIL,
                ModBlocks.OXIDIZED_COPPER_RAIL,
                ModBlocks.WAXED_COPPER_RAIL,
                ModBlocks.WAXED_EXPOSED_COPPER_RAIL,
                ModBlocks.WAXED_WEATHERED_COPPER_RAIL,
                ModBlocks.WAXED_OXIDIZED_COPPER_RAIL,
                ModBlocks.RAIL_CROSSING,
                ModBlocks.RAIL_JUMP,
                ModBlocks.POWERED_DETECTOR_RAIL
        );
    }
}
