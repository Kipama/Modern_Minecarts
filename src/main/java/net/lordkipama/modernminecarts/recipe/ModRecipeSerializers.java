package net.lordkipama.modernminecarts.recipe;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ModernMinecarts.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> CONFIGURABLE_SHAPED_RECIPE =
            RECIPE_SERIALIZERS.register("configurable_shaped_recipe", ConfigurableShapedRecipeSerializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
