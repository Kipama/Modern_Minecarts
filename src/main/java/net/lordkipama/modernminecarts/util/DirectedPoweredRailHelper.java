package net.lordkipama.modernminecarts.util;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.DirectedPoweredRailBlock;
import net.lordkipama.modernminecarts.entity.CustomAbstractMinecartEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

/** Applies directional acceleration for a powered directed rail. */
public final class DirectedPoweredRailHelper {
    private static final double ACCELERATION_PER_TICK = 0.06D;

    private DirectedPoweredRailHelper() {
    }

    public static void applyMotion(CustomAbstractMinecartEntity minecart, Level level, BlockPos pos, BlockState state) {
        if (!ModernMinecartsConfig.enableDirectedPoweredRail()
                || minecart.getLinkedParent() != null
                || !(state.getBlock() instanceof DirectedPoweredRailBlock rail)
                || !state.getValue(PoweredRailBlock.POWERED)) {
            return;
        }

        Vec3 motion = minecart.getDeltaMovement();
        RailShape shape = rail.getRailDirection(state, level, pos, minecart);
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;
        double direction = rail.isDirectionInverted(state) ? -1.0D : 1.0D;
        double maxSpeed = ModernMinecartsConfig.directedPoweredRailSpeed();

        if (northSouth) {
            direction = -direction;
            minecart.setDeltaMovement(motion.x, motion.y,
                    Mth.clamp(motion.z + direction * ACCELERATION_PER_TICK, -maxSpeed, maxSpeed));
        } else {
            minecart.setDeltaMovement(Mth.clamp(motion.x + direction * ACCELERATION_PER_TICK, -maxSpeed, maxSpeed),
                    motion.y, motion.z);
        }
    }
}
