package net.lordkipama.modernminecarts.entity;


import net.lordkipama.modernminecarts.RailSpeeds;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public abstract class CustomAbstractMinecartEntity extends AbstractMinecart {
    private boolean jumpedOffSlope = false;
    private double maxSpeed = 8;

    protected CustomAbstractMinecartEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    protected CustomAbstractMinecartEntity(EntityType<?> pEntityType, Level pLevel, double pX, double pY, double pZ) {
        super(pEntityType, pLevel, pX, pY, pZ);
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

        BlockPos belowBlock = new BlockPos(x, y-1,z);
        if(jumpedOffSlope ==false && hindBlockState.is(ModBlocks.SLOPED_RAIL.get()) && this.level().getBlockState(belowBlock).is(Blocks.AIR)){
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

    @Override
    public double getDragAir() {
        return 0.9625f; //Original 0.95f
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
    public void moveMinecartOnRail(BlockPos pos) { //Non-default because getMaximumSpeed is protected
        CustomAbstractMinecartEntity mc = this;
        double d24 = mc.isVehicle() ? 0.75D : 1.0D;
        double d25 = mc.getMaxSpeedWithRail();
        Vec3 vec3d1 = mc.getDeltaMovement();
        maxSpeed = getMaxSpeedWithRail();


        vec3d1 = new Vec3(Math.min(Math.max(vec3d1.x(), -maxSpeed), maxSpeed),0,Math.min(Math.max(vec3d1.z(), -maxSpeed), maxSpeed));

        setDeltaMovement(vec3d1);
        mc.move(MoverType.SELF, new Vec3(Mth.clamp(d24 * vec3d1.x, -d25, d25), 0.0D, Mth.clamp(d24 * vec3d1.z, -d25, d25)));
    }

    @Override
    public double getMaxSpeedWithRail() { //Non-default because getMaximumSpeed is protected
        if (!canUseRail()) return getMaxSpeed();
        BlockPos pos = this.getCurrentRailPosition();
        BlockState state = this.level().getBlockState(pos);
        if (!state.is(BlockTags.RAILS)) return getMaxSpeed();

        float railMaxSpeed = ((BaseRailBlock)state.getBlock()).getRailMaxSpeed(state, this.level(), pos, this);

        if(railMaxSpeed > RailSpeeds.max_ascending_speed) {
            Vec3 vec3 = getDeltaMovement();
            BlockState frontBlockState = state;
            BlockPos blockpos = pos;
            if (vec3.x > 0) {
                blockpos = new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
                frontBlockState = this.level().getBlockState(blockpos);
            } else if (vec3.x < 0) {
                blockpos = new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
                frontBlockState = this.level().getBlockState(blockpos);
            } else if (vec3.z > 0) {
                blockpos = new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
                frontBlockState = this.level().getBlockState(blockpos);
            } else {
                blockpos = new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
                frontBlockState = this.level().getBlockState(blockpos);
            }

            if (frontBlockState.is(BlockTags.RAILS)) {
                try {
                    final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE;
                    if (frontBlockState.getValue(SHAPE).isAscending()) {
                        railMaxSpeed = ((BaseRailBlock) frontBlockState.getBlock()).getRailMaxSpeed(frontBlockState, this.level(), blockpos, this);
                    }
                } catch (Exception e) {
                    try {
                        final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
                        if (frontBlockState.getValue(SHAPE).isAscending()) {
                            railMaxSpeed = ((BaseRailBlock) frontBlockState.getBlock()).getRailMaxSpeed(frontBlockState, this.level(), blockpos, this); //((BaseRailBlock) state.getBlock()).getRailMaxSpeed(frontBlockState, this.level(), pos, this);
                        }
                    } catch (Exception f) {
                    }
                }
            }
        }

        return Math.min(railMaxSpeed, getCurrentCartSpeedCapOnRail());
    }
}