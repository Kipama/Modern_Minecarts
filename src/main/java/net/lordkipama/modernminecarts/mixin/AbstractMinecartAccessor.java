package net.lordkipama.modernminecarts.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractMinecart.class)
interface AbstractMinecartAccessor {
    @Invoker("getMaxSpeed")
    double modernminecarts$invokeGetMaxSpeed(ServerLevel level);
}
