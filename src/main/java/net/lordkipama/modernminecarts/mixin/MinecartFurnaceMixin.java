package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(MinecartFurnace.class)
abstract class MinecartFurnaceMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$applyFinalFurnaceSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        MinecartFurnace minecart = (MinecartFurnace) (Object) this;
        BlockState railState = level.getBlockState(minecart.getCurrentBlockPosOrRailBelow());

        if (FurnaceMinecartHelper.getFuel(minecart) > 0) {
            // MinecartFurnace halves its superclass result, so its final return value
            // must be overridden after that reduction.
            cir.setReturnValue(FurnaceMinecartHelper.getAppliedRailSpeed(minecart));
        } else if (railState.is(Blocks.POWERED_RAIL)) {
            cir.setReturnValue((double) ModernMinecartsConfig.poweredRailSpeed());
        } else {
            // Match other minecart types when the furnace is not burning. Minecraft's
            // MinecartFurnace applies this reduction after all rail-specific limits.
            cir.setReturnValue(cir.getReturnValue() / (minecart.isInWater() ? 0.75D : 0.5D));
        }
    }

    @ModifyConstant(method = "applyNaturalSlowdown", constant = @Constant(doubleValue = 0.98D))
    private double modernminecarts$removeUnlitFurnaceDrag(double original) {
        MinecartFurnace minecart = (MinecartFurnace) (Object) this;
        return FurnaceMinecartHelper.getFuel(minecart) > 0 ? original : 1.0D;
    }
}
