package net.lordkipama.modernminecarts.attachment;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class MinecartAttachmentTypes {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModernMinecarts.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ChainMinecartData>> CHAIN_DATA =
            ATTACHMENT_TYPES.register("chain_data", () -> AttachmentType.serializable(ChainMinecartData::new).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<FurnaceMinecartData>> FURNACE_DATA =
            ATTACHMENT_TYPES.register("furnace_data", () -> AttachmentType.serializable(FurnaceMinecartData::new).build());

    private MinecartAttachmentTypes() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
