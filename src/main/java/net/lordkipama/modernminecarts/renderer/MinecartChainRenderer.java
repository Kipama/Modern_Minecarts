package net.lordkipama.modernminecarts.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = ModernMinecarts.MOD_ID, value = Dist.CLIENT)
public final class MinecartChainRenderer {
    private static final Identifier CHAIN_LOCATION = Identifier.fromNamespaceAndPath(ModernMinecarts.MOD_ID, "textures/entity/chain.png");
    private static final RenderType CHAIN_TYPE = RenderTypes.entityCutout(CHAIN_LOCATION);

    private MinecartChainRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterOpaqueFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        AABB renderBounds = new AABB(cameraPos, cameraPos).inflate(128.0D);

        for (AbstractMinecart cart : minecraft.level.getEntitiesOfClass(AbstractMinecart.class, renderBounds)) {
            AbstractMinecart parent = MinecartLinkHelper.getLinkedParent(cart);
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

            int packedLight = LevelRenderer.getLightCoords(minecraft.level, BlockPos.containing((startX + endX) * 0.5D, (startY + endY) * 0.5D, (startZ + endZ) * 0.5D));

            poseStack.pushPose();
            poseStack.translate(endX - cameraPos.x, endY - cameraPos.y, endZ - cameraPos.z);
            renderChain(distanceX, distanceY, distanceZ, (float) hAngle, (float) vAngle, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    public static void renderChain(float x, float y, float z, float hAngle, float vAngle, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        float squaredLength = x * x + y * y + z * z;
        float length = (float) Math.sqrt(squaredLength) - 1F;

        matrixStack.pushPose();
        matrixStack.mulPose(Axis.YP.rotationDegrees(-hAngle - 90));
        matrixStack.mulPose(Axis.XP.rotation(-vAngle));
        matrixStack.translate(0, 0, 0.5);
        matrixStack.pushPose();

        VertexConsumer vertexConsumer = buffer.getBuffer(CHAIN_TYPE);
        float vertX1 = 0F;
        float vertY1 = 0.25F;
        float vertX2 = (float) Math.sin(6.2831855F) * 0.125F;
        float vertY2 = (float) Math.cos(6.2831855F) * 0.125F;
        float minU = 0F;
        float maxU = 0.1875F;
        float minV = 0F;
        float maxV = length / 10;
        PoseStack.Pose entry = matrixStack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX1, vertY1, 0F, 0, 0, 0, 255, minU, minV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX1, vertY1, length, 255, 255, 255, 255, minU, maxV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX2, vertY2, length, 255, 255, 255, 255, maxU, maxV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX2, vertY2, 0F, 0, 0, 0, 255, maxU, minV, packedLight);

        matrixStack.popPose();
        matrixStack.translate(0.19, 0.19, 0);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(90));

        entry = matrixStack.last();
        matrix4f = entry.pose();
        matrix3f = entry.normal();

        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX1, vertY1, 0F, 0, 0, 0, 255, minU, minV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX1, vertY1, length, 255, 255, 255, 255, minU, maxV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX2, vertY2, length, 255, 255, 255, 255, maxU, maxV, packedLight);
        addChainVertex(vertexConsumer, matrix4f, matrix3f, vertX2, vertY2, 0F, 0, 0, 0, 255, maxU, minV, packedLight);

        matrixStack.popPose();
    }

    private static void addChainVertex(VertexConsumer vertexConsumer, Matrix4f pose, Matrix3f normalMatrix, float x, float y, float z, int red, int green, int blue, int alpha, float u, float v, int packedLight) {
        vertexConsumer.addVertex(pose, x, y, z)
                .setColor(red, green, blue, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, -1.0F, 0.0F);
    }
}
