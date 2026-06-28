package net.lordkipama.modernminecarts.Proxy;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModernMinecartsPacketHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(CouplePacket.TYPE, CouplePacket.STREAM_CODEC, new MainThreadPayloadHandler<>(CouplePacket::handle));
    }

    public record CouplePacket(int parentID, int childID) implements CustomPacketPayload {
        public static final Type<CouplePacket> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(ModernMinecarts.MOD_ID, "couple"));
        public static final StreamCodec<RegistryFriendlyByteBuf, CouplePacket> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                CouplePacket::parentID,
                ByteBufCodecs.VAR_INT,
                CouplePacket::childID,
                CouplePacket::new
        );

        @Override
        public Type<CouplePacket> type() {
            return TYPE;
        }

        public static void handle(CouplePacket msg, IPayloadContext context) {
            Level world = context.player().level();
            context.enqueueWork(() -> {
                Entity childEntity = world.getEntity(msg.childID());
                if (childEntity instanceof AbstractMinecart child) {
                    MinecartLinkHelper.setLinkedParentClient(child, msg.parentID());
                }

                Entity parentEntity = world.getEntity(msg.parentID());
                if (parentEntity instanceof AbstractMinecart parent) {
                    MinecartLinkHelper.setLinkedChildClient(parent, msg.childID());
                }
            });
        }
    }
}
