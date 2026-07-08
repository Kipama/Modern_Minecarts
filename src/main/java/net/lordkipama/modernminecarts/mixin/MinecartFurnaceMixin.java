package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartFurnace.class)
abstract class MinecartFurnaceMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$restoreConfiguredFurnaceSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        MinecartFurnace minecart = (MinecartFurnace) (Object) this;
        if (MinecartLinkHelper.getLinkedParent(minecart) == null && FurnaceMinecartHelper.getFuel(minecart) > 0) {
            cir.setReturnValue(Math.max(cir.getReturnValue(), FurnaceMinecartHelper.getAppliedRailSpeed(minecart)));
        }
    }
}
