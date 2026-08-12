package net.lordkipama.modernminecarts.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Lets vanilla powered rails form one power network with copper rail variants. */
@Mixin(PoweredRailBlock.class)
public class PoweredRailBlockMixin {
    @Redirect(
            method = "isPoweredByOtherRails(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ZILnet/minecraft/block/enums/RailShape;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"
            )
    )
    private boolean modernminecarts$connectAllPoweredRails(BlockState state, Block block) {
        return state.isOf(block) || state.getBlock() instanceof PoweredRailBlock;
    }
}
