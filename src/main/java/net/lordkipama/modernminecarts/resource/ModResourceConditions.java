package net.lordkipama.modernminecarts.resource;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.resources.RegistryOps;

public final class ModResourceConditions {
    private ModResourceConditions() {
    }

    public static void register() {
        ResourceConditions.register(FeatureEnabledCondition.TYPE);
    }

    private record FeatureEnabledCondition(String feature) implements ResourceCondition {
        private static final MapCodec<FeatureEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("feature").forGetter(FeatureEnabledCondition::feature)
        ).apply(instance, FeatureEnabledCondition::new));
        private static final ResourceConditionType<FeatureEnabledCondition> TYPE =
                ResourceConditionType.create(ModernMinecarts.id("feature_enabled"), CODEC);

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(RegistryOps.RegistryInfoLookup registryLookup) {
            return switch (feature) {
                case "copper_rails" -> ModernMinecartsConfig.enableCopperRails();
                case "rail_crossing" -> ModernMinecartsConfig.enableRailCrossing();
                case "powered_detector_rail" -> ModernMinecartsConfig.enablePoweredDetectorRail();
                case "rail_jump" -> ModernMinecartsConfig.enableRailJump();
                default -> throw new IllegalArgumentException("Unknown feature toggle condition: " + feature);
            };
        }
    }
}
