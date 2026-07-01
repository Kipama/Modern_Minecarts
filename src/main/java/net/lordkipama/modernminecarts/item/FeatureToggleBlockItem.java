package net.lordkipama.modernminecarts.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

import java.util.function.BooleanSupplier;

public class FeatureToggleBlockItem extends BlockItem {
    private final BooleanSupplier enabledSupplier;

    public FeatureToggleBlockItem(Block block, Settings settings, BooleanSupplier enabledSupplier) {
        super(block, settings);
        this.enabledSupplier = enabledSupplier;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!enabledSupplier.getAsBoolean()) {
            return ActionResult.FAIL;
        }

        return super.useOnBlock(context);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        return enabledSupplier.getAsBoolean() && super.canPlace(context, state);
    }
}
