package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.ModernMinecartRailSpeed;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.lordkipama.modernminecarts.util.PoweredDetectorRailHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
abstract class AbstractMinecartMixin {
    @Unique
    private static final float modernminecarts$legacyAirLateralSpeed = 0.8F;
    @Unique
    private static final float modernminecarts$legacyAirVerticalSpeed = -1.0F;
    @Unique
    private static final double modernminecarts$legacyAirDrag = 0.975D;

    @Unique
    private boolean modernminecarts$jumpedOffSlope;

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$applyRailSpeedOverrides(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        AbstractMinecart minecart = (AbstractMinecart) (Object) this;
        BlockPos railPos = minecart.getCurrentBlockPosOrRailBelow();
        BlockState railState = level.getBlockState(railPos);

        if (railState.getBlock() instanceof BaseRailBlock railBlock
                && railBlock instanceof ModernMinecartRailSpeed speedRail
                && ModernMinecarts.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(railState.getBlock()).getNamespace())) {
            float railMaxSpeed = speedRail.getModernMinecartRailSpeed(railState, level, railPos, minecart);
            if (MinecartLinkHelper.getLinkedParent(minecart) != null) {
                railMaxSpeed = modernminecarts$legacyAirLateralSpeed;
            } else if (minecart.isInWater()) {
                railMaxSpeed /= 2.0F;
            }

            cir.setReturnValue((double) modernminecarts$getFrontAdjustedRailSpeed(minecart, level, railPos, railMaxSpeed));
        }

        if ((Object) this instanceof MinecartFurnace furnaceMinecart
                && MinecartLinkHelper.getLinkedParent(furnaceMinecart) == null
                && FurnaceMinecartHelper.getFuel(furnaceMinecart) > 0) {
            double targetSpeed = FurnaceMinecartHelper.getAppliedRailSpeed(furnaceMinecart);
            if (targetSpeed > cir.getReturnValue()) {
                cir.setReturnValue(targetSpeed);
            }
        }
    }

    @Inject(method = "moveAlongTrack", at = @At("HEAD"))
    private void modernminecarts$applyPoweredDetectorRailMotion(ServerLevel level, CallbackInfo ci) {
        PoweredDetectorRailHelper.applyMotion((AbstractMinecart) (Object) this, level);
    }

    @Inject(method = "comeOffTrack", at = @At("HEAD"), cancellable = true)
    private void modernminecarts$jumpOffSlopedRail(ServerLevel level, CallbackInfo cir) {
        AbstractMinecart minecart = (AbstractMinecart) (Object) this;
        double maxSpeed = modernminecarts$legacyAirLateralSpeed;
        Vec3 motion = minecart.getDeltaMovement();

        int x = Mth.floor(minecart.getX());
        int y = Mth.floor(minecart.getY());
        int z = Mth.floor(minecart.getZ());

        BlockState hindBlockState;
        Vec3 rampBoost;

        if (motion.x > 0.0D) {
            hindBlockState = minecart.level().getBlockState(new BlockPos(x - 1, y - 1, z));
            rampBoost = new Vec3(Math.min(motion.x + 0.1D, maxSpeed), Math.min(Math.abs(motion.x), maxSpeed), 0.0D);
        } else if (motion.x < 0.0D) {
            hindBlockState = minecart.level().getBlockState(new BlockPos(x + 1, y - 1, z));
            rampBoost = new Vec3(Math.max(motion.x - 0.1D, -maxSpeed), Math.min(Math.abs(motion.x), maxSpeed), 0.0D);
        } else if (motion.z > 0.0D) {
            hindBlockState = minecart.level().getBlockState(new BlockPos(x, y - 1, z - 1));
            rampBoost = new Vec3(0.0D, Math.min(Math.abs(motion.z), maxSpeed), Math.min(motion.z + 0.1D, maxSpeed));
        } else {
            hindBlockState = minecart.level().getBlockState(new BlockPos(x, y - 1, z + 1));
            rampBoost = new Vec3(0.0D, Math.min(Math.abs(motion.z), maxSpeed), Math.max(motion.z - 0.1D, -maxSpeed));
        }

        BlockPos belowBlock = new BlockPos(x, y - 1, z);
        if (ModernMinecartsConfig.enableRailJump() && !modernminecarts$jumpedOffSlope && hindBlockState.is(ModBlocks.SLOPED_RAIL.get()) && minecart.level().getBlockState(belowBlock).is(Blocks.AIR)) {
            motion = rampBoost;
            modernminecarts$jumpedOffSlope = true;
        } else if (!ModernMinecartsConfig.enableRailJump() || !hindBlockState.is(ModBlocks.SLOPED_RAIL.get())) {
            modernminecarts$jumpedOffSlope = false;
        }

        minecart.setDeltaMovement(Mth.clamp(motion.x, -maxSpeed, maxSpeed), motion.y, Mth.clamp(motion.z, -maxSpeed, maxSpeed));
        if (minecart.onGround() && motion.y <= 0.0D) {
            minecart.setDeltaMovement(minecart.getDeltaMovement().scale(0.5D));
        }

        double maxVerticalSpeed = modernminecarts$legacyAirVerticalSpeed;
        if (maxVerticalSpeed > 0.0D && minecart.getDeltaMovement().y > maxVerticalSpeed) {
            if (Math.abs(minecart.getDeltaMovement().x) < 0.3F && Math.abs(minecart.getDeltaMovement().z) < 0.3F) {
                minecart.setDeltaMovement(minecart.getDeltaMovement().x, 0.15D, minecart.getDeltaMovement().z);
            } else {
                minecart.setDeltaMovement(minecart.getDeltaMovement().x, maxVerticalSpeed, minecart.getDeltaMovement().z);
            }
        }

        minecart.move(MoverType.SELF, minecart.getDeltaMovement());
        if (!minecart.onGround()) {
            minecart.setDeltaMovement(minecart.getDeltaMovement().scale(modernminecarts$legacyAirDrag));
        }

        cir.cancel();
    }

    @Unique
    private static float modernminecarts$getFrontAdjustedRailSpeed(AbstractMinecart minecart, ServerLevel level, BlockPos railPos, float railMaxSpeed) {
        if (railMaxSpeed <= ModernMinecartsConfig.maxAscendingSpeed()) {
            return railMaxSpeed;
        }

        Vec3 motion = minecart.getDeltaMovement();
        BlockPos frontPos;
        if (motion.x > 0.0D) {
            frontPos = railPos.east();
        } else if (motion.x < 0.0D) {
            frontPos = railPos.west();
        } else if (motion.z > 0.0D) {
            frontPos = railPos.south();
        } else {
            frontPos = railPos.north();
        }

        BlockState frontState = level.getBlockState(frontPos);
        if (!(frontState.getBlock() instanceof BaseRailBlock frontRail) || !(frontRail instanceof ModernMinecartRailSpeed speedRail)) {
            return railMaxSpeed;
        }

        RailShape frontShape = frontRail.getRailDirection(frontState, level, frontPos, minecart);
        if (frontShape == RailShape.ASCENDING_EAST
                || frontShape == RailShape.ASCENDING_WEST
                || frontShape == RailShape.ASCENDING_NORTH
                || frontShape == RailShape.ASCENDING_SOUTH) {
            return speedRail.getModernMinecartRailSpeed(frontState, level, frontPos, minecart);
        }

        return railMaxSpeed;
    }
}
