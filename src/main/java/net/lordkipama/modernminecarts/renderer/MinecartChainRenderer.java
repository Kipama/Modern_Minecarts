package net.lordkipama.modernminecarts.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;

@EventBusSubscriber(modid = ModernMinecarts.MOD_ID, value = Dist.CLIENT)
public final class MinecartChainRenderer {
    private static final Identifier CHAIN_LOCATION = Identifier.fromNamespaceAndPath(ModernMinecarts.MOD_ID, "textures/entity/chain.png");

    private MinecartChainRenderer() {
    }

    @SubscribeEvent
    public static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getLevelRenderState().cameraRenderState.pos;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        AABB renderBounds = new AABB(cameraPos, cameraPos).inflate(128.0D);

        for (AbstractMinecart cart : minecraft.level.getEntitiesOfClass(AbstractMinecart.class, renderBounds)) {
            AbstractMinecart parent = MinecartLinkHelper.getLinkedParent(cart);
            if (parent == null) {
                continue;
            }

            float startX = (float) (Mth.lerp(partialTick, parent.xOld, parent.getX()) - cameraPos.x);
            float startY = (float) (Mth.lerp(partialTick, parent.yOld, parent.getY()) - cameraPos.y);
            float startZ = (float) (Mth.lerp(partialTick, parent.zOld, parent.getZ()) - cameraPos.z);
            float endX = (float) (Mth.lerp(partialTick, cart.xOld, cart.getX()) - cameraPos.x);
            float endY = (float) (Mth.lerp(partialTick, cart.yOld, cart.getY()) - cameraPos.y);
            float endZ = (float) (Mth.lerp(partialTick, cart.zOld, cart.getZ()) - cameraPos.z);
            float distanceX = startX - endX;
            float distanceY = startY - endY;
            float distanceZ = startZ - endZ;
            float distance = Mth.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
            if (distance <= 0.001F) {
                continue;
            }

            double horizontalAngle = Math.toDegrees(Math.atan2(endZ - startZ, endX - startX));
            horizontalAngle += Math.ceil(-horizontalAngle / 360.0D) * 360.0D;
            double verticalAngle = Math.asin(distanceY / distance);
            int packedLight = LightCoordsUtil.getLightCoords(minecraft.level, BlockPos.containing((parent.getX() + cart.getX()) * 0.5D, (parent.getY() + cart.getY()) * 0.5D, (parent.getZ() + cart.getZ()) * 0.5D));

            submitChain(event, poseStack, endX, endY, endZ, distanceX, distanceY, distanceZ, (float) horizontalAngle, (float) verticalAngle, packedLight);
        }
    }

    private static void submitChain(
        SubmitCustomGeometryEvent event,
        PoseStack poseStack,
        float endX,
        float endY,
        float endZ,
        float distanceX,
        float distanceY,
        float distanceZ,
        float horizontalAngle,
        float verticalAngle,
        int packedLight
    ) {
        float squaredLength = distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ;
        float length = Mth.sqrt(squaredLength) - 1.0F;

        poseStack.pushPose();
        poseStack.translate(endX, endY, endZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(-horizontalAngle - 90.0F));
        poseStack.mulPose(Axis.XP.rotation(-verticalAngle));
        poseStack.translate(0.0F, 0.0F, 0.5F);
        submitChainPlane(event, poseStack, length, packedLight);
        poseStack.translate(0.19F, 0.19F, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        submitChainPlane(event, poseStack, length, packedLight);
        poseStack.popPose();
    }

    private static void submitChainPlane(SubmitCustomGeometryEvent event, PoseStack poseStack, float length, int packedLight) {
        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.entityCutout(CHAIN_LOCATION), (pose, buffer) -> {
            float vertX1 = 0.0F;
            float vertY1 = 0.25F;
            float vertX2 = (float) Math.sin(6.2831855F) * 0.125F;
            float vertY2 = (float) Math.cos(6.2831855F) * 0.125F;
            float minU = 0.0F;
            float maxU = 0.1875F;
            float minV = 0.0F;
            float maxV = length / 10.0F;

            addChainVertex(buffer, pose, vertX1, vertY1, 0.0F, 0, 0, 0, 255, minU, minV, packedLight);
            addChainVertex(buffer, pose, vertX1, vertY1, length, 255, 255, 255, 255, minU, maxV, packedLight);
            addChainVertex(buffer, pose, vertX2, vertY2, length, 255, 255, 255, 255, maxU, maxV, packedLight);
            addChainVertex(buffer, pose, vertX2, vertY2, 0.0F, 0, 0, 0, 255, maxU, minV, packedLight);
        });
    }

    private static void addChainVertex(
        VertexConsumer vertexConsumer,
        PoseStack.Pose pose,
        float x,
        float y,
        float z,
        int red,
        int green,
        int blue,
        int alpha,
        float u,
        float v,
        int packedLight
    ) {
        vertexConsumer.addVertex(pose, x, y, z)
            .setColor(red, green, blue, alpha)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(packedLight)
            .setNormal(pose, 0.0F, -1.0F, 0.0F);
    }
}
