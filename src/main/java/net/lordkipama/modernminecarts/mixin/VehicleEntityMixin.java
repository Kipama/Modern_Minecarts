package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void modernminecarts$disconnectChild(
            ServerLevel level,
            DamageSource source,
            float damage,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if ((Object) this instanceof AbstractMinecart cart
                && !cart.level().isClientSide()
                && ((ChainMinecartInterface) cart).getLinkedChild() != null) {
            AbstractMinecart child =
                    ((ChainMinecartInterface) cart).getLinkedChild();
            ChainMinecartInterface.unsetParentChild(
                    (ChainMinecartInterface) cart,
                    (ChainMinecartInterface) child
            );
            cart.spawnAtLocation(level, new ItemStack(Items.IRON_CHAIN));
        }
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void modernminecarts$dropLinkedItems(
            ServerLevel level,
            Item item,
            CallbackInfo ci
    ) {
        if (!((Object) this instanceof AbstractMinecart cart)
                || cart.level().isClientSide()) {
            return;
        }

        ChainMinecartInterface linked = (ChainMinecartInterface) cart;
        if (linked.getLinkedParent() != null || linked.getLinkedChild() != null) {
            cart.spawnAtLocation(level, new ItemStack(Items.IRON_CHAIN));
        }
        if (cart instanceof Container container) {
            Containers.dropContents(cart.level(), cart, container);
            container.clearContent();
        }
    }
}
