package net.lordkipama.modernminecarts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lordkipama.modernminecarts.ChainMinecartInterface;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;


/*Copyright (C) 2022 Cammie

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to use, copy, modify, and/or merge copies of the
Software, and to permit persons to whom the Software is furnished to do so, subject to the following
restrictions:

 1) The above copyright notice and this permission notice shall be included in all copies or substantial
    portions of the Software.
 2) You include attribution to the copyright holder(s) in public display of any project that uses any
    portion of the Software.
 3) You may not publish or distribute substantial portions of the Software in its compiled or uncompiled
    forms without prior permission from the copyright holder.
 4) The Software does not make up a substantial portion of your own projects.
*
* */
public class SyncChainedMinecartPacket {
    public static final Identifier ID = ModernMinecarts.id("sync_chained_minecart");

    public static void send(@Nullable Entity parent, @Nullable Entity child, ServerPlayerEntity... players) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(parent != null);

        if(parent != null)
            buf.writeInt(parent.getId());

        buf.writeBoolean(child != null);
        if (child != null)
            buf.writeInt(child.getId());

        for (var player : players) {
            ServerPlayNetworking.send(player, ID, buf);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        boolean parentExists = buf.readBoolean();
        int parentId = parentExists ? buf.readInt() : -1;

        boolean childExists = buf.readBoolean();
        int childId = childExists ? buf.readInt() : -1;

        client.submit(() -> {
            if(client.world != null) {
                ClientWorld world = client.world;

                @Nullable Entity parentEntity = world.getEntityById(parentId);
                @Nullable Entity childEntity = world.getEntityById(childId);

                if (parentEntity instanceof ChainMinecartInterface linkable) {
                    linkable.setLinkedChildClient(childId);
                }

                if (childEntity instanceof ChainMinecartInterface linkable) {
                    linkable.setLinkedParentClient(parentId);
                }
            }
        });
    }
}