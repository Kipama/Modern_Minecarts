package net.lordkipama.modernminecarts.logic;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;

public final class MinecartTuning {
    public static final double VANILLA_RAIL_SPEED = 0.4D;
    public static final double FURNACE_MINECART_MAX_SPEED = 0.4D;
    public static final double MINIMUM_ENGINE_SPEED = 0.2D;
    public static final int SPEEDOMETER_SCALE = 80;
    public static final int SPEEDOMETER_HEIGHT = 32;
    public static final int MAX_TRAIN_LENGTH = 128;

    private MinecartTuning() {
    }

    public static double copperRailSpeed() {
        return ModernMinecartsConfig.copperSpeed();
    }

    public static double exposedCopperRailSpeed() {
        return ModernMinecartsConfig.exposedCopperSpeed();
    }

    public static double weatheredCopperRailSpeed() {
        return ModernMinecartsConfig.weatheredCopperSpeed();
    }

    public static double oxidizedCopperRailSpeed() {
        return ModernMinecartsConfig.oxidizedCopperSpeed();
    }

    public static double poweredRailSpeed() {
        return ModernMinecartsConfig.poweredRailSpeed();
    }

    public static double ascendingCopperRailSpeed() {
        return ModernMinecartsConfig.maxAscendingSpeed();
    }
}
