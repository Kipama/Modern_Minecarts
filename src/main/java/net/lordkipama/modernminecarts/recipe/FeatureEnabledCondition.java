package net.lordkipama.modernminecarts.recipe;

import com.google.gson.JsonObject;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public record FeatureEnabledCondition(String feature) implements ICondition {
    private static final ResourceLocation ID = new ResourceLocation(ModernMinecarts.MOD_ID, "feature_enabled");
    public static final Serializer SERIALIZER = new Serializer();

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return switch (this.feature) {
            case "copper_rails" -> ModernMinecartsConfig.enableCopperRails();
            case "rail_crossing" -> ModernMinecartsConfig.enableRailCrossing();
            case "powered_detector_rail" -> ModernMinecartsConfig.enablePoweredDetectorRail();
            case "rail_jump" -> ModernMinecartsConfig.enableRailJump();
            default -> false;
        };
    }

    public static class Serializer implements IConditionSerializer<FeatureEnabledCondition> {
        @Override
        public void write(JsonObject json, FeatureEnabledCondition value) {
            json.addProperty("feature", value.feature());
        }

        @Override
        public FeatureEnabledCondition read(JsonObject json) {
            return new FeatureEnabledCondition(GsonHelper.getAsString(json, "feature"));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
