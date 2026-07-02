package net.lordkipama.modernminecarts.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractMinecart.class)
public interface MinecartInvoker {
    @Invoker("exits")
    static Pair<Vec3i, Vec3i> invokeGetAdjacentRailPositionsByShape(RailShape shape) {
        throw new AssertionError();
    }

    @Invoker("getMaxSpeed")
    double invokeGetMaxSpeed(ServerLevel level);

    @Invoker("applyNaturalSlowdown")
    Vec3 invokeApplyNaturalSlowdown(Vec3 movement);

}
