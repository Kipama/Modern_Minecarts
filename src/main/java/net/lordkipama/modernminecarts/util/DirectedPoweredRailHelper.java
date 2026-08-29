package net.lordkipama.modernminecarts.util;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.DirectedPoweredRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

/** Applies the directed rail's motion before vanilla processes the rail step. */
public final class DirectedPoweredRailHelper {
    private static final double ACCELERATION_PER_TICK = 0.06D;

    private DirectedPoweredRailHelper() {
    }

    public static void applyMotion(AbstractMinecart minecart, ServerLevel level) {
        if (!ModernMinecartsConfig.enableDirectedPoweredRail()
                || MinecartLinkHelper.getLinkedParent(minecart) != null) {
            return;
        }

        BlockPos railPos = minecart.getCurrentBlockPosOrRailBelow();
        BlockState state = level.getBlockState(railPos);
        if (!(state.getBlock() instanceof DirectedPoweredRailBlock rail)) {
            return;
        }

        Vec3 motion = minecart.getDeltaMovement();
        if (!state.getValue(PoweredRailBlock.POWERED)) {
            if (motion.horizontalDistance() < 0.03D) {
                minecart.setDeltaMovement(0.0D, motion.y, 0.0D);
            } else {
                minecart.setDeltaMovement(motion.x * 0.5D, motion.y, motion.z * 0.5D);
            }
            return;
        }

        RailShape shape = rail.getRailDirection(state, level, railPos, minecart);
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
        double direction = rail.isDirectionInverted(state) ? -1.0D : 1.0D;
        double maxSpeed = ModernMinecartsConfig.directedPoweredRailSpeed();

        if (northSouth) {
            minecart.setDeltaMovement(motion.x, motion.y, Mth.clamp(motion.z - direction * ACCELERATION_PER_TICK, -maxSpeed, maxSpeed));
        } else {
            minecart.setDeltaMovement(Mth.clamp(motion.x + direction * ACCELERATION_PER_TICK, -maxSpeed, maxSpeed), motion.y, motion.z);
        }
    }
}
