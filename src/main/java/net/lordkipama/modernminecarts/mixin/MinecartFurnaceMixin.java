package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartFurnace.class)
abstract class MinecartFurnaceMixin {
    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$applyFinalFurnaceSpeed(ServerLevel level, CallbackInfoReturnable<Double> cir) {
        MinecartFurnace minecart = (MinecartFurnace) (Object) this;
        BlockState railState = level.getBlockState(minecart.getCurrentBlockPosOrRailBelow());
        boolean usesPoweredRailLogic = FurnaceMinecartHelper.usesPoweredRailLogic(railState);
        boolean isRailPowered = usesPoweredRailLogic && railState.getValue(net.minecraft.world.level.block.PoweredRailBlock.POWERED);

        if (FurnaceMinecartHelper.getFuel(minecart) > 0 && (!usesPoweredRailLogic || isRailPowered)) {
            cir.setReturnValue(FurnaceMinecartHelper.getAppliedRailSpeed(minecart));
        } else if (railState.is(Blocks.POWERED_RAIL)) {
            cir.setReturnValue((double) ModernMinecartsConfig.poweredRailSpeed());
        } else {
            cir.setReturnValue(cir.getReturnValue() / (minecart.isInWater() ? 0.75D : 0.5D));
        }
    }

    @ModifyConstant(method = "applyNaturalSlowdown", constant = @Constant(doubleValue = 0.98D))
    private double modernminecarts$removeUnlitFurnaceDrag(double original) {
        MinecartFurnace minecart = (MinecartFurnace) (Object) this;
        BlockState railState = minecart.level().getBlockState(minecart.getCurrentBlockPosOrRailBelow());
        boolean isUnpoweredPoweredRail = FurnaceMinecartHelper.usesPoweredRailLogic(railState)
                && !railState.getValue(net.minecraft.world.level.block.PoweredRailBlock.POWERED);
        return FurnaceMinecartHelper.getFuel(minecart) > 0 && !isUnpoweredPoweredRail ? original : 1.0D;
    }
}
