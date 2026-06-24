package net.lordkipama.modernminecarts.logic;

public final class PoweredDetectorMotion {
    public record Motion(double x, double z) {
    }

    private PoweredDetectorMotion() {
    }

    public static Motion adjust(
            double x,
            double z,
            boolean northSouth,
            boolean inverted,
            boolean powered
    ) {
        if (inverted) {
            if (northSouth) {
                return new Motion(x, adjustNegativeDirection(z, powered));
            }
            return new Motion(adjustPositiveDirection(x, powered), z);
        }

        if (northSouth) {
            return new Motion(x, adjustPositiveDirection(z, powered));
        }
        return new Motion(adjustNegativeDirection(x, powered), z);
    }

    private static double adjustPositiveDirection(double speed, boolean powered) {
        if (speed > 0.2D) {
            return speed / 8.0D - 0.1D;
        }
        return powered ? speed - 0.02D : speed / 7.0D;
    }

    private static double adjustNegativeDirection(double speed, boolean powered) {
        if (speed < -0.2D) {
            return speed / 8.0D + 0.1D;
        }
        return powered ? speed + 0.02D : speed / 7.0D;
    }
}
