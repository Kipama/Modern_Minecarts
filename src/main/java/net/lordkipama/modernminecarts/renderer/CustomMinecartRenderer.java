package net.lordkipama.modernminecarts.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class CustomMinecartRenderer<T extends CustomAbstractMinecartEntity> extends EntityRenderer<T> {
    private static final ResourceLocation MINECART_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/minecart.png");
    private static final ResourceLocation CHAIN_LOCATION = ResourceLocation.fromNamespaceAndPath(ModernMinecarts.MOD_ID, "textures/entity/chain.png");
    private static final RenderType CHAIN_TYPE = RenderType.entityCutoutNoCull(CHAIN_LOCATION);

    protected final EntityModel<T> model;
    private final BlockRenderDispatcher blockRenderer;

    public CustomMinecartRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer) {
        super(context);
        this.shadowRadius = 0.7F;
        this.model = new MinecartModel<>(context.bakeLayer(layer));
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
        matrixStack.pushPose();
        long i = (long) entity.getId() * 493286711L;
        i = i * i * 4392167121L + i * 98761L;
        float f = (((float) (i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f1 = (((float) (i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float f2 = (((float) (i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        matrixStack.translate(f, f1, f2);
        double d0 = Mth.lerp((double) partialTicks, entity.xOld, entity.getX());
        double d1 = Mth.lerp((double) partialTicks, entity.yOld, entity.getY());
        double d2 = Mth.lerp((double) partialTicks, entity.zOld, entity.getZ());
        Vec3 vec3 = entity.getPos(d0, d1, d2);
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        if (vec3 != null) {
            Vec3 vec31 = entity.getPosOffs(d0, d1, d2, 0.3F);
            Vec3 vec32 = entity.getPosOffs(d0, d1, d2, -0.3F);
            if (vec31 == null) {
                vec31 = vec3;
            }
            if (vec32 == null) {
                vec32 = vec3;
            }

            matrixStack.translate(vec3.x - d0, (vec31.y + vec32.y) / 2.0D - d1, vec3.z - d2);
            Vec3 vec33 = vec32.add(-vec31.x, -vec31.y, -vec31.z);
            if (vec33.length() != 0.0D) {
                vec33 = vec33.normalize();
                entityYaw = (float) (Math.atan2(vec33.z, vec33.x) * 180.0D / Math.PI);
                xRot = (float) (Math.atan(vec33.y) * 73.0D);
            }
        }

        matrixStack.translate(0.0F, 0.375F, 0.0F);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(-xRot));
        float hurtTime = (float) entity.getHurtTime() - partialTicks;
        float damage = entity.getDamage() - partialTicks;
        if (damage < 0.0F) {
            damage = 0.0F;
        }

        if (hurtTime > 0.0F) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float) entity.getHurtDir()));
        }

        int displayOffset = entity.getDisplayOffset();
        BlockState blockState = entity.getDisplayBlockState();
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            matrixStack.pushPose();
            matrixStack.scale(0.75F, 0.75F, 0.75F);
            matrixStack.translate(-0.5F, (float) (displayOffset - 8) / 16.0F, 0.5F);
            matrixStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            this.renderMinecartContents(entity, partialTicks, blockState, matrixStack, buffer, packedLight);
            matrixStack.popPose();
        }

        matrixStack.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        matrixStack.popPose();

        CustomAbstractMinecartEntity parent = entity.getLinkedParent();
        if (parent != null) {
            double startX = parent.getX();
            double startY = parent.getY();
            double startZ = parent.getZ();
            double endX = entity.getX();
            double endY = entity.getY();
            double endZ = entity.getZ();

            float distanceX = (float) (startX - endX);
            float distanceY = (float) (startY - endY);
            float distanceZ = (float) (startZ - endZ);
            float distance = entity.distanceTo(parent);

            double hAngle = Math.toDegrees(Math.atan2(endZ - startZ, endX - startX));
            hAngle += Math.ceil(-hAngle / 360) * 360;

            double vAngle = Math.asin(distanceY / distance);
            renderChain(distanceX, distanceY, distanceZ, (float) hAngle, (float) vAngle, matrixStack, buffer, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return MINECART_LOCATION;
    }

    protected void renderMinecartContents(T entity, float partialTicks, BlockState state, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        this.blockRenderer.renderSingleBlock(state, matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
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
