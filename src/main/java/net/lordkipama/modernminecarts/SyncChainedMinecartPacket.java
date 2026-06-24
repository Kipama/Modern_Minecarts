package net.lordkipama.modernminecarts;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lordkipama.modernminecarts.network.ChainLinkSync;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public record SyncChainedMinecartPacket(int parentId, int childId) implements CustomPayload {
    public static final Id<SyncChainedMinecartPacket> ID =
            new Id<>(ModernMinecarts.id("sync_chained_minecart"));
    public static final PacketCodec<RegistryByteBuf, SyncChainedMinecartPacket> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    SyncChainedMinecartPacket::parentId,
                    PacketCodecs.INTEGER,
                    SyncChainedMinecartPacket::childId,
                    SyncChainedMinecartPacket::new
            );

    public static void registerPayload() {
        PayloadTypeRegistry.playS2C().register(ID, CODEC);
    }

    public static void send(
            @Nullable Entity parent,
            @Nullable Entity child,
            ServerPlayerEntity... players
    ) {
        ChainLinkSync sync = ChainLinkSync.of(parent, child);
        SyncChainedMinecartPacket payload =
                new SyncChainedMinecartPacket(sync.parentId(), sync.childId());
        for (ServerPlayerEntity player : players) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public ChainLinkSync sync() {
        return new ChainLinkSync(parentId, childId);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
