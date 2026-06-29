package net.lordkipama.modernminecarts.Item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import java.util.function.BooleanSupplier;

public class FeatureToggleBlockItem extends BlockItem {
    private final BooleanSupplier enabledSupplier;

    public FeatureToggleBlockItem(Block block, Properties properties, BooleanSupplier enabledSupplier) {
        super(block, properties);
        this.enabledSupplier = enabledSupplier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!enabledSupplier.getAsBoolean()) {
            return InteractionResult.FAIL;
        }

        return super.useOn(context);
    }
}
