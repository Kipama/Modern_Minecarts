package net.lordkipama.modernminecarts.recipe;

import com.google.gson.JsonObject;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.Identifier;

public class ConfigurableShapedRecipeSerializer implements RecipeSerializer<ShapedRecipe> {
    private static final ShapedRecipe.Serializer VANILLA = new ShapedRecipe.Serializer();

    @Override
    public ShapedRecipe read(Identifier recipeId, JsonObject json) {
        ShapedRecipe baseRecipe = VANILLA.read(recipeId, json);
        String yieldConfig = json.get("yield_config").getAsString();
        ItemStack result = baseRecipe.getOutput(null).copy();
        result.setCount(getConfiguredYield(yieldConfig));

        return new ShapedRecipe(
                recipeId,
                baseRecipe.getGroup(),
                baseRecipe.getCategory(),
                baseRecipe.getWidth(),
                baseRecipe.getHeight(),
                baseRecipe.getIngredients(),
                result,
                baseRecipe.showNotification()
        );
    }

    @Override
    public ShapedRecipe read(Identifier recipeId, PacketByteBuf buf) {
        return VANILLA.read(recipeId, buf);
    }

    @Override
    public void write(PacketByteBuf buf, ShapedRecipe recipe) {
        VANILLA.write(buf, recipe);
    }

    private static int getConfiguredYield(String yieldConfig) {
        return switch (yieldConfig) {
            case "copper_rail_recipe_yield" -> ModernMinecartsConfig.copperRailRecipeYield();
            case "powered_rail_recipe_yield" -> ModernMinecartsConfig.poweredRailRecipeYield();
            default -> throw new IllegalArgumentException("Unknown configurable recipe yield: " + yieldConfig);
        };
    }
}
