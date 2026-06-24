package net.lordkipama.modernminecarts.logic;

public final class TrainEngineLogic {
    private TrainEngineLogic() {
    }

    public static double calculateSpeedLimit(
            double railSpeed,
            int numberOfChildren,
            int burningFurnaces
    ) {
        int burning = Math.max(1, burningFurnaces);
        int nonBurningCarts = Math.max(0, numberOfChildren - burning + 1);
        if (nonBurningCarts <= 2 * burning) {
            return railSpeed;
        }

        return Math.max(
                railSpeed - (railSpeed / (10.0D * burning))
                        * (nonBurningCarts - 2 * burning),
                MinecartTuning.MINIMUM_ENGINE_SPEED
        );
    }

    public static int speedToDisplayUnits(double speed) {
        return Math.max(0, (int) Math.round(speed * MinecartTuning.SPEEDOMETER_SCALE));
    }

    public static int calculateSpeedometerValue(double actualSpeed, double speedLimit) {
        if (actualSpeed > speedLimit) {
            return speedToDisplayUnits(speedLimit);
        }
        return Math.max(
                0,
                (int) Math.round(actualSpeed * MinecartTuning.SPEEDOMETER_SCALE + 0.2D)
        );
    }
}
