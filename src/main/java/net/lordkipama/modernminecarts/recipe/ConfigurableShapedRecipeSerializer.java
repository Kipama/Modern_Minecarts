package net.lordkipama.modernminecarts.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

public final class ConfigurableShapedRecipeSerializer {
    public static final MapCodec<ConfigurableShapedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> new Recipe.CommonInfo(recipe.showNotification())),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group())),
            ShapedRecipePattern.MAP_CODEC.forGetter(ConfigurableShapedRecipe::getPattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(ConfigurableShapedRecipe::getBaseResult),
            Codec.STRING.fieldOf("yield_config").forGetter(ConfigurableShapedRecipe::yieldConfig)
    ).apply(instance, ConfigurableShapedRecipeSerializer::decodeConfiguredRecipe));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigurableShapedRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC,
            recipe -> new Recipe.CommonInfo(recipe.showNotification()),
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC,
            recipe -> new CraftingRecipe.CraftingBookInfo(recipe.category(), recipe.group()),
            ShapedRecipePattern.STREAM_CODEC,
            ConfigurableShapedRecipe::getPattern,
            ItemStackTemplate.STREAM_CODEC,
            ConfigurableShapedRecipe::getBaseResult,
            ByteBufCodecs.STRING_UTF8,
            ConfigurableShapedRecipe::yieldConfig,
            ConfigurableShapedRecipeSerializer::decodeConfiguredRecipe
    );

    private ConfigurableShapedRecipeSerializer() {
    }

    private static ConfigurableShapedRecipe decodeConfiguredRecipe(
            Recipe.CommonInfo commonInfo,
            CraftingRecipe.CraftingBookInfo bookInfo,
            ShapedRecipePattern pattern,
            ItemStackTemplate result,
            String yieldConfig
    ) {
        return new ConfigurableShapedRecipe(commonInfo, bookInfo, pattern, result, yieldConfig);
    }

    private static int getConfiguredYield(String yieldConfig) {
        return switch (yieldConfig) {
            case "copper_rail_recipe_yield" -> ModernMinecartsConfig.copperRailRecipeYield();
            case "powered_rail_recipe_yield" -> ModernMinecartsConfig.poweredRailRecipeYield();
            default -> throw new IllegalArgumentException("Unknown configurable recipe yield: " + yieldConfig);
        };
    }

    public static final class ConfigurableShapedRecipe extends ShapedRecipe {
        private final ShapedRecipePattern pattern;
        private final ItemStackTemplate baseResult;
        private final String yieldConfig;

        private ConfigurableShapedRecipe(
                Recipe.CommonInfo commonInfo,
                CraftingRecipe.CraftingBookInfo bookInfo,
                ShapedRecipePattern pattern,
                ItemStackTemplate baseResult,
                String yieldConfig
        ) {
            super(commonInfo, bookInfo, pattern, baseResult.withCount(getConfiguredYield(yieldConfig)));
            this.pattern = pattern;
            this.baseResult = baseResult;
            this.yieldConfig = yieldConfig;
        }

        ShapedRecipePattern getPattern() {
            return this.pattern;
        }

        ItemStackTemplate getBaseResult() {
            return this.baseResult;
        }

        String yieldConfig() {
            return this.yieldConfig;
        }

        @SuppressWarnings("unchecked")
        @Override
        public RecipeSerializer<ShapedRecipe> getSerializer() {
            return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) ModRecipeSerializers.CONFIGURABLE_SHAPED_RECIPE;
        }
    }
}
