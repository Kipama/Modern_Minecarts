package net.lordkipama.modernminecarts.block.Custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ModernMinecartRailSpeed {
    float getModernMinecartRailSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart);
}
