package net.lordkipama.modernminecarts;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModernMinecartsConfig {
    private static final double MIN_SPEED = 0.01D;
    private static final double MAX_SPEED = 1.6D;

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.DoubleValue COPPER_SPEED = BUILDER
            .comment("Base speed for unaffected copper rails.", "Allowed range: 0.01 - 1.6")
            .defineInRange("copper_speed", 0.8D, MIN_SPEED, MAX_SPEED);

    private static final ForgeConfigSpec.DoubleValue EXPOSED_COPPER_SPEED = BUILDER
            .comment("Base speed for exposed copper rails.", "Allowed range: 0.01 - 1.6")
            .defineInRange("exposed_copper_speed", 0.6D, MIN_SPEED, MAX_SPEED);

    private static final ForgeConfigSpec.DoubleValue WEATHERED_COPPER_SPEED = BUILDER
            .comment("Base speed for weathered copper rails.", "Allowed range: 0.01 - 1.6")
            .defineInRange("weathered_copper_speed", 0.3D, MIN_SPEED, MAX_SPEED);

    private static final ForgeConfigSpec.DoubleValue OXIDIZED_COPPER_SPEED = BUILDER
            .comment("Base speed for oxidized copper rails.", "Allowed range: 0.01 - 1.6")
            .defineInRange("oxidized_copper_speed", 0.2D, MIN_SPEED, MAX_SPEED);

    private static final ForgeConfigSpec.DoubleValue MAX_ASCENDING_SPEED = BUILDER
            .comment(
                    "Maximum speed while entering ascending rails.",
                    "Above 0.5, minecarts may bump into ascending rails instead of driving up.",
                    "Allowed range: 0.01 - 1.6"
            )
            .defineInRange("max_ascending_speed", 0.5D, MIN_SPEED, MAX_SPEED);

    private static final ForgeConfigSpec.BooleanValue ENABLE_FURNACE_MINECART_CHUNKLOADING = BUILDER
            .comment("If true, burning furnace minecarts keep nearby chunks loaded.")
            .define("enable_furnace_minecart_chunkloading", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static float copperSpeed() {
        return COPPER_SPEED.get().floatValue();
    }

    public static float exposedCopperSpeed() {
        return EXPOSED_COPPER_SPEED.get().floatValue();
    }

    public static float weatheredCopperSpeed() {
        return WEATHERED_COPPER_SPEED.get().floatValue();
    }

    public static float oxidizedCopperSpeed() {
        return OXIDIZED_COPPER_SPEED.get().floatValue();
    }

    public static float maxAscendingSpeed() {
        return MAX_ASCENDING_SPEED.get().floatValue();
    }

    public static boolean enableFurnaceMinecartChunkloading() {
        return ENABLE_FURNACE_MINECART_CHUNKLOADING.get();
    }
}
