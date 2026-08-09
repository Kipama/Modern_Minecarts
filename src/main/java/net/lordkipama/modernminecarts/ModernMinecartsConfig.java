package net.lordkipama.modernminecarts;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ModernMinecartsConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final ModConfigSpec.DoubleValue FURNACE_MINECART_SPEED = BUILDER
            .comment("Maximum speed for powered furnace minecarts.", "Allowed range: 0.01 - 1.6")
            .defineInRange("furnace_minecart_speed", 0.4D, 0.01D, 1.6D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean allowFurnaceMinecartChunkloading = true;

    //Rail speeds for Copper Rails. It is not advised to increase speed further than 0.8f
    public static float copper_speed = 0.8f;
    public static float exposed_copper_speed = 0.6f;
    public static float weathered_copper_speed = 0.3f;
    public static float oxidized_copper_speed = 0.2f;
    public static float powered_rail_speed = 0.4f;

    //This limits a minecarts speed when moving diagonally.
    //If your minecarts "bump into" ascending rails instead of driving up, decrease this value until fixed.
    public static float max_ascending_speed = 0.5f;

    public static float poweredRailSpeed() {
        return powered_rail_speed;
    }

    public static float furnaceMinecartSpeed() {
        return FURNACE_MINECART_SPEED.get().floatValue();
    }

    public static void setFurnaceMinecartSpeed(double value) {
        FURNACE_MINECART_SPEED.set(value);
    }

    public static void save() {
        SPEC.save();
    }
}
