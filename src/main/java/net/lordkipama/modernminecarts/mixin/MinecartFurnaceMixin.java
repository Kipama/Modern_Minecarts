package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartFurnace.class)
abstract class MinecartFurnaceMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$applyFinalPoweredRailSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        MinecartFurnace minecart = (MinecartFurnace) (Object) this;
        BlockState railState = level.getBlockState(minecart.getCurrentBlockPosOrRailBelow());

        if (FurnaceMinecartHelper.getFuel(minecart) > 0
                && railState.getBlock() instanceof PoweredRailBlock poweredRail
                && !poweredRail.isActivatorRail()) {
            // MinecartFurnace halves its superclass result, so its final return value
            // must be overridden after that reduction.
            cir.setReturnValue(FurnaceMinecartHelper.getAppliedRailSpeed(minecart));
        } else if (railState.is(Blocks.POWERED_RAIL)) {
            cir.setReturnValue((double) ModernMinecartsConfig.poweredRailSpeed());
        }
    }
}
