package net.lordkipama.modernminecarts.entity;

import net.lordkipama.modernminecarts.Item.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CustomMinecartEntity extends AbstractMinecart {
    public CustomMinecartEntity(EntityType<? extends AbstractMinecart> p_38470_, Level p_38471_) {
        super(p_38470_, p_38471_);
    }

    public CustomMinecartEntity(Level p_38473_, double p_38474_, double p_38475_, double p_38476_) {
        super(ModEntities.CUSTOM_MINECART_ENTITY.get(), p_38473_, p_38474_, p_38475_, p_38476_);
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
        return ModItems.CUSTOM_MINECART_ITEM.get();
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
}