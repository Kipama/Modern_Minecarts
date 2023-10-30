package net.lordkipama.modernminecarts.Proxy;


import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.entity.CustomAbstractMinecartEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ModernMinecartsPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("modernminecarts", "example"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void Init(){
        int id = 0;

        INSTANCE.registerMessage(id++,
                CouplePacket.class,
                CouplePacket::encode,
                CouplePacket::decode,
                CouplePacket::handle);

    }

    public static class CouplePacket {
        public int parentID;
        public int childID;

        public CouplePacket(int pParentID, int pChildID) {
            this.parentID = pParentID;
            this.childID = pChildID;
        }

       public static void encode(CouplePacket msg, FriendlyByteBuf buf) {
            buf.writeInt(msg.parentID);
            buf.writeInt(msg.childID);
        }

        public static CouplePacket decode(FriendlyByteBuf buf) {
            CouplePacket packet = new CouplePacket(0,0);
            packet.parentID = buf.readInt();
            packet.childID = buf.readInt();
            return packet;
        }

        public static void handle(CouplePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {

                Level world = ModernMinecarts.PROXY.getWorld();

                Entity pChild = world.getEntity(msg.childID);
                if (pChild instanceof CustomAbstractMinecartEntity) {
                    CustomAbstractMinecartEntity child = (CustomAbstractMinecartEntity) pChild;
                    child.setLinkedParentClient(msg.parentID);
                }

                Entity pParent = world.getEntity(msg.parentID);
                if (pParent instanceof CustomAbstractMinecartEntity) {
                    CustomAbstractMinecartEntity parent = (CustomAbstractMinecartEntity) pParent;
                    parent.setLinkedChildClient(msg.childID);
                }

            });
            ctx.get().setPacketHandled(true);
        }

    }
}