package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Minecraft 1.21.11 performs regular minecart physics in this controller rather
 * than {@code AbstractMinecartEntity}. Treat copper rails as powered rails here
 * so they receive vanilla's acceleration and braking behavior.
 */
@Mixin(DefaultMinecartController.class)
public class DefaultMinecartControllerMixin {
    @Redirect(
            method = "moveOnRail",
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
