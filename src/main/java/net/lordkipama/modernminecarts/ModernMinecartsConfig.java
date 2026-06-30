package net.lordkipama.modernminecarts;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModernMinecartsConfig {
    private static final double MIN_SPEED = 0.01D;
    private static final double MAX_SPEED = 1.6D;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.DoubleValue COPPER_SPEED = BUILDER
            .comment("Base speed for unaffected copper rails.", "Allowed range: 0.01 - 1.6")
            .defineInRange("copper_speed", 0.8D, MIN_SPEED, MAX_SPEED);
    private static final ModConfigSpec.DoubleValue EXPOSED_COPPER_SPEED = BUILDER
            .comment("Base speed for exposed copper rails.", "Allowed range: 0.01 - 1.6")
            .defineInRange("exposed_copper_speed", 0.6D, MIN_SPEED, MAX_SPEED);
    private static final ModConfigSpec.DoubleValue WEATHERED_COPPER_SPEED = BUILDER
            .comment("Base speed for weathered copper rails.", "Allowed range: 0.01 - 1.6")
            .defineInRange("weathered_copper_speed", 0.3D, MIN_SPEED, MAX_SPEED);
    private static final ModConfigSpec.DoubleValue OXIDIZED_COPPER_SPEED = BUILDER
            .comment("Base speed for oxidized copper rails.", "Allowed range: 0.01 - 1.6")
            .defineInRange("oxidized_copper_speed", 0.2D, MIN_SPEED, MAX_SPEED);
    private static final ModConfigSpec.DoubleValue MAX_ASCENDING_SPEED = BUILDER
            .comment("Maximum speed while entering ascending rails.")
            .defineInRange("max_ascending_speed", 0.5D, MIN_SPEED, MAX_SPEED);
    private static final ModConfigSpec.BooleanValue ENABLE_FURNACE_MINECART_CHUNKLOADING = BUILDER
            .comment("If true, burning furnace minecarts keep nearby chunks loaded.")
            .define("enable_furnace_minecart_chunkloading", true);
    private static final ModConfigSpec.BooleanValue ENABLE_MINECART_CHAINING = BUILDER
            .comment("If true, minecarts can be linked together using chains.")
            .define("enable_minecart_chaining", true);
    private static final ModConfigSpec.BooleanValue ENABLE_COPPER_RAILS = BUILDER
            .comment("If true, copper rails can be crafted, placed, and shown in creative tabs.")
            .define("enable_copper_rails", true);
    private static final ModConfigSpec.BooleanValue ENABLE_RAIL_CROSSING = BUILDER
            .comment("If true, rail crossings can be placed and used.")
            .define("enable_rail_crossing", true);
    private static final ModConfigSpec.BooleanValue ENABLE_POWERED_DETECTOR_RAIL = BUILDER
            .comment("If true, powered detector rails can be placed and used.")
            .define("enable_powered_detector_rail", true);
    private static final ModConfigSpec.BooleanValue ENABLE_RAIL_JUMP = BUILDER
            .comment("If true, rail jumps can be placed and created from rails using sticks.")
            .define("enable_rail_jump", true);
    private static final ModConfigSpec.IntValue COPPER_RAIL_RECIPE_YIELD = BUILDER
            .comment("How many copper rails the recipe crafts.")
            .defineInRange("copper_rail_recipe_yield", 6, 1, 64);
    private static final ModConfigSpec.IntValue POWERED_RAIL_RECIPE_YIELD = BUILDER
            .comment("How many powered rails the recipe crafts.")
            .defineInRange("powered_rail_recipe_yield", 12, 1, 64);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static float copperSpeed() { return COPPER_SPEED.get().floatValue(); }
    public static float exposedCopperSpeed() { return EXPOSED_COPPER_SPEED.get().floatValue(); }
    public static float weatheredCopperSpeed() { return WEATHERED_COPPER_SPEED.get().floatValue(); }
    public static float oxidizedCopperSpeed() { return OXIDIZED_COPPER_SPEED.get().floatValue(); }
    public static float maxAscendingSpeed() { return MAX_ASCENDING_SPEED.get().floatValue(); }
    public static boolean enableFurnaceMinecartChunkloading() { return ENABLE_FURNACE_MINECART_CHUNKLOADING.get(); }
    public static boolean enableMinecartChaining() { return ENABLE_MINECART_CHAINING.get(); }
    public static boolean enableCopperRails() { return ENABLE_COPPER_RAILS.get(); }
    public static boolean enableRailCrossing() { return ENABLE_RAIL_CROSSING.get(); }
    public static boolean enablePoweredDetectorRail() { return ENABLE_POWERED_DETECTOR_RAIL.get(); }
    public static boolean enableRailJump() { return ENABLE_RAIL_JUMP.get(); }
    public static int copperRailRecipeYield() { return COPPER_RAIL_RECIPE_YIELD.get(); }
    public static int poweredRailRecipeYield() { return POWERED_RAIL_RECIPE_YIELD.get(); }

    public static void setCopperSpeed(double value) { COPPER_SPEED.set(value); }
    public static void setExposedCopperSpeed(double value) { EXPOSED_COPPER_SPEED.set(value); }
    public static void setWeatheredCopperSpeed(double value) { WEATHERED_COPPER_SPEED.set(value); }
    public static void setOxidizedCopperSpeed(double value) { OXIDIZED_COPPER_SPEED.set(value); }
    public static void setMaxAscendingSpeed(double value) { MAX_ASCENDING_SPEED.set(value); }
    public static void setEnableFurnaceMinecartChunkloading(boolean value) { ENABLE_FURNACE_MINECART_CHUNKLOADING.set(value); }
    public static void setEnableMinecartChaining(boolean value) { ENABLE_MINECART_CHAINING.set(value); }
    public static void setEnableCopperRails(boolean value) { ENABLE_COPPER_RAILS.set(value); }
    public static void setEnableRailCrossing(boolean value) { ENABLE_RAIL_CROSSING.set(value); }
    public static void setEnablePoweredDetectorRail(boolean value) { ENABLE_POWERED_DETECTOR_RAIL.set(value); }
    public static void setEnableRailJump(boolean value) { ENABLE_RAIL_JUMP.set(value); }
    public static void setCopperRailRecipeYield(int value) { COPPER_RAIL_RECIPE_YIELD.set(value); }
    public static void setPoweredRailRecipeYield(int value) { POWERED_RAIL_RECIPE_YIELD.set(value); }
    public static void save() { SPEC.save(); }
}
