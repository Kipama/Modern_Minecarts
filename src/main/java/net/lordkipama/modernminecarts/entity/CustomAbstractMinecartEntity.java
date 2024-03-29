package net.lordkipama.modernminecarts.entity;


import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.lordkipama.modernminecarts.Proxy.ModernMinecartsPacketHandler;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//Parts of this code is based on the work of Cammie.
//Namely everything related to ChainMinecartInterface, except for connecting of chained inventories
/*Copyright (C) 2022 Cammie

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to use, copy, modify, and/or merge copies of the
Software, and to permit persons to whom the Software is furnished to do so, subject to the following
restrictions:

 1) The above copyright notice and this permission notice shall be included in all copies or substantial
    portions of the Software.
 2) You include attribution to the copyright holder(s) in public display of any project that uses any
    portion of the Software.
 3) You may not publish or distribute substantial portions of the Software in its compiled or uncompiled
    forms without prior permission from the copyright holder.
 4) The Software does not make up a substantial portion of your own projects.
*/

public abstract class CustomAbstractMinecartEntity extends AbstractMinecart implements ChainMinecartInterface{
    private boolean jumpedOffSlope = false;
    private double maxSpeed = 0.8;

    private  @Nullable UUID parentUUID;
    private  @Nullable UUID childUUID;
    private int parentIdClient;
    private int childIdClient;
    private boolean flipped;
    private int lSteps;
    private double lx;
    private double ly;
    private double lz;
    private double lyr;
    private double lxr;
    private double lxd;
    private double lyd;
    private double lzd;



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
        BlockState hindBlockState;
        double lateralMomentum;
        Vec3 newVec3;

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
        if(!jumpedOffSlope && hindBlockState.is(ModBlocks.SLOPED_RAIL.get()) && this.level().getBlockState(belowBlock).is(Blocks.AIR)){
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
        return 0.975f; //Original 0.95f
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
        double d24 =1.0D;
        double d25 = mc.getMaxSpeedWithRail();
        Vec3 vec3d1 = mc.getDeltaMovement();
        maxSpeed = d25;
        if(this.getLinkedParent()!=null) {
            double distance = getLinkedParent().distanceTo(this) - 1;
            if (distance > 1.5) {
                d25=d25*1.2;
            }
        }
        vec3d1 = new Vec3(Math.min(Math.max(vec3d1.x(), -d25), d25),0,Math.min(Math.max(vec3d1.z(), -d25), d25));

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
        if(getLinkedParent()!=null){
            railMaxSpeed= 0.8F;
        }

        if(this.isInWater() && !state.is(Blocks.POWERED_RAIL)){
            railMaxSpeed = railMaxSpeed/2;
        }

        if(railMaxSpeed > ModernMinecartsConfig.max_ascending_speed) {
            Vec3 vec3 = getDeltaMovement();
            BlockState frontBlockState;
            BlockPos blockpos;
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
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return Math.min(railMaxSpeed, getCurrentCartSpeedCapOnRail());
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide()) {
            if (getLinkedParent() != null) {
                double distance = getLinkedParent().distanceTo(this) - 1;


                if (distance <= 4) {
                    ModernMinecartsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(()->this), new ModernMinecartsPacketHandler.CouplePacket(getLinkedParent().getId(), this.getId()));

                    Vec3 direction = getLinkedParent().position().subtract(this.position()).normalize();
                    Vec3 parentVelocity = getLinkedParent().getDeltaMovement();

                    if (parentVelocity.length() <0.06) {
                        this.setDeltaMovement(direction);
                        if(distance>1.1){
                            this.setDeltaMovement(this.getDeltaMovement().multiply(0.06*distance, distance, 0.06*distance));
                        }
                        else if(distance>1.02) {
                            this.setDeltaMovement(this.getDeltaMovement().multiply(0.04, distance, 0.04));
                        }
                        else if(distance<0.9){
                            this.setDeltaMovement(this.getDeltaMovement().multiply(-0.3,distance,-0.15));
                        }
                        else if(distance<0.98){
                            this.setDeltaMovement(this.getDeltaMovement().multiply(-0.04,distance,-0.04));
                        }
                        else{
                            this.setDeltaMovement(this.getDeltaMovement().multiply(0, distance, 0));
                        }
                    }
                    else {
                        double newDistance = distance-0.33;

                        this.setDeltaMovement(direction.multiply(parentVelocity.length(), parentVelocity.length(), parentVelocity.length()));
                        this.setDeltaMovement(this.getDeltaMovement().multiply(Math.pow(newDistance,2), newDistance, Math.pow(newDistance,2)));
                    }
                } else {
                    ChainMinecartInterface.unsetParentChild(this.getLinkedParent(), this);
                    level().addFreshEntity(new ItemEntity(level(),this.getX(), this.getY(), this.getZ(), new ItemStack(Items.CHAIN)));
                    //dropStack(new ItemStack(Items.CHAIN));
                    return;
                }

                if (getLinkedParent() != null) {
                    if (getLinkedParent().isRemoved()) {
                        ChainMinecartInterface.unsetParentChild(getLinkedParent(), this);
                        refreshTrain(false);
                    }
                }
                else if(parentUUID!=null){
                    refreshTrain(false);
                    parentUUID=null;
                }
            }


            if (getLinkedChild() != null){
                if (getLinkedChild().isRemoved()) {
                    ChainMinecartInterface.unsetParentChild(this, getLinkedChild());
                    refreshTrain(true);
                }
            }
            else if(childUUID!=null){
                refreshTrain(true);
                childUUID=null;
            }
        }
        if(this.getDeltaMovement().length()<0.005){
            this.setDeltaMovement(0,this.getDeltaMovement().y,0);
        }
        //Super's tick modified
        if (this.getHurtTime() > 0) {
            this.setHurtTime(this.getHurtTime() - 1);
        }

