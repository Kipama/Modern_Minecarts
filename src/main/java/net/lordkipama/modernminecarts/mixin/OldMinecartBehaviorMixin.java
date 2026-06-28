package net.lordkipama.modernminecarts.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import net.lordkipama.modernminecarts.util.PoweredDetectorRailHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OldMinecartBehavior.class)
abstract class OldMinecartBehaviorMixin {
    private static final double modernminecarts$occupiedRailSpeedCompensation = 1.0D / 0.75D;

    @Inject(method = "moveAlongTrack", at = @At("HEAD"))
    private void modernminecarts$applyPoweredDetectorRailMotion(ServerLevel level, CallbackInfo ci) {
        PoweredDetectorRailHelper.applyMotion(((MinecartBehaviorAccessor) this).modernminecarts$getMinecart(), level);
    }

    @Redirect(
            method = "moveAlongTrack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void modernminecarts$preserveOccupiedRailSpeed(AbstractMinecart minecart, MoverType moverType, Vec3 movement) {
        if (minecart.isVehicle() && minecart.level() instanceof ServerLevel serverLevel) {
            double maxRailSpeed = ((AbstractMinecartAccessor) minecart).modernminecarts$invokeGetMaxSpeed(serverLevel);
            movement = new Vec3(
                    Mth.clamp(movement.x * modernminecarts$occupiedRailSpeedCompensation, -maxRailSpeed, maxRailSpeed),
                    movement.y,
                    Mth.clamp(movement.z * modernminecarts$occupiedRailSpeedCompensation, -maxRailSpeed, maxRailSpeed)
            );
        }

        minecart.move(moverType, movement);
    }

    @Inject(
            method = "moveAlongTrack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void modernminecarts$syncAppliedRailMotion(ServerLevel level, CallbackInfo ci) {
        AbstractMinecart minecart = ((MinecartBehaviorAccessor) this).modernminecarts$getMinecart();
        Vec3 motion = minecart.getDeltaMovement();
        double maxRailSpeed = ((AbstractMinecartAccessor) minecart).modernminecarts$invokeGetMaxSpeed(level);

        // Keep the stored motion aligned with the actual rail step so slope
        // jumps use the speed the cart really had when it left the track.
        minecart.setDeltaMovement(
                Mth.clamp(motion.x, -maxRailSpeed, maxRailSpeed),
                0.0D,
                Mth.clamp(motion.z, -maxRailSpeed, maxRailSpeed)
        );
    }
}
