package net.lordkipama.modernminecarts.renderer;



import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.entity.CustomAbstractMinecartEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;

//The following code is based on the work of Cammie. The only changes have been porting it from fabric to forge.
/*Copyright (C) 2022 Cammie

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to use, copy, modify, and/or merge copies of the
Software, and to permit persons to whom the Software is furnished to do so, subject to the following
restrictions:

 1) The above copyright notice and this permission notice shall be included in all copies or substantial
    portions of the Software.
 2) You include attribution to the copyright holder(s) in public display of any project that uses any
    portion of the Software.
 3) You may not publish or distribute substantial portions of the Software in its compiled or uncompiled
    forms without prior permission from the copyright holder.
 4) The Software does not make up a substantial portion of your own projects.
*
* */
@OnlyIn(Dist.CLIENT)
public class CustomMinecartRenderer<T extends CustomAbstractMinecartEntity> extends EntityRenderer<T> {
    private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
    private static final ResourceLocation CHAIN_LOCATION = new ResourceLocation(ModernMinecarts.MOD_ID, "textures/entity/chain.png");
    private static final RenderType CHAIN_TYPE = RenderType.entityCutoutNoCull(CHAIN_LOCATION); //entityCutoutNoCull


    protected final EntityModel<T> model;
    private final BlockRenderDispatcher blockRenderer;

    public CustomMinecartRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pLayer) {
        super(pContext);
        this.shadowRadius = 0.7F;
        this.model = new MinecartModel<>(pContext.bakeLayer(pLayer));
        this.blockRenderer = pContext.getBlockRenderDispatcher();
    }

    public void render(@NotNull T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
        pMatrixStack.pushPose();
        long i = (long) pEntity.getId() * 493286711L;
        i = i * i * 4392167121L + i * 98761L;
        float f = (((float) (i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f1 = (((float) (i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f2 = (((float) (i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        pMatrixStack.translate(f, f1, f2);
        double d0 = Mth.lerp((double) pPartialTicks, pEntity.xOld, pEntity.getX());
        double d1 = Mth.lerp((double) pPartialTicks, pEntity.yOld, pEntity.getY());
        double d2 = Mth.lerp((double) pPartialTicks, pEntity.zOld, pEntity.getZ());
        double d3 = (double) 0.3F;
        Vec3 vec3 = pEntity.getPos(d0, d1, d2);
        float f3 = Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot());
        if (vec3 != null) {
            Vec3 vec31 = pEntity.getPosOffs(d0, d1, d2, (double) 0.3F);
            Vec3 vec32 = pEntity.getPosOffs(d0, d1, d2, (double) -0.3F);
            if (vec31 == null) {
                vec31 = vec3;
            }

            if (vec32 == null) {
                vec32 = vec3;
            }

            pMatrixStack.translate(vec3.x - d0, (vec31.y + vec32.y) / 2.0D - d1, vec3.z - d2);
            Vec3 vec33 = vec32.add(-vec31.x, -vec31.y, -vec31.z);
            if (vec33.length() != 0.0D) {
                vec33 = vec33.normalize();
                pEntityYaw = (float) (Math.atan2(vec33.z, vec33.x) * 180.0D / Math.PI);
                f3 = (float) (Math.atan(vec33.y) * 73.0D);
            }
        }

        pMatrixStack.translate(0.0F, 0.375F, 0.0F);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pEntityYaw));
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(-f3));
        float f5 = (float) pEntity.getHurtTime() - pPartialTicks;
        float f6 = pEntity.getDamage() - pPartialTicks;
        if (f6 < 0.0F) {
            f6 = 0.0F;
        }

        if (f5 > 0.0F) {
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(Mth.sin(f5) * f5 * f6 / 10.0F * (float) pEntity.getHurtDir()));
        }

        int j = pEntity.getDisplayOffset();
        BlockState blockstate = pEntity.getDisplayBlockState();
        if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
            pMatrixStack.pushPose();
            float f4 = 0.75F;
            pMatrixStack.scale(0.75F, 0.75F, 0.75F);
            pMatrixStack.translate(-0.5F, (float) (j - 8) / 16.0F, 0.5F);
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
            this.renderMinecartContents(pEntity, pPartialTicks, blockstate, pMatrixStack, pBuffer, pPackedLight);
            pMatrixStack.popPose();
        }

        pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(pEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.model.renderType(this.getTextureLocation(pEntity)));
        this.model.renderToBuffer(pMatrixStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pMatrixStack.popPose();

        CustomAbstractMinecartEntity parent = pEntity.getLinkedParent();

        if (parent != null) {
            double startX = parent.getX();
            double startY = parent.getY();
            double startZ = parent.getZ();
            double endX = pEntity.getX();
            double endY = pEntity.getY();
            double endZ = pEntity.getZ();

            float distanceX = (float) (startX - endX);
            float distanceY = (float) (startY - endY);
            float distanceZ = (float) (startZ - endZ);
            float distance = pEntity.distanceTo(parent);

            double hAngle = Math.toDegrees(Math.atan2(endZ - startZ, endX - startX));
            hAngle += Math.ceil(-hAngle / 360) * 360;

            double vAngle = Math.asin(distanceY / distance);

            renderChain(distanceX, distanceY, distanceZ, (float) hAngle, (float) vAngle, pMatrixStack, pBuffer, pPackedLight);
        }

    }

    /**
     * Returns the location of an entity's texture.
     */
    public ResourceLocation getTextureLocation(T pEntity) {
        return MINECART_LOCATION;
    }

    protected void renderMinecartContents(T pEntity, float pPartialTicks, BlockState pState, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        this.blockRenderer.renderSingleBlock(pState, pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
    }

    public void renderChain(float x, float y, float z, float hAngle, float vAngle, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {

        float squaredLength = x * x + y * y + z * z;
        float length = (float)Math.sqrt(squaredLength) - 1F;



        pMatrixStack.pushPose();
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-hAngle - 90));
        pMatrixStack.mulPose(Vector3f.XP.rotation(-vAngle));
        pMatrixStack.translate(0, 0, 0.5);
        pMatrixStack.pushPose();

        VertexConsumer vertexConsumer = pBuffer.getBuffer(CHAIN_TYPE);

        float vertX1 = 0F;
        float vertY1 = 0.25F;
        float vertX2 = (float)Math.sin(6.2831855F) * 0.125F;
        float vertY2 = (float)Math.cos(6.2831855F) * 0.125F;
        float minU = 0F;
        float maxU = 0.1875F;
        float minV = 0F;
        float maxV = length / 10;
        PoseStack.Pose entry = pMatrixStack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        vertexConsumer.vertex(matrix4f, vertX1, vertY1, 0F).color(0, 0, 0, 255).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX1, vertY1, length).color(255, 255, 255, 255).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX2, vertY2, length).color(255, 255, 255, 255).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX2, vertY2, 0F).color(0, 0, 0, 255).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();


        pMatrixStack.popPose();
        pMatrixStack.translate(0.19, 0.19, 0);
        pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90));

        entry = pMatrixStack.last();//.peek()
        matrix4f = entry.pose();
        matrix3f = entry.normal();


        //pMatrixStack.popPose();
        vertexConsumer.vertex(matrix4f, vertX1, vertY1, 0F).color(0, 0, 0, 255).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX1, vertY1, length).color(255, 255, 255, 255).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX2, vertY2, length).color(255, 255, 255, 255).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX2, vertY2, 0F).color(0, 0, 0, 255).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(pPackedLight).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        //this.model.renderToBuffer(pMatrixStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        pMatrixStack.popPose();
    }
}
