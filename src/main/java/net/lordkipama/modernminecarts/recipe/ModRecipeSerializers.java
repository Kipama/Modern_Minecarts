package net.lordkipama.modernminecarts.recipe;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModRecipeSerializers {
    public static final RecipeSerializer<?> CONFIGURABLE_SHAPED_RECIPE =
            Registry.register(
                    Registries.RECIPE_SERIALIZER,
                    ModernMinecarts.id("configurable_shaped_recipe"),
                    new ConfigurableShapedRecipeSerializer()
            );

    private ModRecipeSerializers() {
    }

    public static void register() {
    }
}
