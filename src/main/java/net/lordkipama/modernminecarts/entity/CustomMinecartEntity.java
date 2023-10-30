package net.lordkipama.modernminecarts.entity;

import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.RailSpeeds;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.level.block.Block.canSupportRigidBlock;

public class CustomMinecartEntity extends CustomAbstractMinecartEntity {

    public CustomMinecartEntity(EntityType<? extends AbstractMinecart> p_38470_, Level p_38471_) {
        super(p_38470_, p_38471_);
    }

    public CustomMinecartEntity(Level p_38473_, double p_38474_, double p_38475_, double p_38476_) {
        super(VanillaEntities.MINECART_ENTITY.get(), p_38473_, p_38474_, p_38475_, p_38476_);
    }

    public InteractionResult interact(Player p_38483_, InteractionHand p_38484_) {
        if (p_38483_.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        } else if (this.isVehicle()) {
            return InteractionResult.PASS;
        } else if (!this.level().isClientSide) {
            return p_38483_.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    protected Item getDropItem() {
        if(getLinkedParent() != null || getLinkedChild() != null){
            level().addFreshEntity(new ItemEntity(level(),this.getX(), this.getY(), this.getZ(), new ItemStack(Items.CHAIN)));
        }

        return VanillaItems.MINECART_ITEM.get();
    }

    public void activateMinecart(int p_38478_, int p_38479_, int p_38480_, boolean p_38481_) {
        if (p_38481_) {
            if (this.isVehicle()) {
                this.ejectPassengers();
            }

            if (this.getHurtTime() == 0) {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.setDamage(50.0F);
                this.markHurt();
            }
        }

    }

    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.RIDEABLE;
    }

    private boolean shouldBeRemoved(BlockPos pPos, Level pLevel, RailShape pShape) {
        if (!canSupportRigidBlock(pLevel, pPos.below())) {
            return true;
        } else {
            switch (pShape) {
                case ASCENDING_EAST:
                    return !canSupportRigidBlock(pLevel, pPos.east());
                case ASCENDING_WEST:
                    return !canSupportRigidBlock(pLevel, pPos.west());
                case ASCENDING_NORTH:
                    return !canSupportRigidBlock(pLevel, pPos.north());
                case ASCENDING_SOUTH:
                    return !canSupportRigidBlock(pLevel, pPos.south());
                default:
                    return false;
            }
        }
    }
}