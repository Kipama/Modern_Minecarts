package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.SyncChainedMinecartPacket;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void modernminecarts$sendLinkingInitData(ServerPlayer player, CallbackInfo ci) {
        if (this.entity instanceof ChainMinecartInterface chainMinecart) {
            SyncChainedMinecartPacket.send(chainMinecart.getLinkedParent(), this.entity, player);
            SyncChainedMinecartPacket.send(this.entity, chainMinecart.getLinkedChild(), player);
        }
    }
}
