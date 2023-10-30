package net.lordkipama.modernminecarts.entity;


import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ModernMinecarts.MOD_ID);


    /*public static final RegistryObject<EntityType<CustomMinecartEntity>> CUSTOM_MINECART_ENTITY =
            ENTITY_TYPES.register("custom_minecart",
                    () -> EntityType.Builder.<CustomMinecartEntity>of(CustomMinecartEntity::new, MobCategory.MISC )
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("custom_minecart"));
     */

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
