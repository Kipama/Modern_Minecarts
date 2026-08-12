package net.lordkipama.modernminecarts.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Lets vanilla powered rails form one power network with copper rail variants. */
@Mixin(PoweredRailBlock.class)
public class PoweredRailBlockMixin {
    @Redirect(
            method = "isSameRailWithPower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZILnet/minecraft/world/level/block/state/properties/RailShape;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Ljava/lang/Object;)Z"
            )
    )
    private boolean modernminecarts$connectAllPoweredRails(BlockState state, Object block) {
        return state.is((Block) block) || state.getBlock() instanceof PoweredRailBlock;
    }
}
