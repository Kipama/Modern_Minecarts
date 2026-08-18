package net.lordkipama.modernminecarts.recipe;

import com.mojang.serialization.Codec;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ConfigurableShapedRecipeSerializer implements RecipeSerializer<ShapedRecipe> {
    private static final ShapedRecipe.Serializer VANILLA = new ShapedRecipe.Serializer();

    @Override
    public Codec<ShapedRecipe> codec() {
        return VANILLA.codec().xmap(this::withConfiguredYield, recipe -> recipe);
    }

    @Override
    public ShapedRecipe fromNetwork(FriendlyByteBuf buffer) {
        return withConfiguredYield(VANILLA.fromNetwork(buffer));
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ShapedRecipe recipe) {
        VANILLA.toNetwork(buffer, recipe);
    }

    private ShapedRecipe withConfiguredYield(ShapedRecipe baseRecipe) {
        ItemStack result = baseRecipe.getResultItem(RegistryAccess.EMPTY).copy();
        result.setCount(ModernMinecartsConfig.directedPoweredRailRecipeYield());
        return new ShapedRecipe(
                baseRecipe.getGroup(),
                baseRecipe.category(),
                baseRecipe.getWidth(),
                baseRecipe.getHeight(),
                baseRecipe.getIngredients(),
                result,
                baseRecipe.showNotification()
        );
    }
}
