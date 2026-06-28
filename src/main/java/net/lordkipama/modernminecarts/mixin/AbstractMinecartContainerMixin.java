package net.lordkipama.modernminecarts.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartContainer.class)
abstract class AbstractMinecartContainerMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void modernminecarts$skipInventoryOpenWhileCrouching(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Object self = this;
        if (player.isCrouching() && (self instanceof MinecartChest || self instanceof MinecartHopper)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
