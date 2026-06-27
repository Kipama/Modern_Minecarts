package net.lordkipama.modernminecarts.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = ModernMinecarts.MOD_ID, value = Dist.CLIENT)
public final class MinecartChainRenderer {
    private MinecartChainRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        for (AbstractMinecart cart : minecraft.level.getEntitiesOfClass(AbstractMinecart.class, minecraft.gameRenderer.getMainCamera().getEntity().getBoundingBox().inflate(128.0D))) {
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

            int packedLight = LevelRenderer.getLightColor(minecraft.level, BlockPos.containing((startX + endX) * 0.5D, (startY + endY) * 0.5D, (startZ + endZ) * 0.5D));

            poseStack.pushPose();
            poseStack.translate(endX - cameraPos.x, endY - cameraPos.y, endZ - cameraPos.z);
            CustomMinecartRenderer.renderChain(distanceX, distanceY, distanceZ, (float) hAngle, (float) vAngle, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        }

        bufferSource.endBatch();
    }
}
