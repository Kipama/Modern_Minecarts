package net.lordkipama.modernminecarts.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

public record FeatureEnabledCondition(String feature) implements ICondition {
    public static final MapCodec<FeatureEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
            .group(Codec.STRING.fieldOf("feature").forGetter(FeatureEnabledCondition::feature))
            .apply(builder, FeatureEnabledCondition::new));

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

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
