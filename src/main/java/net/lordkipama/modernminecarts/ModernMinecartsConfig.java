package net.lordkipama.modernminecarts;

import org.slf4j.Logger;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ModernMinecartsConfig {
    private static final double MIN_SPEED = 0.01D;
    private static final double MAX_SPEED = 1.6D;
    private static final int MIN_RECIPE_YIELD = 1;
    private static final int MAX_RECIPE_YIELD = 64;

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("modernminecarts.properties");

    private static double copperSpeed = 0.8D;
    private static double exposedCopperSpeed = 0.6D;
    private static double weatheredCopperSpeed = 0.3D;
    private static double oxidizedCopperSpeed = 0.2D;
    private static double poweredRailSpeed = 0.4D;
    private static double maxAscendingSpeed = 0.5D;

    private static boolean enableFurnaceMinecartChunkloading = true;
    private static boolean enableMinecartChaining = true;
    private static boolean enableCopperRails = true;
    private static boolean enableRailCrossing = true;
    private static boolean enablePoweredDetectorRail = true;
    private static boolean enableRailJump = true;

    private static int copperRailRecipeYield = 6;
    private static int poweredRailRecipeYield = 12;

    private ModernMinecartsConfig() {
    }

    public static void load(Logger logger) {
        Properties properties = new Properties();

        if (Files.exists(CONFIG_PATH)) {
            try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
                properties.load(inputStream);
            } catch (IOException exception) {
                logger.warn("Failed to read config file {}. Falling back to defaults.", CONFIG_PATH, exception);
            }
        }

        copperSpeed = readDouble(properties, "copper_speed", 0.8D, MIN_SPEED, MAX_SPEED, logger);
        exposedCopperSpeed = readDouble(properties, "exposed_copper_speed", 0.6D, MIN_SPEED, MAX_SPEED, logger);
        weatheredCopperSpeed = readDouble(properties, "weathered_copper_speed", 0.3D, MIN_SPEED, MAX_SPEED, logger);
        oxidizedCopperSpeed = readDouble(properties, "oxidized_copper_speed", 0.2D, MIN_SPEED, MAX_SPEED, logger);
        poweredRailSpeed = readDouble(properties, "powered_rail_speed", 0.4D, MIN_SPEED, MAX_SPEED, logger);
        maxAscendingSpeed = readDouble(properties, "max_ascending_speed", 0.5D, MIN_SPEED, MAX_SPEED, logger);

        enableFurnaceMinecartChunkloading = readBoolean(
                properties,
                "enable_furnace_minecart_chunkloading",
                true,
                logger
        );
        enableMinecartChaining = readBoolean(properties, "enable_minecart_chaining", true, logger);
        enableCopperRails = readBoolean(properties, "enable_copper_rails", true, logger);
        enableRailCrossing = readBoolean(properties, "enable_rail_crossing", true, logger);
        enablePoweredDetectorRail = readBoolean(properties, "enable_powered_detector_rail", true, logger);
        enableRailJump = readBoolean(properties, "enable_rail_jump", true, logger);

        copperRailRecipeYield = readInt(
                properties,
                "copper_rail_recipe_yield",
                6,
                MIN_RECIPE_YIELD,
                MAX_RECIPE_YIELD,
                logger
        );
        poweredRailRecipeYield = readInt(
                properties,
                "powered_rail_recipe_yield",
                12,
                MIN_RECIPE_YIELD,
                MAX_RECIPE_YIELD,
                logger
        );

        save(logger);
    }

    public static void save(Logger logger) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(CONFIG_PATH), StandardCharsets.UTF_8)) {
                writer.write("# Modern Minecarts configuration\n");
                writer.write("# Edit the values below and restart the game to apply changes.\n\n");

                writer.write("# Rail Speeds\n");
                writer.write("# Allowed range for all speed values: 0.01 - 1.6\n");
                writer.write("copper_speed=" + copperSpeed + "\n");
                writer.write("exposed_copper_speed=" + exposedCopperSpeed + "\n");
                writer.write("weathered_copper_speed=" + weatheredCopperSpeed + "\n");
                writer.write("oxidized_copper_speed=" + oxidizedCopperSpeed + "\n");
                writer.write("powered_rail_speed=" + poweredRailSpeed + "\n");
                writer.write("max_ascending_speed=" + maxAscendingSpeed + "\n\n");

                writer.write("# New Features\n");
                writer.write("enable_furnace_minecart_chunkloading=" + enableFurnaceMinecartChunkloading + "\n");
                writer.write("enable_minecart_chaining=" + enableMinecartChaining + "\n");
                writer.write("enable_copper_rails=" + enableCopperRails + "\n");
                writer.write("enable_rail_crossing=" + enableRailCrossing + "\n");
                writer.write("enable_powered_detector_rail=" + enablePoweredDetectorRail + "\n");
                writer.write("enable_rail_jump=" + enableRailJump + "\n\n");

                writer.write("# Crafting Recipes\n");
                writer.write("# Allowed range for recipe yields: 1 - 64\n");
                writer.write("copper_rail_recipe_yield=" + copperRailRecipeYield + "\n");
                writer.write("powered_rail_recipe_yield=" + poweredRailRecipeYield + "\n");
            }
        } catch (IOException exception) {
            logger.warn("Failed to write config file {}.", CONFIG_PATH, exception);
        }
    }

    public static double copperSpeed() {
        return copperSpeed;
    }

    public static double exposedCopperSpeed() {
        return exposedCopperSpeed;
    }

    public static double weatheredCopperSpeed() {
        return weatheredCopperSpeed;
    }

    public static double oxidizedCopperSpeed() {
        return oxidizedCopperSpeed;
    }

    public static double poweredRailSpeed() {
        return poweredRailSpeed;
    }

    public static double maxAscendingSpeed() {
        return maxAscendingSpeed;
    }

    public static boolean enableFurnaceMinecartChunkloading() {
        return enableFurnaceMinecartChunkloading;
    }

    public static boolean enableMinecartChaining() {
        return enableMinecartChaining;
    }

    public static boolean enableCopperRails() {
        return enableCopperRails;
    }

    public static boolean enableRailCrossing() {
        return enableRailCrossing;
    }

    public static boolean enablePoweredDetectorRail() {
        return enablePoweredDetectorRail;
    }

    public static boolean enableRailJump() {
        return enableRailJump;
    }

    public static int copperRailRecipeYield() {
        return copperRailRecipeYield;
    }

    public static int poweredRailRecipeYield() {
        return poweredRailRecipeYield;
    }

    private static double readDouble(
            Properties properties,
            String key,
            double defaultValue,
            double min,
            double max,
            Logger logger
    ) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }

        try {
            return clamp(Double.parseDouble(raw.trim()), min, max);
        } catch (NumberFormatException exception) {
            logger.warn("Invalid double value '{}' for {}. Using default {}.", raw, key, defaultValue);
            return defaultValue;
        }
    }

    private static int readInt(
            Properties properties,
            String key,
            int defaultValue,
            int min,
            int max,
            Logger logger
    ) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }

        try {
            return (int) clamp(Integer.parseInt(raw.trim()), min, max);
        } catch (NumberFormatException exception) {
            logger.warn("Invalid integer value '{}' for {}. Using default {}.", raw, key, defaultValue);
            return defaultValue;
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean defaultValue, Logger logger) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return defaultValue;
        }

        if ("true".equalsIgnoreCase(raw.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(raw.trim())) {
            return false;
        }

        logger.warn("Invalid boolean value '{}' for {}. Using default {}.", raw, key, defaultValue);
        return defaultValue;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
