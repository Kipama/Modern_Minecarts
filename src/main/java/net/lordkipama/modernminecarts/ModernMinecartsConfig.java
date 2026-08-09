package net.lordkipama.modernminecarts;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModernMinecartsConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.DoubleValue FURNACE_MINECART_SPEED = BUILDER
            .comment("Maximum speed for powered furnace minecarts.", "Allowed range: 0.01 - 1.6")
            .defineInRange("furnace_minecart_speed", 0.4D, 0.01D, 1.6D);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean allowFurnaceMinecartChunkloading = true;

    //Rail speeds for Copper Rails. It is not advised to increase speed further than 0.8f
    public static float copper_speed = 0.8f;
    public static float exposed_copper_speed = 0.6f;
    public static float weathered_copper_speed = 0.3f;
    public static float oxidized_copper_speed = 0.2f;
    public static float powered_rail_speed = 0.4f;

    public static float furnaceMinecartSpeed() {
        return FURNACE_MINECART_SPEED.get().floatValue();
    }

    //This limits a minecarts speed when moving diagonally.
    //If your minecarts "bump into" ascending rails instead of driving up, decrease this value until fixed.
    public static float max_ascending_speed = 0.5f;
}
