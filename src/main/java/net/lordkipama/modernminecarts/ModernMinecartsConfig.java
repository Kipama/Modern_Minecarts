package net.lordkipama.modernminecarts;

public class ModernMinecartsConfig {

    public static boolean allowFurnaceMinecartChunkloading = true;

    //Rail speeds for Copper Rails. It is not advised to increase speed further than 0.8f
    public static float copper_speed = 0.8f;
    public static float exposed_copper_speed = 0.6f;
    public static float weathered_copper_speed = 0.3f;
    public static float oxidized_copper_speed = 0.2f;

    //This limits a minecarts speed when moving diagonally.
    //If your minecarts "bump into" ascending rails instead of driving up, decrease this value until fixed.
    public static float max_ascending_speed = 0.5f;
}
