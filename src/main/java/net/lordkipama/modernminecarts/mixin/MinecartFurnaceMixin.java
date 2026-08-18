package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(MinecartFurnace.class)
abstract class MinecartFurnaceMixin {
    @ModifyConstant(method = "applyNaturalSlowdown", constant = @Constant(doubleValue = 0.98D))
    private double modernminecarts$removeUnlitFurnaceDrag(double original) {
        MinecartFurnace minecart = (MinecartFurnace) (Object) this;
        BlockState railState = minecart.level().getBlockState(minecart.getOnPos());
        boolean isUnpoweredPoweredRail = FurnaceMinecartHelper.usesPoweredRailLogic(railState)
                && !railState.getValue(PoweredRailBlock.POWERED);
        return FurnaceMinecartHelper.getFuel(minecart) > 0 && !isUnpoweredPoweredRail ? original : 1.0D;
    }
}
