package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.util.HopperMinecartHelper;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartHopper.class)
abstract class MinecartHopperMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void modernminecarts$transferLinkedInventory(CallbackInfo ci) {
        HopperMinecartHelper.tick((MinecartHopper) (Object) this);
    }
}
