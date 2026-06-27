package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
abstract class AbstractMinecartMixin {
    @Inject(method = "getMaxSpeedWithRail", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$raiseFurnaceRailSpeed(CallbackInfoReturnable<Double> cir) {
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
}
