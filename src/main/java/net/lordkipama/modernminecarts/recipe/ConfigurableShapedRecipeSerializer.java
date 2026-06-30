package net.lordkipama.modernminecarts.recipe;

import com.google.gson.JsonObject;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ConfigurableShapedRecipeSerializer implements RecipeSerializer<ShapedRecipe> {
    private static final ShapedRecipe.Serializer VANILLA = new ShapedRecipe.Serializer();

    @Override
    public ShapedRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        ShapedRecipe baseRecipe = VANILLA.fromJson(recipeId, json);
        String yieldConfig = GsonHelper.getAsString(json, "yield_config");
        ItemStack result = baseRecipe.getResultItem(RegistryAccess.EMPTY).copy();
        result.setCount(getConfiguredYield(yieldConfig));
        return new ShapedRecipe(
                recipeId,
                baseRecipe.getGroup(),
                baseRecipe.category(),
                baseRecipe.getWidth(),
                baseRecipe.getHeight(),
                baseRecipe.getIngredients(),
                result,
                baseRecipe.showNotification()
        );
    }

    @Override
    public ShapedRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return VANILLA.fromNetwork(recipeId, buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ShapedRecipe recipe) {
        VANILLA.toNetwork(buffer, recipe);
    }

    private static int getConfiguredYield(String yieldConfig) {
        return switch (yieldConfig) {
            case "copper_rail_recipe_yield" -> ModernMinecartsConfig.copperRailRecipeYield();
            case "powered_rail_recipe_yield" -> ModernMinecartsConfig.poweredRailRecipeYield();
            default -> throw new IllegalArgumentException("Unknown configurable recipe yield: " + yieldConfig);
        };
    }
}
