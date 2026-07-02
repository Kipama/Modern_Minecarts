package net.lordkipama.modernminecarts.util;

import net.minecraft.world.level.block.state.properties.RailShape;

public final class RailShapeHelper {
    private RailShapeHelper() {
    }

    public static boolean isAscending(RailShape shape) {
        return shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_EAST
                || shape == RailShape.ASCENDING_SOUTH
                || shape == RailShape.ASCENDING_WEST;
    }
}