        if (this.getDamage() > 0.0F) {
            this.setDamage(this.getDamage() - 1.0F);
        }

        this.checkBelowWorld();
        this.handleNetherPortal();
        if (this.level().isClientSide) {
            if (this.lSteps > 0) {
                double d5 = this.getX() + (this.lx - this.getX()) / (double)this.lSteps;
                double d6 = this.getY() + (this.ly - this.getY()) / (double)this.lSteps;
                double d7 = this.getZ() + (this.lz - this.getZ()) / (double)this.lSteps;
                double d2 = Mth.wrapDegrees(this.lyr - (double)this.getYRot());
                this.setYRot(this.getYRot() + (float)d2 / (float)this.lSteps);
                this.setXRot(this.getXRot() + (float)(this.lxr - (double)this.getXRot()) / (float)this.lSteps);
                --this.lSteps;
                this.setPos(d5, d6, d7);
                this.setRot(this.getYRot(), this.getXRot());
            } else {
                this.reapplyPosition();
                this.setRot(this.getYRot(), this.getXRot());
            }

        } else {
            if (!this.isNoGravity()) {
                double d0 = this.isInWater() ? -0.005D : -0.04D;
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d0, 0.0D));
            }

            int k = Mth.floor(this.getX());
            int i = Mth.floor(this.getY());
            int j = Mth.floor(this.getZ());
            if (this.level().getBlockState(new BlockPos(k, i - 1, j)).is(BlockTags.RAILS)) {
                --i;
            }

            BlockPos blockpos = new BlockPos(k, i, j);
            BlockState blockstate = this.level().getBlockState(blockpos);
            boolean onRails = BaseRailBlock.isRail(blockstate);
            if (canUseRail() && onRails) {
                this.moveAlongTrack(blockpos, blockstate);
                if (blockstate.getBlock() instanceof PoweredRailBlock && ((PoweredRailBlock) blockstate.getBlock()).isActivatorRail()) {
                    this.activateMinecart(k, i, j, blockstate.getValue(PoweredRailBlock.POWERED));
                }
            } else {
                this.comeOffTrack();
            }

            this.checkInsideBlocks();
            this.setXRot(0.0F);
            double d1 = this.xo - this.getX();
            double d3 = this.zo - this.getZ();
            if (d1 * d1 + d3 * d3 > 0.001D) {
                this.setYRot((float)(Mth.atan2(d3, d1) * 180.0D / Math.PI));
                if (this.flipped) {
                    this.setYRot(this.getYRot() + 180.0F);
                }
            }

            double d4 = Mth.wrapDegrees(this.getYRot() - this.yRotO);
            if (d4 < -170.0D || d4 >= 170.0D) {
                this.setYRot(this.getYRot() + 180.0F);
                this.flipped = !this.flipped;
            }

            this.setRot(this.getYRot(), this.getXRot());
            AABB box;

            //Don't trust IntelliJ! This condition is in fact not always true.
            if (getCollisionHandler() != null) box = getCollisionHandler().getMinecartCollisionBox(this);
            else                               box = this.getBoundingBox().inflate(0.2F, 0.0D, 0.2F);
            if (canBeRidden() && this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
                List<Entity> list = this.level().getEntities(this, box, EntitySelector.pushableBy(this));
                if (!list.isEmpty()) {
                    for (Entity entity1 : list) {
                        if (!(entity1 instanceof Player) && !(entity1 instanceof IronGolem) && !(entity1 instanceof AbstractMinecart) && !this.isVehicle() && !entity1.isPassenger()) {
                            entity1.startRiding(this);
                        } else {
                            entity1.push(this);
                        }
                    }
                }
            } else {
                for(Entity entity : this.level().getEntities(this, box)) {
                    if (!this.hasPassenger(entity) && entity.isPushable() && entity instanceof AbstractMinecart) {
                        entity.push(this);
                    }
                }
            }

            this.updateInWaterStateAndDoFluidPushing();
            if (this.isInLava()) {
                this.lavaHurt();
                this.fallDistance *= 0.5F;
            }

            this.firstTick = false;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if(pCompound.contains("ParentUuid"))
            parentUUID = pCompound.getUUID("ParentUuid");
        if(pCompound.contains("ChildUuid"))
            childUUID = pCompound.getUUID("ChildUuid");

        super.readAdditionalSaveData(pCompound);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        if(this.parentUUID != null)
            pCompound.putUUID("ParentUuid", this.parentUUID);
        if(this.childUUID != null)
            pCompound.putUUID("ChildUuid", this.childUUID);

        super.addAdditionalSaveData(pCompound);
    }



    public CustomAbstractMinecartEntity getLinkedParent() {
        Entity entity = null;

        if(level().isClientSide())
        {
            entity = level().getEntity(this.parentIdClient);
        }
        else {
            if(level() instanceof ServerLevel server) {
                entity = server.getEntity(this.parentUUID);
            }
        }

        if(entity instanceof CustomAbstractMinecartEntity mc){
            return mc;
        }

        else {
            return null;
        }
    }

    public int getParentIdClient(){return parentIdClient;}


    public void setLinkedParent(@Nullable CustomAbstractMinecartEntity parent) {
        if (parent != null) {
            this.parentUUID = parent.getUUID();
            this.parentIdClient = parent.getId();
        } else {
            this.parentUUID = null;
            this.parentIdClient = -1;
        }

        if (!this.level().isClientSide()) {
            ModernMinecartsPacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(()->this), new ModernMinecartsPacketHandler.CouplePacket(this.getParentIdClient(), this.getId()));
        }
        this.startRefreshTrain();
    }


    public void setLinkedParentClient(int id) {
        this.parentIdClient = id;
    }


    public CustomAbstractMinecartEntity getLinkedChild() {
        //var entity = this.world instanceof ServerWorld serverWorld && this.childUuid != null ? serverWorld.getEntity(this.childUuid) : this.world.getEntityById(this.childIdClient);
        //return entity instanceof CustomAbstractMinecartEntity abstractMinecartEntity ? abstractMinecartEntity : null;

        Entity entity = null;

        if(level().isClientSide())
        {
            entity = level().getEntity(childIdClient);
        }
        else {
            if(level() instanceof ServerLevel server) {
                entity = server.getEntity(childUUID);
            }
        }

        if(entity instanceof CustomAbstractMinecartEntity mc){
            return mc;
        }
        else return null;
    }

    public void setLinkedChild(@Nullable CustomAbstractMinecartEntity child) {
        if (child != null) {
            this.childUUID = child.getUUID();
            this.childIdClient = child.getId();
        } else {
            this.childUUID = null;
            this.childIdClient = -1;
        }
        this.startRefreshTrain();
    }


    public void setLinkedChildClient(int id) {
        this.childIdClient = id;
    }

    @Override
    protected void moveAlongTrack(BlockPos pPos, BlockState pState) {
        this.resetFallDistance();
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        Vec3 vec3 = this.getPos(d0, d1, d2);
        d1 = pPos.getY();
        boolean flag = false;
        boolean flag1 = false;
        BaseRailBlock baserailblock = (BaseRailBlock) pState.getBlock();
        if ((baserailblock instanceof PoweredRailBlock && !((PoweredRailBlock) baserailblock).isActivatorRail())) {
            flag = pState.getValue(PoweredRailBlock.POWERED);
            flag1 = !flag;
        }
        else if(baserailblock instanceof PoweredDetectorRailBlock weightedState) {
            flag = pState.getValue(PoweredRailBlock.POWERED);
            flag1 = !flag;
            //Add "start boost"
            Vec3 deltaMovementSpeed = this.getDeltaMovement();
            if(this.getLinkedParent()==null) {

                if (weightedState.getDirInverted(pState)) {
                    if (weightedState.getRailShape(pState).equals(RailShape.NORTH_SOUTH) || weightedState.getRailShape(pState).equals(RailShape.ASCENDING_NORTH) || weightedState.getRailShape(pState).equals(RailShape.ASCENDING_SOUTH)) {
                        if (deltaMovementSpeed.z < -0.2) {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x(), deltaMovementSpeed.y(), deltaMovementSpeed.z() / 8 + 0.1));
                        } else if (flag) {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x(), deltaMovementSpeed.y(), deltaMovementSpeed.z() + 0.02));
                        } else {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x(), deltaMovementSpeed.y(), deltaMovementSpeed.z() / 7));
                        }
                    } else {
                        if (deltaMovementSpeed.x > 0.2) {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x() / 8 - 0.1, deltaMovementSpeed.y(), deltaMovementSpeed.z()));
                        } else if (flag) {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x() - 0.02, deltaMovementSpeed.y(), deltaMovementSpeed.z()));
                        } else {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x() / 7, deltaMovementSpeed.y(), deltaMovementSpeed.z()));
                        }
                    }
                } else {
                    if (weightedState.getRailShape(pState).equals(RailShape.NORTH_SOUTH) || weightedState.getRailShape(pState).equals(RailShape.ASCENDING_NORTH) || weightedState.getRailShape(pState).equals(RailShape.ASCENDING_SOUTH)) {
                        if (deltaMovementSpeed.z > 0.2) {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x(), deltaMovementSpeed.y(), deltaMovementSpeed.z() / 8 - 0.1));
                        } else if (flag) {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x(), deltaMovementSpeed.y(), deltaMovementSpeed.z() - 0.02));
                        } else {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x(), deltaMovementSpeed.y(), deltaMovementSpeed.z() / 7));
                        }
                    } else {
                        if (deltaMovementSpeed.x < -0.2) {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x() / 8 + 0.1, deltaMovementSpeed.y(), deltaMovementSpeed.z()));
                        } else if (flag) {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x() + 0.02, deltaMovementSpeed.y(), deltaMovementSpeed.z()));
                        } else {
                            this.setDeltaMovement(new Vec3(deltaMovementSpeed.x() / 7, deltaMovementSpeed.y(), deltaMovementSpeed.z()));
                        }
                    }
                }
            }
        }
        double d3 = getSlopeAdjustment();
        if (this.isInWater()) {
            d3 *= 0.2D;
        }

        Vec3 vec31 = this.getDeltaMovement();
        RailShape railshape = ((BaseRailBlock)pState.getBlock()).getRailDirection(pState, this.level(), pPos, this);
        switch (railshape) {
            case ASCENDING_EAST -> {
                this.setDeltaMovement(vec31.add(-d3, 0.0D, 0.0D));
                ++d1;
            }
            case ASCENDING_WEST -> {
                this.setDeltaMovement(vec31.add(d3, 0.0D, 0.0D));
                ++d1;
            }
            case ASCENDING_NORTH -> {
                this.setDeltaMovement(vec31.add(0.0D, 0.0D, d3));
                ++d1;
            }
            case ASCENDING_SOUTH -> {
                this.setDeltaMovement(vec31.add(0.0D, 0.0D, -d3));
                ++d1;
            }
        }

        vec31 = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> pair = exits(railshape);
        Vec3i vec3i = pair.getFirst();
        Vec3i vec3i1 = pair.getSecond();
        double d4 = (vec3i1.getX() - vec3i.getX());
        double d5 = (vec3i1.getZ() - vec3i.getZ());
        double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        double d7 = vec31.x * d4 + vec31.z * d5;
        if (d7 < 0.0D) {
            d4 = -d4;
            d5 = -d5;
        }

        double d8 = Math.min(2.0D, vec31.horizontalDistance());
        vec31 = new Vec3(d8 * d4 / d6, vec31.y, d8 * d5 / d6);
        this.setDeltaMovement(vec31);
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Player) {
            Vec3 vec32 = entity.getDeltaMovement();
            double d9 = vec32.horizontalDistanceSqr();
            double d11 = this.getDeltaMovement().length();
            if (d9 > 1.0E-4D && d11 < 0.2D) {
                this.setDeltaMovement(this.getDeltaMovement().add(vec32.x * 0.3D, 0.0D, vec32.z * 0.3D));
                flag1 = false;
            }
        }

        if (flag1 && shouldDoRailFunctions()) {
            double d22 = this.getDeltaMovement().horizontalDistance();
            if (d22 < 0.03D) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
            }
        }

        double d23 = (double)pPos.getX() + 0.5D + (double)vec3i.getX() * 0.5D;
        double d10 = (double)pPos.getZ() + 0.5D + (double)vec3i.getZ() * 0.5D;
        double d12 = (double)pPos.getX() + 0.5D + (double)vec3i1.getX() * 0.5D;
        double d13 = (double)pPos.getZ() + 0.5D + (double)vec3i1.getZ() * 0.5D;
        d4 = d12 - d23;
        d5 = d13 - d10;
        double d14;
        if (d4 == 0.0D) {
            d14 = d2 - (double)pPos.getZ();
        } else if (d5 == 0.0D) {
            d14 = d0 - (double)pPos.getX();
        } else {
            double d15 = d0 - d23;
            double d16 = d2 - d10;
            d14 = (d15 * d4 + d16 * d5) * 2.0D;
        }

        d0 = d23 + d4 * d14;
        d2 = d10 + d5 * d14;
        this.setPos(d0, d1, d2);
        this.moveMinecartOnRail(pPos);
        if (vec3i.getY() != 0 && Mth.floor(this.getX()) - pPos.getX() == vec3i.getX() && Mth.floor(this.getZ()) - pPos.getZ() == vec3i.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)vec3i.getY(), this.getZ());
        } else if (vec3i1.getY() != 0 && Mth.floor(this.getX()) - pPos.getX() == vec3i1.getX() && Mth.floor(this.getZ()) - pPos.getZ() == vec3i1.getZ()) {
            this.setPos(this.getX(), this.getY() + (double)vec3i1.getY(), this.getZ());
        }

        this.applyNaturalSlowdown();
        Vec3 vec33 = this.getPos(this.getX(), this.getY(), this.getZ());
        if (vec33 != null && vec3 != null) {
            double d17 = (vec3.y - vec33.y) * 0.05D;
            Vec3 vec34 = this.getDeltaMovement();
            double d18 = vec34.horizontalDistance();
            if (d18 > 0.0D) {
                this.setDeltaMovement(vec34.multiply((d18 + d17) / d18, 1.0D, (d18 + d17) / d18));
            }

            this.setPos(this.getX(), vec33.y, this.getZ());
        }

        int j = Mth.floor(this.getX());
        int i = Mth.floor(this.getZ());
        if (j != pPos.getX() || i != pPos.getZ()) {
            Vec3 vec35 = this.getDeltaMovement();
            double d26 = vec35.horizontalDistance();
            this.setDeltaMovement(d26 * (double)(j - pPos.getX()), vec35.y, d26 * (double)(i - pPos.getZ()));
        }

        if (shouldDoRailFunctions())
            baserailblock.onMinecartPass(pState, level(), pPos, this);

        if (flag && shouldDoRailFunctions()) {
            Vec3 vec36 = this.getDeltaMovement();
            double d27 = vec36.horizontalDistance();
            if (d27 > 0.01D) {
                this.setDeltaMovement(vec36.add(vec36.x / d27 * 0.06D, 0.0D, vec36.z / d27 * 0.06D));
            } else {
                Vec3 vec37 = this.getDeltaMovement();
                double d20 = vec37.x;
                double d21 = vec37.z;
                if (railshape == RailShape.EAST_WEST) {
                    if (this.isRedstoneConductor(pPos.west())) {
                        d20 = 0.02D;
                    } else if (this.isRedstoneConductor(pPos.east())) {
                        d20 = -0.02D;
                    }
                } else {
                    if (railshape != RailShape.NORTH_SOUTH) {
                        return;
                    }

                    if (this.isRedstoneConductor(pPos.north())) {
                        d21 = 0.02D;
                    } else if (this.isRedstoneConductor(pPos.south())) {
                        d21 = -0.02D;
                    }
                }

                this.setDeltaMovement(d20, vec37.y, d21);
            }
        }

    }


    private boolean isRedstoneConductor(BlockPos pPos) {
        return this.level().getBlockState(pPos).isRedstoneConductor(this.level(), pPos);
    }

    private static Pair<Vec3i, Vec3i> exits(RailShape pShape) {
        return EXITS.get(pShape);
    }

    private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Util.make(Maps.newEnumMap(RailShape.class), (p_38135_) -> {
        Vec3i vec3i = Direction.WEST.getNormal();
        Vec3i vec3i1 = Direction.EAST.getNormal();
        Vec3i vec3i2 = Direction.NORTH.getNormal();
        Vec3i vec3i3 = Direction.SOUTH.getNormal();
        Vec3i vec3i4 = vec3i.below();
        Vec3i vec3i5 = vec3i1.below();
        Vec3i vec3i6 = vec3i2.below();
        Vec3i vec3i7 = vec3i3.below();
        p_38135_.put(RailShape.NORTH_SOUTH, Pair.of(vec3i2, vec3i3));
        p_38135_.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i1));
        p_38135_.put(RailShape.ASCENDING_EAST, Pair.of(vec3i4, vec3i1));
        p_38135_.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i5));
        p_38135_.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i2, vec3i7));
        p_38135_.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i6, vec3i3));
        p_38135_.put(RailShape.SOUTH_EAST, Pair.of(vec3i3, vec3i1));
        p_38135_.put(RailShape.SOUTH_WEST, Pair.of(vec3i3, vec3i));
        p_38135_.put(RailShape.NORTH_WEST, Pair.of(vec3i2, vec3i));
        p_38135_.put(RailShape.NORTH_EAST, Pair.of(vec3i2, vec3i1));
    });

    @Override
    protected void applyNaturalSlowdown() {
        double d0 =0.997D;
        Vec3 vec3 = this.getDeltaMovement();
        vec3 = vec3.multiply(d0, 0.0D, d0);
        if (this.isInWater()) {
            vec3 = vec3.scale(0.95F);
        }

        this.setDeltaMovement(vec3);
    }

    @Override
    public void lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport) {
        this.lx = pX;
        this.ly = pY;
        this.lz = pZ;
        this.lyr = pYaw;
        this.lxr = pPitch;
        this.lSteps = pPosRotationIncrements + 2;
        this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
        super.lerpTo(pX,pY,pZ,pYaw,pPitch, pPosRotationIncrements, pTeleport);
    }

    @Override
    public void lerpMotion(double pX, double pY, double pZ) {
        this.lxd = pX;
        this.lyd = pY;
        this.lzd = pZ;
        this.setDeltaMovement(this.lxd, this.lyd, this.lzd);
        super.lerpMotion(pX, pY, pZ);
    }

    public List<ContainerEntity> getContainerMinecartItemstacks(List<ContainerEntity> entries, boolean directionUp, boolean checkHoppers){
        CustomAbstractMinecartEntity parent = this.getLinkedParent();
        CustomAbstractMinecartEntity child = this.getLinkedChild();



        if(directionUp && parent!=null){
            entries.addAll(parent.getContainerMinecartItemstacks(entries, true, checkHoppers));
        }
        else if(!directionUp && child!=null){
            entries.addAll(child.getContainerMinecartItemstacks(entries, false, checkHoppers));
        }

        if((this instanceof CustomMinecartHopperEntity && !checkHoppers)|| this instanceof CustomMinecartFurnaceEntity){
            return entries;
        }
        else if(this instanceof ContainerEntity imacontainer){
            entries.add(imacontainer);
            entries.add(imacontainer);
        }
        return entries;
    }

    public void startRefreshTrain(){
        refreshTrain(true);
        refreshTrain(false);
    }

    public void refreshTrain(boolean directionUp){
        if(!directionUp && this.getLinkedChild()!=null){
            this.getLinkedChild().refreshTrain(false);
        }
        else if(this.getLinkedParent()!=null) {
                this.getLinkedParent().refreshTrain(true);
        }


        if(this instanceof CustomMinecartHopperEntity hopperMc){
            hopperMc.getChainInventories();
        }
        else if(this instanceof  CustomMinecartFurnaceEntity furnaceMc){
            furnaceMc.numberOfChildren = furnaceMc.getNumberOfChildren();
            furnaceMc.getChainInventories();
        }
    }

    public int getNumberOfChildren(){
        if(this.getLinkedChild()!=null){
            return this.getLinkedChild().getNumberOfChildren()+1;
        }
        else{
            return 0;
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (!this.level().isClientSide && !this.isRemoved()) {
            if (this.isInvulnerableTo(pSource)) {
                return false;
            } else {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.markHurt();
                this.setDamage(this.getDamage() + pAmount * 10.0F);
                this.gameEvent(GameEvent.ENTITY_DAMAGE, pSource.getEntity());
                boolean flag = pSource.getEntity() instanceof Player && ((Player)pSource.getEntity()).getAbilities().instabuild;
                if(this.getLinkedChild()!=null){
                    ChainMinecartInterface.unsetParentChild(this, this.getLinkedChild());
                    level().addFreshEntity(new ItemEntity(level(),this.getX(), this.getY(), this.getZ(), new ItemStack(Items.CHAIN)));
                }

                if (flag || this.getDamage() > 40.0F) {
                    this.ejectPassengers();
                    if (flag && !this.hasCustomName()) {
                        this.discard();
                    } else {
                        this.destroy(pSource);
                    }
                }

                return true;
            }
        } else {
            return true;
        }
    }
}

