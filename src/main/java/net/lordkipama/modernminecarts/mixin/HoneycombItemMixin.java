package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.util.AdvancementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.DataMapHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoneycombItem.class)
abstract class HoneycombItemMixin {
    @Inject(method = "useOn", at = @At("RETURN"))
    private void modernminecarts$awardWaxOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction()) {
            return;
        }

        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer) || context.getLevel().isClientSide()) {
            return;
        }

        BlockPos pos = context.getClickedPos();
        BlockState currentState = context.getLevel().getBlockState(pos);
        if (DataMapHooks.getBlockUnwaxed(currentState.getBlock()) != null) {
            AdvancementHelper.awardWaxOn(serverPlayer);
        }
    }
}
