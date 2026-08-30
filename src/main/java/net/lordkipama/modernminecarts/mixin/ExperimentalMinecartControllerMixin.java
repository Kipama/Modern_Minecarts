package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.DirectedPoweredRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.logic.MinecartTuning;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
                || state.getBlock() instanceof WaxedCopperRailBlock
                || state.getBlock() instanceof DirectedPoweredRailBlock));
    }

    @Inject(method = "moveOnRail", at = @At("TAIL"))
    private void modernminecarts$applyDirectedPoweredRailMotion(ServerWorld world, CallbackInfo ci) {
        AbstractMinecartEntity minecart = ((MinecartControllerAccessor) (Object) this).modernminecarts$getMinecart();
        BlockPos railPos = minecart.getRailOrMinecartPos();
        BlockState state = world.getBlockState(railPos);
        if (!(state.getBlock() instanceof DirectedPoweredRailBlock)
                || !state.get(PoweredRailBlock.POWERED)
                || ((ChainMinecartInterface) minecart).getLinkedParent() != null) {
            return;
        }

        RailShape shape = state.get(PoweredRailBlock.SHAPE);
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
        boolean inverted = state.get(DirectedPoweredRailBlock.INVERTED);
        Vec3d velocity = minecart.getVelocity();
        double acceleration = 0.06D;
        double maxSpeed = MinecartTuning.directedPoweredRailSpeed();

        if (northSouth) {
            double z = velocity.z + (inverted ? acceleration : -acceleration);
            minecart.setVelocity(velocity.x, velocity.y, MathHelper.clamp(z, -maxSpeed, maxSpeed));
        } else {
            double x = velocity.x + (inverted ? -acceleration : acceleration);
            minecart.setVelocity(MathHelper.clamp(x, -maxSpeed, maxSpeed), velocity.y, velocity.z);
        }
    }
}
