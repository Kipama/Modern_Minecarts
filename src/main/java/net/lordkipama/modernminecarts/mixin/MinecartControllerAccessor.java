package net.lordkipama.modernminecarts.mixin;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartController.class)
public interface MinecartControllerAccessor {
    @Accessor("minecart")
    AbstractMinecartEntity modernminecarts$getMinecart();
}
