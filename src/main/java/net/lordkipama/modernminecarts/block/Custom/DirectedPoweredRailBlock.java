package net.lordkipama.modernminecarts.block.Custom;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** A powered rail whose propulsion always follows its placement direction. */
public class DirectedPoweredRailBlock extends PoweredRailBlock {
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;

    public DirectedPoweredRailBlock(BlockBehaviour.Properties properties) {
        super(properties, true);
    }

    @Override
    protected void registerDefaultState() {
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(SHAPE, net.minecraft.world.level.block.state.properties.RailShape.NORTH_SOUTH)
                .setValue(POWERED, false)
                .setValue(WATERLOGGED, false)
                .setValue(INVERTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(INVERTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) {
            return null;
        }
        Direction facing = context.getHorizontalDirection();
        return state.setValue(INVERTED, facing == Direction.SOUTH || facing == Direction.WEST);
    }

    public boolean isDirectionInverted(BlockState state) {
        return state.getValue(INVERTED);
    }
}
