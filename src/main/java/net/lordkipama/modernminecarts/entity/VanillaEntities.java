package net.lordkipama.modernminecarts.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class VanillaEntities {
    private static final DeferredRegister<EntityType<?>> VANILLA_ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, "minecraft");

    public static final DeferredHolder<EntityType<?>, EntityType<CustomMinecartEntity>> MINECART_ENTITY =
            VANILLA_ENTITIES.register("minecart",
                    () -> EntityType.Builder.<CustomMinecartEntity>of(CustomMinecartEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("minecart"));

    public static final DeferredHolder<EntityType<?>, EntityType<CustomMinecartChestEntity>> CHEST_MINECART_ENTITY =
            VANILLA_ENTITIES.register("chest_minecart",
                    () -> EntityType.Builder.<CustomMinecartChestEntity>of(CustomMinecartChestEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("chest_minecart"));

    public static final DeferredHolder<EntityType<?>, EntityType<CustomMinecartCommandBlockEntity>> COMMAND_BLOCK_MINECART_ENTITY =
            VANILLA_ENTITIES.register("command_block_minecart",
                    () -> EntityType.Builder.<CustomMinecartCommandBlockEntity>of(CustomMinecartCommandBlockEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("command_block_minecart"));

    public static final DeferredHolder<EntityType<?>, EntityType<CustomMinecartFurnaceEntity>> FURNACE_MINECART_ENTITY =
            VANILLA_ENTITIES.register("furnace_minecart",
                    () -> EntityType.Builder.<CustomMinecartFurnaceEntity>of(CustomMinecartFurnaceEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("furnace_minecart"));

    public static final DeferredHolder<EntityType<?>, EntityType<CustomMinecartHopperEntity>> HOPPER_MINECART_ENTITY =
            VANILLA_ENTITIES.register("hopper_minecart",
                    () -> EntityType.Builder.<CustomMinecartHopperEntity>of(CustomMinecartHopperEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("hopper_minecart"));

    public static final DeferredHolder<EntityType<?>, EntityType<CustomMinecartSpawnerEntity>> SPAWNER_MINECART_ENTITY =
            VANILLA_ENTITIES.register("spawner_minecart",
                    () -> EntityType.Builder.<CustomMinecartSpawnerEntity>of(CustomMinecartSpawnerEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("spawner_minecart"));

    public static final DeferredHolder<EntityType<?>, EntityType<CustomMinecartTNTEntity>> TNT_MINECART_ENTITY =
            VANILLA_ENTITIES.register("tnt_minecart",
                    () -> EntityType.Builder.<CustomMinecartTNTEntity>of(CustomMinecartTNTEntity::new, MobCategory.MISC)
                            .sized(0.98F, 0.7F)
                            .clientTrackingRange(8)
                            .build("tnt_minecart"));

    public static void register(IEventBus eventBus) {
        VANILLA_ENTITIES.register(eventBus);
    }
}
