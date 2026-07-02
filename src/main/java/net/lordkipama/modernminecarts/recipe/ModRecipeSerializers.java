package net.lordkipama.modernminecarts.recipe;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipeSerializers {
    public static final RecipeSerializer<ConfigurableShapedRecipeSerializer.ConfigurableShapedRecipe> CONFIGURABLE_SHAPED_RECIPE =
            Registry.register(
                    BuiltInRegistries.RECIPE_SERIALIZER,
                    ModernMinecarts.id("configurable_shaped_recipe"),
                    new RecipeSerializer<>(ConfigurableShapedRecipeSerializer.MAP_CODEC, ConfigurableShapedRecipeSerializer.STREAM_CODEC)
            );

    private ModRecipeSerializers() {
    }

    public static void register() {
    }
}
