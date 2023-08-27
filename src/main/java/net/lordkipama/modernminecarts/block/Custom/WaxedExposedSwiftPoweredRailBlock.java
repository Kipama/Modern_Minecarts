package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.RailSpeeds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class WaxedExposedSwiftPoweredRailBlock extends PoweredRailBlock {

    public WaxedExposedSwiftPoweredRailBlock(Properties copy) {
        super(copy, true);
    }

    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
        return RailSpeeds.exposed_swift_speed;
    }
}