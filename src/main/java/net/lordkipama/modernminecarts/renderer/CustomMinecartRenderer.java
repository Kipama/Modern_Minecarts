package net.lordkipama.modernminecarts.renderer;


import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class CustomMinecartRenderer extends MinecartRenderer {


    public CustomMinecartRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pLayer) {
        super(pContext, pLayer);
    }
}