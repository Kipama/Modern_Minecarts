package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Applies the copper-rail powered behavior when Minecart Improvements is enabled. */
@Mixin(ExperimentalMinecartController.class)
public class ExperimentalMinecartControllerMixin {
    @Redirect(
            method = {"decelerateFromPoweredRail", "accelerateFromPoweredRail"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
            )
    )
    private boolean modernminecarts$treatCopperRailsAsPowered(BlockState state, Block block) {
        return state.isOf(block)
                || (block == Blocks.POWERED_RAIL
                && (state.getBlock() instanceof CopperRailBlock
                || state.getBlock() instanceof WaxedCopperRailBlock));
    }
}
