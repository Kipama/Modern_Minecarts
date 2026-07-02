package net.lordkipama.modernminecarts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class MinecartChainRenderer {
    private static final Identifier CHAIN_LOCATION = ModernMinecarts.id("textures/entity/chain.png");
    private static final RenderType CHAIN_TYPE = RenderTypes.entityCutout(CHAIN_LOCATION);

    private MinecartChainRenderer() {
    }

    public static void register() {
        LevelRenderEvents.AFTER_SOLID_FEATURES.register(MinecartChainRenderer::render);
    }

    private static void render(LevelRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = context.bufferSource();
        PoseStack poseStack = context.poseStack();
        Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().position();
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        AABB renderBounds = new AABB(cameraPos, cameraPos).inflate(128.0D);

        for (AbstractMinecart cart : minecraft.level.getEntitiesOfClass(AbstractMinecart.class, renderBounds)) {
            AbstractMinecart parent = ((ChainMinecartInterface) cart).getLinkedParent();
            if (parent == null) {
                continue;
            }

            double startX = Mth.lerp(partialTick, parent.xOld, parent.getX());
            double startY = Mth.lerp(partialTick, parent.yOld, parent.getY());
            double startZ = Mth.lerp(partialTick, parent.zOld, parent.getZ());
            double endX = Mth.lerp(partialTick, cart.xOld, cart.getX());
            double endY = Mth.lerp(partialTick, cart.yOld, cart.getY());
            double endZ = Mth.lerp(partialTick, cart.zOld, cart.getZ());

            float distanceX = (float) (startX - endX);
            float distanceY = (float) (startY - endY);
            float distanceZ = (float) (startZ - endZ);
            float distance = (float) Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
            if (distance <= 0.001F) {
                continue;
            }

            double hAngle = Math.toDegrees(Math.atan2(endZ - startZ, endX - startX));
            hAngle += Math.ceil(-hAngle / 360.0D) * 360.0D;
            double vAngle = Math.asin(distanceY / distance);
            int packedLight = LevelRenderer.getLightCoords(
                    minecraft.level,
                    BlockPos.containing((startX + endX) * 0.5D, (startY + endY) * 0.5D, (startZ + endZ) * 0.5D)
            );

            poseStack.pushPose();
            poseStack.translate(endX - cameraPos.x, endY - cameraPos.y, endZ - cameraPos.z);
            renderChain(distanceX, distanceY, distanceZ, (float) hAngle, (float) vAngle, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }
    }

    private static void renderChain(float x, float y, float z, float hAngle, float vAngle, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float length = (float) Math.sqrt(x * x + y * y + z * z) - 1.0F;
        if (length <= 0.0F) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-hAngle - 90.0F));
        poseStack.mulPose(Axis.XP.rotation(-vAngle));
        poseStack.translate(0.0D, 0.0D, 0.5D);
        poseStack.pushPose();

        VertexConsumer vertexConsumer = buffer.getBuffer(CHAIN_TYPE);
        float vertX1 = 0.0F;
        float vertY1 = 0.25F;
        float vertX2 = (float) Math.sin(Math.PI * 2.0D) * 0.125F;
        float vertY2 = (float) Math.cos(Math.PI * 2.0D) * 0.125F;
        float minU = 0.0F;
        float maxU = 0.1875F;
        float minV = 0.0F;
        float maxV = length / 10.0F;
        PoseStack.Pose entry = poseStack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX1, vertY1, 0.0F, 0, 0, 0, 255, minU, minV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX1, vertY1, length, 255, 255, 255, 255, minU, maxV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX2, vertY2, length, 255, 255, 255, 255, maxU, maxV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX2, vertY2, 0.0F, 0, 0, 0, 255, maxU, minV, packedLight);

        poseStack.popPose();
        poseStack.translate(0.19D, 0.19D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));

        entry = poseStack.last();
        matrix4f = entry.pose();
        matrix3f = entry.normal();

        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX1, vertY1, 0.0F, 0, 0, 0, 255, minU, minV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX1, vertY1, length, 255, 255, 255, 255, minU, maxV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX2, vertY2, length, 255, 255, 255, 255, maxU, maxV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX2, vertY2, 0.0F, 0, 0, 0, 255, maxU, minV, packedLight);

        poseStack.popPose();
    }

    private static void addChainVertex(VertexConsumer vertexConsumer, Matrix4f pose, Matrix3f normalMatrix, float x, float y, float z, int red, int green, int blue, int alpha, float u, float v, int packedLight) {
        Objects.requireNonNull(normalMatrix);
        vertexConsumer.addVertex(pose, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, -1.0F, 0.0F);
    }
}
