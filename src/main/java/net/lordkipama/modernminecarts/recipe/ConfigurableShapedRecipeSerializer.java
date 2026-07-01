package net.lordkipama.modernminecarts.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;

import java.util.Optional;

public class ConfigurableShapedRecipeSerializer implements RecipeSerializer<ConfigurableShapedRecipeSerializer.ConfigurableShapedRecipe> {
    private static final ShapedRecipe.Serializer VANILLA = new ShapedRecipe.Serializer();
    private static final MapCodec<ConfigurableShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(ConfigurableShapedRecipe::getGroup),
            CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(ConfigurableShapedRecipe::getCategory),
            RawShapedRecipe.CODEC.forGetter(ConfigurableShapedRecipe::getRawRecipe),
            ItemStack.VALIDATED_CODEC.fieldOf("result").forGetter(ConfigurableShapedRecipe::getConfiguredResult),
            Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(ConfigurableShapedRecipe::showNotification),
            Codec.STRING.fieldOf("yield_config").forGetter(ConfigurableShapedRecipe::yieldConfig)
    ).apply(instance, ConfigurableShapedRecipeSerializer::decodeConfiguredRecipe));
    private static final PacketCodec<RegistryByteBuf, ConfigurableShapedRecipe> PACKET_CODEC =
            PacketCodec.ofStatic(ConfigurableShapedRecipeSerializer::writePacket, ConfigurableShapedRecipeSerializer::readPacket);

    @Override
    public MapCodec<ConfigurableShapedRecipe> codec() {
        return CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, ConfigurableShapedRecipe> packetCodec() {
        return PACKET_CODEC;
    }

    private static ConfigurableShapedRecipe decodeConfiguredRecipe(
            String group,
            CraftingRecipeCategory category,
            RawShapedRecipe raw,
            ItemStack result,
            boolean showNotification,
            String yieldConfig
    ) {
        ItemStack configuredResult = result.copy();
        configuredResult.setCount(getConfiguredYield(yieldConfig));
        return new ConfigurableShapedRecipe(group, category, raw, configuredResult, showNotification, yieldConfig);
    }

    private static ConfigurableShapedRecipe readPacket(RegistryByteBuf buf) {
        ShapedRecipe recipe = VANILLA.packetCodec().decode(buf);
        return ConfigurableShapedRecipe.fromNetwork(recipe);
    }

    private static void writePacket(RegistryByteBuf buf, ConfigurableShapedRecipe recipe) {
        VANILLA.packetCodec().encode(buf, recipe.asVanillaRecipe());
    }

    private static int getConfiguredYield(String yieldConfig) {
        return switch (yieldConfig) {
            case "copper_rail_recipe_yield" -> ModernMinecartsConfig.copperRailRecipeYield();
            case "powered_rail_recipe_yield" -> ModernMinecartsConfig.poweredRailRecipeYield();
            default -> throw new IllegalArgumentException("Unknown configurable recipe yield: " + yieldConfig);
        };
    }

    public static final class ConfigurableShapedRecipe extends ShapedRecipe {
        private static final String NETWORK_YIELD_CONFIG = "__network_synced__";

        private final RawShapedRecipe rawRecipe;
        private final ItemStack configuredResult;
        private final String yieldConfig;

        private ConfigurableShapedRecipe(
                String group,
                CraftingRecipeCategory category,
                RawShapedRecipe rawRecipe,
                ItemStack configuredResult,
                boolean showNotification,
                String yieldConfig
        ) {
            super(group, category, rawRecipe, configuredResult, showNotification);
            this.rawRecipe = rawRecipe;
            this.configuredResult = configuredResult;
            this.yieldConfig = yieldConfig;
        }

        private static ConfigurableShapedRecipe fromNetwork(ShapedRecipe recipe) {
            return new ConfigurableShapedRecipe(
                    recipe.getGroup(),
                    recipe.getCategory(),
                    new RawShapedRecipe(recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), Optional.empty()),
                    recipe.getResult(null).copy(),
                    recipe.showNotification(),
                    NETWORK_YIELD_CONFIG
            );
        }

        private ShapedRecipe asVanillaRecipe() {
            return new ShapedRecipe(getGroup(), getCategory(), rawRecipe, configuredResult, showNotification());
        }

        RawShapedRecipe getRawRecipe() {
            return rawRecipe;
        }

        ItemStack getConfiguredResult() {
            return configuredResult;
        }

        String yieldConfig() {
            return yieldConfig;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return ModRecipeSerializers.CONFIGURABLE_SHAPED_RECIPE;
        }
    }
}
