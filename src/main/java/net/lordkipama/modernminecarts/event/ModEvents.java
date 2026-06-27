package net.lordkipama.modernminecarts.event;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.util.AdvancementHelper;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

@EventBusSubscriber(modid = ModernMinecarts.MOD_ID)
public final class ModEvents {
    private static final String PARENT_ENTITY_TAG = "ParentEntity";

    private ModEvents() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());

        if (!event.getLevel().isClientSide()
                && player.isCrouching()
                && stack.getItem() == Items.CHAIN
                && stack.has(DataComponents.CUSTOM_DATA)
                && isRightClickingAir(player, event.getLevel())) {
            CompoundTag nbt = getCustomDataTag(stack);

            if (nbt.contains(PARENT_ENTITY_TAG)) {
                nbt.remove(PARENT_ENTITY_TAG);
                saveCustomDataTag(stack, nbt);

                event.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHAIN_BREAK, SoundSource.NEUTRAL, 1F, 1F);
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getLevel().isClientSide() || event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) {
            return;
        }

        if (!(event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) || !event.getItemStack().is(Items.HONEYCOMB)) {
            return;
        }

        BlockState blockState = event.getLevel().getBlockState(event.getPos());
        if (isCopperRail(blockState)) {
            AdvancementHelper.awardWaxOn(serverPlayer);
        }
    }

    private static boolean isCopperRail(BlockState state) {
        return state.is(ModBlocks.COPPER_RAIL.get())
            || state.is(ModBlocks.EXPOSED_COPPER_RAIL.get())
            || state.is(ModBlocks.WEATHERED_COPPER_RAIL.get())
            || state.is(ModBlocks.OXIDIZED_COPPER_RAIL.get());
    }

    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());
        Entity entity = event.getTarget();

        if (entity instanceof MinecartFurnace furnaceCart && !player.isCrouching()) {
            if (!event.getLevel().isClientSide()) {
                player.openMenu(FurnaceMinecartHelper.createMenuProvider(furnaceCart));
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            event.setCanceled(true);
            return;
        }

        if (!(entity instanceof AbstractMinecart cart) || stack.getItem() != Items.CHAIN || !player.isCrouching() || !(event.getLevel() instanceof ServerLevel server)) {
            return;
        }

        CompoundTag nbt = getCustomDataTag(stack);
        if (nbt.contains(PARENT_ENTITY_TAG)) {
            if (!nbt.getUUID(PARENT_ENTITY_TAG).equals(cart.getUUID())
                    && server.getEntity(nbt.getUUID(PARENT_ENTITY_TAG)) instanceof AbstractMinecart parent) {
                if (MinecartLinkHelper.isTrainCircular(parent, cart)) {
                    if (MinecartLinkHelper.getLinkedParent(parent) == cart) {
                        if (MinecartLinkHelper.getLinkedParent(cart) != null) {
                            BlockPos pos = MinecartLinkHelper.getLinkedParent(cart).blockPosition();
                            event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                            MinecartLinkHelper.unsetParentChild(MinecartLinkHelper.getLinkedParent(cart), cart);
                        }
                        if (MinecartLinkHelper.getLinkedChild(parent) != null) {
                            BlockPos pos = MinecartLinkHelper.getLinkedChild(parent).blockPosition();
                            event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                            MinecartLinkHelper.unsetParentChild(parent, MinecartLinkHelper.getLinkedChild(parent));
                        }

                        MinecartLinkHelper.unsetParentChild(cart, parent);
                        MinecartLinkHelper.setParentChild(parent, cart);
                    }
                } else if (MinecartLinkHelper.getLinkedParent(cart) != parent) {
                    if (MinecartLinkHelper.getLinkedParent(cart) != null) {
                        MinecartLinkHelper.unsetParentChild(MinecartLinkHelper.getLinkedParent(cart), cart);
                        BlockPos pos = entity.blockPosition();
                        event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                    }
                    if (MinecartLinkHelper.getLinkedChild(parent) != null) {
                        BlockPos pos = MinecartLinkHelper.getLinkedChild(parent).blockPosition();
                        event.getLevel().addFreshEntity(new ItemEntity(event.getLevel(), pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.CHAIN)));
                        MinecartLinkHelper.unsetParentChild(parent, MinecartLinkHelper.getLinkedChild(parent));
                    }
                    if (!player.isCreative()) {
                        stack.shrink(1);
                    }
                    MinecartLinkHelper.setParentChild(parent, cart);
                }
            }

            event.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CHAIN_PLACE, SoundSource.NEUTRAL, 1F, 1F);
            nbt.remove(PARENT_ENTITY_TAG);
            saveCustomDataTag(stack, nbt);
        } else {
            nbt.putUUID(PARENT_ENTITY_TAG, cart.getUUID());
            saveCustomDataTag(stack, nbt);
            event.getLevel().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 1F, 1F);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractMinecart minecart) || event.getEntity().level().isClientSide()) {
            return;
        }

        MinecartLinkHelper.tickFollower(minecart);
        MinecartLinkHelper.unlinkRemovedNeighbor(minecart);
    }

    @SubscribeEvent
    public static void onEntityTickPre(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof MinecartFurnace furnaceMinecart) || event.getEntity().level().isClientSide()) {
            return;
        }

        FurnaceMinecartHelper.tick(furnaceMinecart);
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof AbstractMinecart minecart) || event.getLevel().isClientSide()) {
            return;
        }

        AbstractMinecart parent = MinecartLinkHelper.getLinkedParent(minecart);
        AbstractMinecart child = MinecartLinkHelper.getLinkedChild(minecart);
        if (parent != null) {
            MinecartLinkHelper.dropChainItem(minecart);
            MinecartLinkHelper.unsetParentChild(parent, minecart);
        }
        if (child != null) {
            MinecartLinkHelper.dropChainItem(minecart);
            MinecartLinkHelper.unsetParentChild(minecart, child);
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void object, ResourceManager resourceManager, ProfilerFiller profiler) {
                replacePoweredRailRecipe(event.getServerResources().getRecipeManager(), event.getServerResources().getRegistryLookup());
            }
        });
    }

    private static void replacePoweredRailRecipe(RecipeManager recipeManager, HolderLookup.Provider registries) {
        ResourceLocation recipeId = ResourceLocation.withDefaultNamespace("powered_rail");
        RecipeHolder<?> replacement = loadRecipe(recipeId, registries);
        if (replacement == null) {
            return;
        }

        Collection<RecipeHolder<?>> existingRecipes = recipeManager.getRecipes();
        List<RecipeHolder<?>> updatedRecipes = new ArrayList<>(existingRecipes.size());
        boolean replaced = false;
        for (RecipeHolder<?> recipeHolder : existingRecipes) {
            if (recipeHolder.id().equals(recipeId)) {
                updatedRecipes.add(replacement);
                replaced = true;
            } else {
                updatedRecipes.add(recipeHolder);
            }
        }

        if (!replaced) {
            updatedRecipes.add(replacement);
        }

        recipeManager.replaceRecipes(updatedRecipes);
    }

    private static RecipeHolder<?> loadRecipe(ResourceLocation recipeId, HolderLookup.Provider registries) {
        try (InputStream inputStream = ModEvents.class.getResourceAsStream("/data/minecraft/recipe/powered_rail.json")) {
            if (inputStream == null) {
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                Recipe<?> recipe = Recipe.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
                return new RecipeHolder<>(recipeId, recipe);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isRightClickingAir(Player player, Level level) {
        double reachDistance = 5.0D;
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookDirection = player.getLookAngle();
        Vec3 endPosition = eyePosition.add(lookDirection.scale(reachDistance));

        HitResult blockHit = level.clip(new ClipContext(
                eyePosition,
                endPosition,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (blockHit.getType() != HitResult.Type.MISS) {
            return false;
        }

        AABB searchBox = player.getBoundingBox()
                .expandTowards(lookDirection.scale(reachDistance))
                .inflate(1.0D);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level,
                player,
                eyePosition,
                endPosition,
                searchBox,
                entity -> !entity.isSpectator() && entity.isPickable()
        );

        return entityHit == null;
    }

    private static CompoundTag getCustomDataTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void saveCustomDataTag(ItemStack stack, CompoundTag tag) {
        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
