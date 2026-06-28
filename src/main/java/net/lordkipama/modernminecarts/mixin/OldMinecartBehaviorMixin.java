package net.lordkipama.modernminecarts.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.OldMinecartBehavior;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OldMinecartBehavior.class)
abstract class OldMinecartBehaviorMixin {
    @Inject(
            method = "moveAlongTrack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"
            )
    )
    private void modernminecarts$syncAppliedRailMotion(ServerLevel level, CallbackInfo ci) {
        AbstractMinecart minecart = ((MinecartBehaviorAccessor) this).modernminecarts$getMinecart();
        Vec3 motion = minecart.getDeltaMovement();
        double movementFactor = minecart.isVehicle() ? 0.75D : 1.0D;
        double maxRailSpeed = ((AbstractMinecartAccessor) minecart).modernminecarts$invokeGetMaxSpeed(level);

        // Keep the stored motion aligned with the actual rail step so slope
        // jumps use the speed the cart really had when it left the track.
        minecart.setDeltaMovement(
                Mth.clamp(movementFactor * motion.x, -maxRailSpeed, maxRailSpeed),
                0.0D,
                Mth.clamp(movementFactor * motion.z, -maxRailSpeed, maxRailSpeed)
        );
    }
}
