package net.lordkipama.modernminecarts.util;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public final class PoweredDetectorRailHelper {
    private PoweredDetectorRailHelper() {
    }

    public static void applyMotion(AbstractMinecart minecart, ServerLevel level) {
        if (!ModernMinecartsConfig.enablePoweredDetectorRail()) {
            return;
        }

        BlockPos pos = minecart.getCurrentBlockPosOrRailBelow();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof BaseRailBlock railBlock) || !(railBlock instanceof PoweredDetectorRailBlock detectorRail)) {
            return;
        }

        if (MinecartLinkHelper.getLinkedParent(minecart) != null) {
            return;
        }

        boolean powered = state.getValue(PoweredRailBlock.POWERED);
        Vec3 motion = minecart.getDeltaMovement();
        RailShape shape = detectorRail.getRailShape(state);
        boolean northSouth = shape == RailShape.NORTH_SOUTH || shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH;

        if (detectorRail.getDirInverted(state)) {
            if (northSouth) {
                if (motion.z < -0.2D) {
                    minecart.setDeltaMovement(motion.x, motion.y, motion.z / 8.0D + 0.1D);
                } else if (powered) {
                    minecart.setDeltaMovement(motion.x, motion.y, motion.z + 0.02D);
                } else {
                    minecart.setDeltaMovement(motion.x, motion.y, motion.z / 7.0D);
                }
            } else if (motion.x > 0.2D) {
                minecart.setDeltaMovement(motion.x / 8.0D - 0.1D, motion.y, motion.z);
            } else if (powered) {
                minecart.setDeltaMovement(motion.x - 0.02D, motion.y, motion.z);
            } else {
                minecart.setDeltaMovement(motion.x / 7.0D, motion.y, motion.z);
            }
        } else if (northSouth) {
            if (motion.z > 0.2D) {
                minecart.setDeltaMovement(motion.x, motion.y, motion.z / 8.0D - 0.1D);
            } else if (powered) {
                minecart.setDeltaMovement(motion.x, motion.y, motion.z - 0.02D);
            } else {
                minecart.setDeltaMovement(motion.x, motion.y, motion.z / 7.0D);
            }
        } else if (motion.x < -0.2D) {
            minecart.setDeltaMovement(motion.x / 8.0D + 0.1D, motion.y, motion.z);
        } else if (powered) {
            minecart.setDeltaMovement(motion.x + 0.02D, motion.y, motion.z);
        } else {
            minecart.setDeltaMovement(motion.x / 7.0D, motion.y, motion.z);
        }
    }
}
