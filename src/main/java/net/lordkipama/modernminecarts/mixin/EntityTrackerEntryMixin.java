package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.ChainMinecartInterface;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {

    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "startTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onStartedTrackingBy(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    public void minecarttweaks$sendLinkingInitData(ServerPlayerEntity player, CallbackInfo ci) {
        if (this.entity instanceof ChainMinecartInterface ChainMinecartInterface) {
            net.lordkipama.modernminecarts.SyncChainedMinecartPacket.send(ChainMinecartInterface.getLinkedParent(), this.entity, player);
            net.lordkipama.modernminecarts.SyncChainedMinecartPacket.send(this.entity, ((ChainMinecartInterface) this.entity).getLinkedChild(), player);
        }
    }
}