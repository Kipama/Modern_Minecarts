package net.lordkipama.modernminecarts.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractMinecartEntity.class)
public interface MinecartInvoker {
    @Invoker("getAdjacentRailPositionsByShape")
        public static Pair<Vec3i, Vec3i> invokeGetAdjacentRailPositionsByShape(RailShape shape){
        throw new AssertionError();
    }


    @Invoker("getMaxSpeed")
    public double invokeGetMaxSpeed(ServerWorld world);

    @Invoker("applySlowdown")
    public Vec3d invokeApplySlowdown(Vec3d velocity);

    @Invoker("willHitBlockAt")
    public boolean invokeWillHitBlockAt(BlockPos pos);
}
