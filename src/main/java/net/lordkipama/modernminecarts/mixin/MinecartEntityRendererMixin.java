package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.AbstractMinecartEntityRenderer;
import net.minecraft.client.render.entity.state.MinecartEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.WeakHashMap;

@Mixin(AbstractMinecartEntityRenderer.class)
public abstract class MinecartEntityRendererMixin {
    @Unique
    private static final Identifier CHAIN_TEXTURE =
            ModernMinecarts.id("textures/entity/chain.png");
    @Unique
    private static final RenderLayer CHAIN_LAYER =
            RenderLayers.entitySmoothCutout(CHAIN_TEXTURE);
    @Unique
    private static final Map<MinecartEntityRenderState, Vec3d> CHAIN_OFFSETS =
            new WeakHashMap<>();

    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void modernminecarts$updateChainState(
            AbstractMinecartEntity child,
            MinecartEntityRenderState state,
            float tickDelta,
            CallbackInfo ci
    ) {
        AbstractMinecartEntity parent = ((ChainMinecartInterface) child).getLinkedParent();
        if (parent == null) {
            CHAIN_OFFSETS.remove(state);
            return;
        }

        CHAIN_OFFSETS.put(
                state,
                parent.getLerpedPos(tickDelta).subtract(child.getLerpedPos(tickDelta))
        );
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void modernminecarts$renderChain(
            MinecartEntityRenderState state,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            CameraRenderState cameraState,
            CallbackInfo ci
    ) {
        Vec3d offset = CHAIN_OFFSETS.get(state);
        if (offset == null || offset.lengthSquared() < 1.0E-6D) {
            return;
        }

        float x = (float) offset.x;
        float y = (float) offset.y;
        float z = (float) offset.z;
        float distance = MathHelper.sqrt(x * x + y * y + z * z);
        float horizontalAngle = (float) Math.toDegrees(Math.atan2(-z, -x));
        float verticalAngle = (float) Math.asin(y / distance);
        modernminecarts$submitChain(
                x,
                y,
                z,
                horizontalAngle,
                verticalAngle,
                matrices,
                queue,
                state.light
        );
    }

    @Unique
    private static void modernminecarts$submitChain(
            float x,
            float y,
            float z,
            float horizontalAngle,
            float verticalAngle,
            MatrixStack matrices,
            OrderedRenderCommandQueue queue,
            int light
    ) {
        float length = MathHelper.sqrt(x * x + y * y + z * z) - 1.0F;
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-horizontalAngle - 90.0F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(-verticalAngle));
        matrices.translate(0.0F, 0.0F, 0.5F);

        queue.submitCustom(
                matrices,
                CHAIN_LAYER,
                (entry, vertices) -> modernminecarts$drawChainPlane(vertices, entry, length, light)
        );

        matrices.translate(0.19F, 0.19F, 0.0F);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(90.0F));
        queue.submitCustom(
                matrices,
                CHAIN_LAYER,
                (entry, vertices) -> modernminecarts$drawChainPlane(vertices, entry, length, light)
        );
        matrices.pop();
    }

    @Unique
    private static void modernminecarts$drawChainPlane(
            VertexConsumer vertices,
            MatrixStack.Entry entry,
            float length,
            int light
    ) {
        float x1 = 0.0F;
        float y1 = 0.25F;
        float x2 = 0.0F;
        float y2 = 0.125F;
        float maxV = length / 10.0F;

        modernminecarts$vertex(vertices, entry, x1, y1, 0.0F, 0, 0, 0, 0.0F, 0.0F, light);
        modernminecarts$vertex(vertices, entry, x1, y1, length, 255, 255, 255, 0.0F, maxV, light);
        modernminecarts$vertex(vertices, entry, x2, y2, length, 255, 255, 255, 0.1875F, maxV, light);
        modernminecarts$vertex(vertices, entry, x2, y2, 0.0F, 0, 0, 0, 0.1875F, 0.0F, light);
    }

    @Unique
    private static void modernminecarts$vertex(
            VertexConsumer vertices,
            MatrixStack.Entry entry,
            float x,
            float y,
            float z,
            int red,
            int green,
            int blue,
            float u,
            float v,
            int light
    ) {
        vertices.vertex(entry, x, y, z)
                .color(red, green, blue, 255)
                .texture(u, v)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(entry, 0.0F, -1.0F, 0.0F);
    }
}
