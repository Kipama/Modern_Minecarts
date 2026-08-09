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
                railSpeed / 2.0D
        );
    }

    public static int speedToDisplayUnits(double speed) {
        return Math.max(0, (int) Math.round(speed * MinecartTuning.SPEEDOMETER_SCALE));
    }

    public static int calculateSpeedometerValue(double actualSpeed, double speedLimit) {
        if (speedLimit <= 0.0D) {
            return 0;
        }
        return Math.min(
                MinecartTuning.SPEEDOMETER_HEIGHT,
                (int) Math.round(actualSpeed / speedLimit * MinecartTuning.SPEEDOMETER_HEIGHT)
        );
    }
}
