package net.lordkipama.modernminecarts.recipe;

import com.mojang.serialization.MapCodec;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModConditionSerializers {
    public static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, ModernMinecarts.MOD_ID);

    static {
        CONDITION_SERIALIZERS.register("feature_enabled", () -> FeatureEnabledCondition.CODEC);
    }

    private ModConditionSerializers() {
    }

    public static void register(IEventBus eventBus) {
        CONDITION_SERIALIZERS.register(eventBus);
    }
}
