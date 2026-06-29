package net.lordkipama.modernminecarts.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartChest.class)
abstract class MinecartChestMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void modernminecarts$skipInventoryOpenWhileCrouching(Player player, InteractionHand hand, Vec3 hitLocation, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.isCrouching()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
