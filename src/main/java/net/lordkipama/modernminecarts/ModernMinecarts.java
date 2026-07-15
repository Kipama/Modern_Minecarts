package net.lordkipama.modernminecarts;


import net.lordkipama.modernminecarts.client.ModernMinecartsClient;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.Proxy.ClientProxy;
import net.lordkipama.modernminecarts.Proxy.IProxy;
import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.Proxy.ServerProxy;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.block.VanillaBlocks;
import net.lordkipama.modernminecarts.entity.CustomMinecartChestEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartCommandBlockEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartFurnaceEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartHopperEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartSpawnerEntity;
import net.lordkipama.modernminecarts.entity.CustomMinecartTNTEntity;
import net.lordkipama.modernminecarts.entity.ModEntities;
import net.lordkipama.modernminecarts.entity.VanillaEntities;
import net.lordkipama.modernminecarts.inventory.ModMenus;
import net.lordkipama.modernminecarts.recipe.FeatureEnabledCondition;
import net.lordkipama.modernminecarts.recipe.ModRecipeSerializers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModernMinecarts.MOD_ID)
public class ModernMinecarts {
    public static final String MOD_ID = "modernminecarts";
    public static IProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public ModernMinecarts() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModernMinecartsConfig.SPEC);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ModernMinecartsClient::register);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        ModEntities.register(modEventBus);
        VanillaEntities.register(modEventBus);
        VanillaItems.register(modEventBus);
        VanillaBlocks.register(modEventBus);
        ModMenus.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        ModernMinecartsPacketHandler.Init();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> CraftingHelper.register(FeatureEnabledCondition.SERIALIZER));
    }
}
