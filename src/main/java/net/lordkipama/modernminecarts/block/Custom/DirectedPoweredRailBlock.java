package net.lordkipama.modernminecarts.block.Custom;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

/** A powered rail whose propulsion always follows its placement direction. */
public class DirectedPoweredRailBlock extends PoweredRailBlock {
    public static final BooleanProperty INVERTED = Properties.INVERTED;

    public DirectedPoweredRailBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(INVERTED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(INVERTED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        if (state == null) {
            return null;
        }

        Direction facing = context.getHorizontalPlayerFacing();
        return state.with(INVERTED, facing == Direction.SOUTH || facing == Direction.WEST);
    }
}
