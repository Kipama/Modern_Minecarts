package net.lordkipama.modernminecarts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.network.ChainLinkSync;
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
        ChainLinkSync sync = ChainLinkSync.of(parent, child);
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(sync.hasParent());
        if(sync.hasParent())
            buf.writeInt(sync.parentId());
        buf.writeBoolean(sync.hasChild());
        if (sync.hasChild())
            buf.writeInt(sync.childId());

        for (var player : players) {
            ServerPlayNetworking.send(player, ID, buf);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int parentId = buf.readBoolean() ? buf.readInt() : ChainLinkSync.MISSING_ENTITY;
        int childId = buf.readBoolean() ? buf.readInt() : ChainLinkSync.MISSING_ENTITY;
        ChainLinkSync sync = new ChainLinkSync(parentId, childId);

        client.submit(() -> {
            if(client.world != null) {
                sync.apply(client.world);
            }
        });
    }
}
