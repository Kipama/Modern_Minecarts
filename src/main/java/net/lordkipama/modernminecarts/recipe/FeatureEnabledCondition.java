package net.lordkipama.modernminecarts.recipe;

import com.mojang.serialization.Codec;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public record FeatureEnabledCondition(String feature) implements ICondition {
    public static final Codec<FeatureEnabledCondition> CODEC = Codec.STRING
            .fieldOf("feature")
            .xmap(FeatureEnabledCondition::new, FeatureEnabledCondition::feature)
            .codec();

    private static final DeferredRegister<Codec<? extends ICondition>> CONDITION_CODECS =
            DeferredRegister.create(ForgeRegistries.Keys.CONDITION_SERIALIZERS, ModernMinecarts.MOD_ID);
    public static final RegistryObject<Codec<FeatureEnabledCondition>> SERIALIZER =
            CONDITION_CODECS.register("feature_enabled", () -> CODEC);

    @Override
    public boolean test(IContext context) {
        return switch (this.feature) {
            case "copper_rails" -> ModernMinecartsConfig.enableCopperRails();
            case "rail_crossing" -> ModernMinecartsConfig.enableRailCrossing();
            case "powered_detector_rail" -> ModernMinecartsConfig.enablePoweredDetectorRail();
            case "directed_powered_rail" -> ModernMinecartsConfig.enableDirectedPoweredRail();
            case "rail_jump" -> ModernMinecartsConfig.enableRailJump();
            default -> false;
        };
    }

    public Codec<? extends ICondition> codec() {
        return CODEC;
    }

    public static void register(IEventBus eventBus) {
        CONDITION_CODECS.register(eventBus);
    }
}
