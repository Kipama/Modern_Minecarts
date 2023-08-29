package net.lordkipama.modernminecarts.block.Custom;

import net.lordkipama.modernminecarts.RailSpeeds;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;

public class SwiftPoweredRailBlock extends PoweredRailBlock implements WeatheringRailBlock {
    private final WeatheringRailBlock.WeatherState weatherState;

    public SwiftPoweredRailBlock(Properties copy,WeatheringRailBlock.WeatherState p_154925_) {
        super(copy, true);
        this.weatherState = p_154925_;

    }



    @Override
    public boolean canMakeSlopes(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }

    @Override
    public float getRailMaxSpeed(BlockState state, Level world, BlockPos pos, AbstractMinecart cart) {
        String currentAge = String.valueOf(getAge());

        return switch (currentAge) {
            case "UNAFFECTED" -> RailSpeeds.default_swift_speed;
            case "EXPOSED" -> RailSpeeds.exposed_swift_speed;
            case "WEATHERED" -> RailSpeeds.weathered_swift_speed;
            default -> RailSpeeds.oxidized_swift_speed;
        };

    }

    @Override
    public void use(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);

        System.out.println("use in WeatheringRailBlock is triggered");
        context.getLevel().setBlock(context.getClickedPos(), ModBlocks.WAXED_EXPOSED_SWIFT_POWERED_RAIL.get().defaultBlockState(),1);
    }

    //COPPER AGING
    public void randomTick(BlockState p_222665_, ServerLevel p_222666_, BlockPos p_222667_, RandomSource p_222668_) {
        this.onRandomTick(p_222665_, p_222666_, p_222667_, p_222668_);
    }

    public boolean isRandomlyTicking(BlockState p_154935_) {
        return WeatheringRailBlock.getNext(p_154935_.getBlock()).isPresent();
    }

    public WeatheringRailBlock.WeatherState getAge() {
        return this.weatherState;
    }


}