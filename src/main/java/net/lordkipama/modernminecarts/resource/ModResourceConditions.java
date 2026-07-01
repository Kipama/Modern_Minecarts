package net.lordkipama.modernminecarts.resource;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;

public final class ModResourceConditions {
    private ModResourceConditions() {
    }

    public static void register() {
        ResourceConditions.register(ModernMinecarts.id("feature_enabled"), ModResourceConditions::featureEnabled);
    }

    private static boolean featureEnabled(JsonObject json) {
        String feature = json.get("feature").getAsString();
        return switch (feature) {
            case "copper_rails" -> ModernMinecartsConfig.enableCopperRails();
            case "rail_crossing" -> ModernMinecartsConfig.enableRailCrossing();
            case "powered_detector_rail" -> ModernMinecartsConfig.enablePoweredDetectorRail();
            case "rail_jump" -> ModernMinecartsConfig.enableRailJump();
            default -> throw new IllegalArgumentException("Unknown feature toggle condition: " + feature);
        };
    }
}
