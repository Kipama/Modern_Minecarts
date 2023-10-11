package net.lordkipama.modernminecarts.entity;

import net.lordkipama.modernminecarts.Item.ModItems;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.level.block.Block.canSupportRigidBlock;

public class CustomMinecartEntity extends AbstractMinecart {
    private boolean jumpedOffSlope = false;
    private double maxSpeed = 8;

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

    /*  Standard speed values:
    public static float DEFAULT_MAX_SPEED_AIR_LATERAL = 0.4f;
    public static float DEFAULT_MAX_SPEED_AIR_VERTICAL = -1.0f;
    public static double DEFAULT_AIR_DRAG = 0.95f;
    */

    @Override
    public double getDragAir() {
        return 0.95f; //0.975f
    }

    @Override
    public float getMaxSpeedAirLateral() {
        return 0.8f;
    }

    @Override
    protected double getMaxSpeed() {
        return maxSpeed;
    }


    @Override
    public void comeOffTrack() {

        double d0 = this.onGround() ? this.getMaxSpeed() : getMaxSpeedAirLateral(); //getMaxSpeedAirLateral()
        Vec3 vec3 = this.getDeltaMovement();

        //Get hindblock
        int x = Mth.floor(this.getX());
        int y = Mth.floor(this.getY());
        int z = Mth.floor(this.getZ());
        BlockState hindBlockState = null;
        double lateralMomentum = 0.0;
        Vec3 newVec3 = null;

        if(vec3.x>0){
            BlockPos blockpos = new BlockPos(x-1, y-1, z);
            lateralMomentum = Math.abs(vec3.x);
            hindBlockState = this.level().getBlockState(blockpos);
            newVec3 = new Vec3(Math.min(vec3.x+0.1, maxSpeed),Math.min(lateralMomentum, maxSpeed),0);
        }
        else if(vec3.x<0){
            BlockPos blockpos = new BlockPos(x+1, y-1, z);
            lateralMomentum = Math.abs(vec3.x);
            hindBlockState = this.level().getBlockState(blockpos);
            newVec3 = new Vec3(Math.max(vec3.x-0.1, -maxSpeed),Math.min(lateralMomentum, maxSpeed),0);
        }
        else if(vec3.z>0){
            BlockPos blockpos = new BlockPos(x, y-1, z-1);
            lateralMomentum = Math.abs(vec3.z);
            hindBlockState = this.level().getBlockState(blockpos);
            newVec3 = new Vec3(0,Math.min(lateralMomentum, maxSpeed),Math.min(vec3.z+0.1, maxSpeed));
        }
        else {
            BlockPos blockpos = new BlockPos(x, y-1, z+1);
            lateralMomentum = Math.abs(vec3.z);
            hindBlockState = this.level().getBlockState(blockpos);
            newVec3 = new Vec3(0,Math.min(lateralMomentum, maxSpeed),Math.max(vec3.z-0.1, -maxSpeed));
        }

        if(jumpedOffSlope ==false && hindBlockState.is(ModBlocks.SLOPED_RAIL.get())){
            //modify vec3 if rail is ramp
            vec3 = newVec3;

            jumpedOffSlope = true;
        }
        else if(!hindBlockState.is(ModBlocks.SLOPED_RAIL.get())){
            jumpedOffSlope = false;
        }


        this.setDeltaMovement(Mth.clamp(vec3.x, -d0, d0), vec3.y, Mth.clamp(vec3.z, -d0, d0));
        if (this.onGround() && vec3.y <= 0) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
        }

        if (getMaxSpeedAirVertical() > 0 && getDeltaMovement().y > getMaxSpeedAirVertical()) {
            if (Math.abs(getDeltaMovement().x) < 0.3f && Math.abs(getDeltaMovement().z) < 0.3f)
                setDeltaMovement(new Vec3(getDeltaMovement().x, 0.15f, getDeltaMovement().z));
            else
                setDeltaMovement(new Vec3(getDeltaMovement().x, getMaxSpeedAirVertical(), getDeltaMovement().z));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(getDragAir()));
        }
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




    @Override
    public void moveMinecartOnRail(BlockPos pos) { //Non-default because getMaximumSpeed is protected
        CustomMinecartEntity mc = this;
        double d24 = mc.isVehicle() ? 0.75D : 1.0D;
        double d25 = mc.getMaxSpeedWithRail();
        Vec3 vec3d1 = mc.getDeltaMovement();
        maxSpeed = getMaxSpeedWithRail();


        vec3d1 = new Vec3(Math.min(Math.max(vec3d1.x(), -maxSpeed), maxSpeed),0,Math.min(Math.max(vec3d1.z(), -maxSpeed), maxSpeed));



        setDeltaMovement(vec3d1);
        System.out.println(vec3d1);
        System.out.println(maxSpeed);
        mc.move(MoverType.SELF, new Vec3(Mth.clamp(d24 * vec3d1.x, -d25, d25), 0.0D, Mth.clamp(d24 * vec3d1.z, -d25, d25)));
    }
}