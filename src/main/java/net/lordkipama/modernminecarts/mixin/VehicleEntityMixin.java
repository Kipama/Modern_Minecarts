package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"))
    private void modernminecarts$disconnectChild(
            ServerWorld world,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if ((Object) this instanceof AbstractMinecartEntity cart
                && !cart.getEntityWorld().isClient()
                && ((ChainMinecartInterface) cart).getLinkedChild() != null) {
            AbstractMinecartEntity child =
                    ((ChainMinecartInterface) cart).getLinkedChild();
            ChainMinecartInterface.unsetParentChild(
                    (ChainMinecartInterface) cart,
                    (ChainMinecartInterface) child
            );
            cart.dropStack(world, new ItemStack(Items.IRON_CHAIN));
        }
    }

    @Inject(method = "killAndDropSelf", at = @At("HEAD"))
    private void modernminecarts$dropLinkedItems(
            ServerWorld world,
            DamageSource source,
            CallbackInfo ci
    ) {
        if (!((Object) this instanceof AbstractMinecartEntity cart)
                || cart.getEntityWorld().isClient()) {
            return;
        }

        ChainMinecartInterface linked = (ChainMinecartInterface) cart;
        if (linked.getLinkedParent() != null || linked.getLinkedChild() != null) {
            cart.dropStack(world, new ItemStack(Items.IRON_CHAIN));
        }
        if (cart instanceof Inventory inventory) {
            ItemScatterer.spawn(cart.getEntityWorld(), cart, inventory);
            inventory.clear();
        }
    }
}
