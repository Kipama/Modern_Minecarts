package net.lordkipama.modernminecarts.entity;


import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class CustomMinecartTNTEntity extends CustomAbstractMinecartEntity {
    private static final byte EVENT_PRIME = 10;
    private int fuse = -1;

    public CustomMinecartTNTEntity(EntityType<? extends net.lordkipama.modernminecarts.entity.CustomMinecartTNTEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CustomMinecartTNTEntity(Level pLevel, double pX, double pY, double pZ) {
        super(VanillaEntities.TNT_MINECART_ENTITY.get(), pLevel, pX, pY, pZ);
    }

    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.TNT;
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.TNT.defaultBlockState();
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick();
        if (this.fuse > 0) {
            --this.fuse;
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
        } else if (this.fuse == 0) {
            this.explode(this.getDeltaMovement().horizontalDistanceSqr());
        }

        if (this.horizontalCollision) {
            double d0 = this.getDeltaMovement().horizontalDistanceSqr();
            if (d0 >= (double)0.01F) {
                this.explode(d0);
            }
        }

    }

    /**
     * Called when the entity is attacked.
     */
    public boolean hurt(DamageSource pSource, float pAmount) {
        Entity entity = pSource.getDirectEntity();
        if (entity instanceof AbstractArrow abstractarrow) {
            if (abstractarrow.isOnFire()) {
                DamageSource damagesource = this.damageSources().explosion(this, pSource.getEntity());
                this.explode(damagesource, abstractarrow.getDeltaMovement().lengthSqr());
            }
        }

        return super.hurt(pSource, pAmount);
    }

    public void destroy(DamageSource pSource) {
        double d0 = this.getDeltaMovement().horizontalDistanceSqr();
        if (!pSource.is(DamageTypeTags.IS_FIRE) && !pSource.is(DamageTypeTags.IS_EXPLOSION) && !(d0 >= (double)0.01F)) {
            super.destroy(pSource);
        } else {
            if (this.fuse < 0) {
                this.primeFuse();
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
            }

        }
    }

    protected Item getDropItem() {
        if(getLinkedParent() != null || getLinkedChild() != null){
            level().addFreshEntity(new ItemEntity(level(),this.getX(), this.getY(), this.getZ(), new ItemStack(Items.CHAIN)));
        }
        return Items.TNT_MINECART;
    }

    /**
     * Makes the minecart explode.
     */
    protected void explode(double pRadiusModifier) {
        this.explode((DamageSource)null, pRadiusModifier);
    }

    protected void explode(@Nullable DamageSource p_259539_, double p_260287_) {
        if (!this.level().isClientSide) {
            double d0 = Math.sqrt(p_260287_);
            if (d0 > 5.0D) {
                d0 = 5.0D;
            }

            this.level().explode(this, p_259539_, (ExplosionDamageCalculator)null, this.getX(), this.getY(), this.getZ(), (float)(4.0D + this.random.nextDouble() * 1.5D * d0), false, Level.ExplosionInteraction.TNT);
            this.discard();
        }

    }

    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (pFallDistance >= 3.0F) {
            float f = pFallDistance / 10.0F;
            this.explode((double)(f * f));
        }

        return super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }

    /**
     * Called every tick the minecart is on an activator rail.
     */
    public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
        if (pReceivingPower && this.fuse < 0) {
            this.primeFuse();
        }

    }

    /**
     * Handles an entity event received from a {@link net.minecraft.network.protocol.game.ClientboundEntityEventPacket}.
     */
    public void handleEntityEvent(byte pId) {
        if (pId == 10) {
            this.primeFuse();
        } else {
            super.handleEntityEvent(pId);
        }

    }

    /**
     * Ignites this TNT cart.
     */
    public void primeFuse() {
        this.fuse = 80;
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)10);
            if (!this.isSilent()) {
                this.level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

    }

    /**
     * Gets the remaining fuse time in ticks.
     */
    public int getFuse() {
        return this.fuse;
    }

    /**
     * Returns {@code true} if the TNT minecart is ignited.
     */
    public boolean isPrimed() {
        return this.fuse > -1;
    }

    /**
     * Explosion resistance of a block relative to this entity
     */
    public float getBlockExplosionResistance(Explosion pExplosion, BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, float pExplosionPower) {
        return !this.isPrimed() || !pBlockState.is(BlockTags.RAILS) && !pLevel.getBlockState(pPos.above()).is(BlockTags.RAILS) ? super.getBlockExplosionResistance(pExplosion, pLevel, pPos, pBlockState, pFluidState, pExplosionPower) : 0.0F;
    }

    public boolean shouldBlockExplode(Explosion pExplosion, BlockGetter pLevel, BlockPos pPos, BlockState pBlockState, float pExplosionPower) {
        return !this.isPrimed() || !pBlockState.is(BlockTags.RAILS) && !pLevel.getBlockState(pPos.above()).is(BlockTags.RAILS) ? super.shouldBlockExplode(pExplosion, pLevel, pPos, pBlockState, pExplosionPower) : false;
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("TNTFuse", 99)) {
            this.fuse = pCompound.getInt("TNTFuse");
        }

    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("TNTFuse", this.fuse);
    }
}