package net.lordkipama.modernminecarts;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lordkipama.modernminecarts.network.ChainLinkSync;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public record SyncChainedMinecartPacket(int parentId, int childId) implements CustomPacketPayload {
    public static final Type<SyncChainedMinecartPacket> TYPE = new Type<>(ModernMinecarts.id("sync_chained_minecart"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncChainedMinecartPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SyncChainedMinecartPacket::parentId,
            ByteBufCodecs.INT,
            SyncChainedMinecartPacket::childId,
            SyncChainedMinecartPacket::new
    );

    public static void registerPayload() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    public static void send(@Nullable Entity parent, @Nullable Entity child, ServerPlayer... players) {
        ChainLinkSync sync = ChainLinkSync.of(parent, child);
        SyncChainedMinecartPacket payload = new SyncChainedMinecartPacket(sync.parentId(), sync.childId());
        for (ServerPlayer player : players) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public ChainLinkSync sync() {
        return new ChainLinkSync(this.parentId, this.childId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
