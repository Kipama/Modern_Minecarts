package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.entity.MoverType;
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
    private static final double modernminecarts$legacyAirDrag = 0.975D;

    @Unique
    private boolean modernminecarts$jumpedOffSlope;

    @Inject(method = "getMaxSpeedWithRail", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$raiseFurnaceRailSpeed(CallbackInfoReturnable<Double> cir) {
        AbstractMinecart minecart = (AbstractMinecart) (Object) this;
        BlockState railState = minecart.level().getBlockState(minecart.getCurrentRailPosition());
        if (railState.is(Blocks.POWERED_RAIL)) {
            cir.setReturnValue((double) ModernMinecartsConfig.poweredRailSpeed());
        }

        if (!((Object) this instanceof MinecartFurnace furnaceMinecart)) {
            return;
        }

        if (MinecartLinkHelper.getLinkedParent(furnaceMinecart) != null || FurnaceMinecartHelper.getFuel(furnaceMinecart) <= 0) {
            return;
        }

        double targetSpeed = FurnaceMinecartHelper.getAppliedRailSpeed(furnaceMinecart);
        if (targetSpeed > cir.getReturnValue()) {
            cir.setReturnValue(targetSpeed);
        }
    }

    @Inject(method = "getMaxSpeedAirLateral", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$restoreAirLateralSpeed(CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() < modernminecarts$legacyAirLateralSpeed) {
            cir.setReturnValue(modernminecarts$legacyAirLateralSpeed);
        }
    }

    @Inject(method = "getDragAir", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$restoreAirDrag(CallbackInfoReturnable<Double> cir) {
        if (cir.getReturnValue() < modernminecarts$legacyAirDrag) {
            cir.setReturnValue(modernminecarts$legacyAirDrag);
        }
    }

    @Inject(method = "moveAlongTrack", at = @At("HEAD"))
    private void modernminecarts$applyPoweredDetectorRailMotion(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!ModernMinecartsConfig.enablePoweredDetectorRail() || !(state.getBlock() instanceof BaseRailBlock railBlock) || !(railBlock instanceof net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock detectorRail)) {
            return;
        }

        AbstractMinecart minecart = (AbstractMinecart) (Object) this;
        if (MinecartLinkHelper.getLinkedParent(minecart) != null) {
            return;
        }

        boolean powered = state.getValue(PoweredRailBlock.POWERED);
        Vec3 motion = minecart.getDeltaMovement();
        RailShape shape = detectorRail.getRailShape(state);
        boolean northSouth = shape == RailShape.NORTH_SOUTH || shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH;

        if (detectorRail.getDirInverted(state)) {
            if (northSouth) {
                if (motion.z < -0.2D) {
                    minecart.setDeltaMovement(motion.x, motion.y, motion.z / 8.0D + 0.1D);
                } else if (powered) {
                    minecart.setDeltaMovement(motion.x, motion.y, motion.z + 0.02D);
                } else {
                    minecart.setDeltaMovement(motion.x, motion.y, motion.z / 7.0D);
                }
            } else {
                if (motion.x > 0.2D) {
                    minecart.setDeltaMovement(motion.x / 8.0D - 0.1D, motion.y, motion.z);
                } else if (powered) {
                    minecart.setDeltaMovement(motion.x - 0.02D, motion.y, motion.z);
                } else {
                    minecart.setDeltaMovement(motion.x / 7.0D, motion.y, motion.z);
                }
            }
        } else if (northSouth) {
            if (motion.z > 0.2D) {
                minecart.setDeltaMovement(motion.x, motion.y, motion.z / 8.0D - 0.1D);
            } else if (powered) {
                minecart.setDeltaMovement(motion.x, motion.y, motion.z - 0.02D);
            } else {
                minecart.setDeltaMovement(motion.x, motion.y, motion.z / 7.0D);
            }
        } else {
            if (motion.x < -0.2D) {
                minecart.setDeltaMovement(motion.x / 8.0D + 0.1D, motion.y, motion.z);
            } else if (powered) {
                minecart.setDeltaMovement(motion.x + 0.02D, motion.y, motion.z);
            } else {
                minecart.setDeltaMovement(motion.x / 7.0D, motion.y, motion.z);
            }
        }
    }

    @Inject(method = "moveMinecartOnRail", at = @At("HEAD"))
    private void modernminecarts$syncAppliedRailSpeed(BlockPos pos, CallbackInfo ci) {
        AbstractMinecart minecart = (AbstractMinecart) (Object) this;
        double maxRailSpeed = minecart.getMaxSpeedWithRail();
        Vec3 motion = minecart.getDeltaMovement();

        // Keep deltaMovement aligned with the clamped rail speed so slope jumps
        // use the actual motion applied on the rail instead of the uncapped accumulator.
        minecart.setDeltaMovement(Mth.clamp(motion.x, -maxRailSpeed, maxRailSpeed), 0.0D, Mth.clamp(motion.z, -maxRailSpeed, maxRailSpeed));
    }

    @Inject(method = "comeOffTrack", at = @At("HEAD"), cancellable = true)
    private void modernminecarts$jumpOffSlopedRail(CallbackInfo cir) {
        AbstractMinecart minecart = (AbstractMinecart) (Object) this;
        double maxSpeed = minecart.getMaxSpeedAirLateral();
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

        double maxVerticalSpeed = minecart.getMaxSpeedAirVertical();
        if (maxVerticalSpeed > 0.0D && minecart.getDeltaMovement().y > maxVerticalSpeed) {
            if (Math.abs(minecart.getDeltaMovement().x) < 0.3F && Math.abs(minecart.getDeltaMovement().z) < 0.3F) {
                minecart.setDeltaMovement(minecart.getDeltaMovement().x, 0.15D, minecart.getDeltaMovement().z);
            } else {
                minecart.setDeltaMovement(minecart.getDeltaMovement().x, maxVerticalSpeed, minecart.getDeltaMovement().z);
            }
        }

        minecart.move(MoverType.SELF, minecart.getDeltaMovement());
        if (!minecart.onGround()) {
            minecart.setDeltaMovement(minecart.getDeltaMovement().scale(minecart.getDragAir()));
        }

        cir.cancel();
    }
}
