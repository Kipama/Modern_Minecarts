package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.DirectedPoweredRailBlock;
import net.lordkipama.modernminecarts.util.DirectedPoweredRailHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.block.PoweredRailBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NewMinecartBehavior.class)
abstract class NewMinecartBehaviorMixin {
    @Inject(method = "moveAlongTrack", at = @At("HEAD"))
    private void modernminecarts$applyDirectedPoweredRailMotion(ServerLevel level, CallbackInfo ci) {
        AbstractMinecart minecart = ((MinecartBehaviorAccessor) this).modernminecarts$getMinecart();
        DirectedPoweredRailHelper.applyMotion(minecart, level);
    }

    @Redirect(
            method = "calculateHaltTrackSpeed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/PoweredRailBlock;isActivatorRail()Z")
    )
    private boolean modernminecarts$skipVanillaDirectedRailBraking(PoweredRailBlock rail) {
        return (ModernMinecartsConfig.enableDirectedPoweredRail() && rail instanceof DirectedPoweredRailBlock)
                || rail.isActivatorRail();
    }

    @Redirect(
            method = "calculateBoostTrackSpeed",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/PoweredRailBlock;isActivatorRail()Z")
    )
    private boolean modernminecarts$skipVanillaDirectedRailBoosting(PoweredRailBlock rail) {
        return (ModernMinecartsConfig.enableDirectedPoweredRail() && rail instanceof DirectedPoweredRailBlock)
                || rail.isActivatorRail();
    }
}
