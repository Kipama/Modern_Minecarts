package net.lordkipama.modernminecarts.entity;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VanillaEntities {
    private static final DeferredRegister<EntityType<?>> VANILLA_ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "minecraft");


    public static final RegistryObject<EntityType<CustomMinecartEntity>> MINECART_ENTITY =
            VANILLA_ENTITIES.register("minecart",
                    () -> EntityType.Builder.<CustomMinecartEntity>of(CustomMinecartEntity::new, MobCategory.MISC )
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("minecart"));


    public static void register(IEventBus eventBus) {
        VANILLA_ENTITIES.register(eventBus);
    }
}
