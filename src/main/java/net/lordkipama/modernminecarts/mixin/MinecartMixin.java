package net.lordkipama.modernminecarts.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.interfaces.ContainerMinecartInteface;
import net.lordkipama.modernminecarts.logic.MinecartTuning;
import net.lordkipama.modernminecarts.logic.PoweredDetectorMotion;
import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.util.Uuids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.UUID;


@Mixin(AbstractMinecartEntity.class)
public class MinecartMixin implements ChainMinecartInterface {
    @Unique private @Nullable UUID parentUuid;
    @Unique private @Nullable UUID childUuid;

    @Unique private int parentIdClient;
    @Unique private int childIdClient;
    @Unique private boolean jumpedOffSlope = false;
    @Unique private double maxSpeed = 0.8;
    /**
     * @author LordKipama
     * @reason Mixins are scary and I'm too bad to add everything separately
    */
    @Overwrite
    public void moveOnRail(ServerWorld world) {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity)(Object)this;
        BlockPos pos = thisObject.getRailOrMinecartPos();
        BlockState state = world.getBlockState(pos);
        double w;
        Vec3d vec3d5;
        double u;
        double t;
        double s;
        thisObject.onLanding();
        double d = thisObject.getX();
        double e = thisObject.getY();
        double f = thisObject.getZ();
        Vec3d vec3d = modernminecarts$snapPositionToRail(d, e, f);
        e = pos.getY();
        boolean bl = false;
        boolean bl2 = false;
        //if (state.isOf(Blocks.POWERED_RAIL) || state.isOf(ModBlocks.COPPER_RAIL) || state.isOf(ModBlocks.COPPER_RAIL) || state.isOf(ModBlocks.EXPOSED_COPPER_RAIL) || state.isOf(ModBlocks.WEATHERED_COPPER_RAIL) || state.isOf(ModBlocks.OXIDIZED_COPPER_RAIL)) {
        if(state.getBlock()  instanceof PoweredRailBlock){
            bl = state.get(PoweredRailBlock.POWERED);
            bl2 = !bl;
        } else if (state.getBlock() instanceof PoweredDetectorRailBlock detectorRail) {
            bl = state.get(PoweredDetectorRailBlock.POWERED);
            bl2 = !bl;
            modernminecarts$applyPoweredDetectorMotion(thisObject, state, detectorRail, bl);
        }
        double g = 0.0078125;
        if (thisObject.isTouchingWater()) {
            g *= 0.2;
        }
        Vec3d vec3d2 = thisObject.getVelocity();
        RailShape railShape;
        if(state.isOf(ModBlocks.RAIL_CROSSING)){

            if (Math.abs(vec3d2.z) > Math.abs(vec3d2.x)) {
                railShape = RailShape.NORTH_SOUTH;
            }
            else {
                railShape = RailShape.EAST_WEST;
            }
        }
        else {
            railShape = state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty());
        }
        switch (railShape) {
            case ASCENDING_EAST: {
                thisObject.setVelocity(vec3d2.add(-g, 0.0, 0.0));
                e += 1.0;
                break;
            }
            case ASCENDING_WEST: {
                thisObject.setVelocity(vec3d2.add(g, 0.0, 0.0));
                e += 1.0;
                break;
            }
            case ASCENDING_NORTH: {
                thisObject.setVelocity(vec3d2.add(0.0, 0.0, g));
                e += 1.0;
                break;
            }
            case ASCENDING_SOUTH: {
                thisObject.setVelocity(vec3d2.add(0.0, 0.0, -g));
                e += 1.0;
            }
        }
        vec3d2 = thisObject.getVelocity();

        Pair<Vec3i, Vec3i> pair = MinecartInvoker.invokeGetAdjacentRailPositionsByShape(railShape);
        Vec3i vec3i = pair.getFirst();
        Vec3i vec3i2 = pair.getSecond();
        double h = vec3i2.getX() - vec3i.getX();
        double i = vec3i2.getZ() - vec3i.getZ();
        double j = Math.sqrt(h * h + i * i);
        double k = vec3d2.x * h + vec3d2.z * i;
        if (k < 0.0) {
            h = -h;
            i = -i;
        }
        double l = Math.min(2.0, vec3d2.horizontalLength());
        vec3d2 = new Vec3d(l * h / j, vec3d2.y, l * i / j);
        thisObject.setVelocity(vec3d2);
        Entity entity = thisObject.getFirstPassenger();
        if (entity instanceof PlayerEntity) {
            Vec3d vec3d3 = entity.getVelocity();
            double m = vec3d3.horizontalLengthSquared();
            double n = thisObject.getVelocity().horizontalLengthSquared();
            if (m > 1.0E-4 && n < 0.01) {
                thisObject.setVelocity(thisObject.getVelocity().add(vec3d3.x * 0.1, 0.0, vec3d3.z * 0.1));
                bl2 = false;
            }
        }
        if (bl2) {
            double o = thisObject.getVelocity().horizontalLength();
            if (o < 0.03) {
                thisObject.setVelocity(Vec3d.ZERO);
            } else {
                thisObject.setVelocity(thisObject.getVelocity().multiply(0.5, 0.0, 0.5));
            }
        }
        double o = (double)pos.getX() + 0.5 + (double)vec3i.getX() * 0.5;
        double p = (double)pos.getZ() + 0.5 + (double)vec3i.getZ() * 0.5;
        double q = (double)pos.getX() + 0.5 + (double)vec3i2.getX() * 0.5;
        double r = (double)pos.getZ() + 0.5 + (double)vec3i2.getZ() * 0.5;
        h = q - o;
        i = r - p;
        if (h == 0.0) {
            s = f - (double)pos.getZ();
        } else if (i == 0.0) {
            s = d - (double)pos.getX();
        } else {
            t = d - o;
            u = f - p;
            s = (t * h + u * i) * 2.0;
        }
        d = o + h * s;
        f = p + i * s;
        thisObject.setPosition(d, e, f);
        t = 1; //REMOVED Slowdown for minecart with passangers thisObject.hasPassengers() ? 0.75 : 1.0;
        u =((MinecartInvoker) thisObject).invokeGetMaxSpeed(world);
        vec3d2 = thisObject.getVelocity();
        vec3d2 = new Vec3d(Math.min(Math.max(vec3d2.x, -u), u),0,Math.min(Math.max(vec3d2.z, -u), u));

        thisObject.setVelocity(vec3d2);

        thisObject.move(MovementType.SELF, new Vec3d(MathHelper.clamp(t * vec3d2.x, -u, u), 0.0, MathHelper.clamp(t * vec3d2.z, -u, u)));
        if (vec3i.getY() != 0 && MathHelper.floor(thisObject.getX()) - pos.getX() == vec3i.getX() && MathHelper.floor(thisObject.getZ()) - pos.getZ() == vec3i.getZ()) {
            thisObject.setPosition(thisObject.getX(), thisObject.getY() + (double)vec3i.getY(), thisObject.getZ());
        } else if (vec3i2.getY() != 0 && MathHelper.floor(thisObject.getX()) - pos.getX() == vec3i2.getX() && MathHelper.floor(thisObject.getZ()) - pos.getZ() == vec3i2.getZ()) {
            thisObject.setPosition(thisObject.getX(), thisObject.getY() + (double)vec3i2.getY(), thisObject.getZ());
        }
        thisObject.setVelocity(((MinecartInvoker) thisObject).invokeApplySlowdown(thisObject.getVelocity()));
        Vec3d vec3d4 = modernminecarts$snapPositionToRail(thisObject.getX(), thisObject.getY(), thisObject.getZ());
        if (vec3d4 != null && vec3d != null) {
            double v = (vec3d.y - vec3d4.y) * 0.05;
            vec3d5 = thisObject.getVelocity();
            w = vec3d5.horizontalLength();
            if (w > 0.0) {
                thisObject.setVelocity(vec3d5.multiply((w + v) / w, 1.0, (w + v) / w));
            }
            thisObject.setPosition(thisObject.getX(), vec3d4.y, thisObject.getZ());
        }
        int x = MathHelper.floor(thisObject.getX());
        int y = MathHelper.floor(thisObject.getZ());
        if (x != pos.getX() || y != pos.getZ()) {
            vec3d5 = thisObject.getVelocity();
            w = vec3d5.horizontalLength();
            thisObject.setVelocity(w * (double)(x - pos.getX()), vec3d5.y, w * (double)(y - pos.getZ()));
        }
        if (bl) {
            vec3d5 = thisObject.getVelocity();
            w = vec3d5.horizontalLength();
            if (w > 0.01) {
                double z = 0.06;
                thisObject.setVelocity(vec3d5.add(vec3d5.x / w * 0.06, 0.0, vec3d5.z / w * 0.06));
            } else {
                Vec3d vec3d6 = thisObject.getVelocity();
                double aa = vec3d6.x;
                double ab = vec3d6.z;
                if (railShape == RailShape.EAST_WEST) {
                    if (((MinecartInvoker) thisObject).invokeWillHitBlockAt(pos.west())) {
                        aa = 0.02;
                    } else if (((MinecartInvoker) thisObject).invokeWillHitBlockAt(pos.east())) {
                        aa = -0.02;
                    }
                } else if (railShape == RailShape.NORTH_SOUTH) {
                    if (((MinecartInvoker) thisObject).invokeWillHitBlockAt(pos.north())) {
                        ab = 0.02;
                    } else if (((MinecartInvoker) thisObject).invokeWillHitBlockAt(pos.south())) {
                        ab = -0.02;
                    }
                } else {
                    return;
                }
                thisObject.setVelocity(aa, vec3d6.y, ab);
            }
        }

    }

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void injectedGetMaxSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity)(Object)this;

        int i = MathHelper.floor(thisObject.getX());
        int j = MathHelper.floor(thisObject.getY());
        int k = MathHelper.floor(thisObject.getZ());
        BlockState block = thisObject.getEntityWorld().getBlockState(new BlockPos(i, j , k));
        BlockState blockUnder = thisObject.getEntityWorld().getBlockState(new BlockPos(i, j - 1, k));


        if (thisObject.isOnRail()){
            //If Rail is flat
            if(block.getBlock().getClass() == CopperRailBlock.class || block.getBlock().getClass() == WaxedCopperRailBlock.class) {
                if(block.isOf(ModBlocks.COPPER_RAIL) || block.isOf(ModBlocks.WAXED_COPPER_RAIL)) {
                    cir.setReturnValue(MinecartTuning.copperRailSpeed());
                }
                else if(block.isOf(ModBlocks.EXPOSED_COPPER_RAIL) || block.isOf(ModBlocks.WAXED_EXPOSED_COPPER_RAIL)) {
                    cir.setReturnValue(MinecartTuning.exposedCopperRailSpeed());
                }
                else if(block.isOf(ModBlocks.WEATHERED_COPPER_RAIL) || block.isOf(ModBlocks.WAXED_WEATHERED_COPPER_RAIL)) {
                    cir.setReturnValue(MinecartTuning.weatheredCopperRailSpeed());
                }
                else if(block.isOf(ModBlocks.OXIDIZED_COPPER_RAIL) || block.isOf(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL)) {
                    cir.setReturnValue(MinecartTuning.oxidizedCopperRailSpeed());
                }
            }
            //If rail is sloped
            else if(blockUnder.getBlock().getClass() == CopperRailBlock.class
                    || blockUnder.getBlock().getClass() == WaxedCopperRailBlock.class) {
                if(block.isOf(ModBlocks.COPPER_RAIL) || block.isOf(ModBlocks.WAXED_COPPER_RAIL)) {
                    cir.setReturnValue(MinecartTuning.ascendingCopperRailSpeed());
                }
                else if(block.isOf(ModBlocks.EXPOSED_COPPER_RAIL) || block.isOf(ModBlocks.WAXED_EXPOSED_COPPER_RAIL)) {
                    cir.setReturnValue(MinecartTuning.ascendingCopperRailSpeed());
                }
                else if(block.isOf(ModBlocks.WEATHERED_COPPER_RAIL) || block.isOf(ModBlocks.WAXED_WEATHERED_COPPER_RAIL)) {
                    cir.setReturnValue(MinecartTuning.weatheredCopperRailSpeed());
                }
                else if(block.isOf(ModBlocks.OXIDIZED_COPPER_RAIL) || block.isOf(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL)) {
                    cir.setReturnValue(MinecartTuning.oxidizedCopperRailSpeed());
                }
            }
            else if(blockUnder.isOf(ModBlocks.RAIL_JUMP)){
                boolean airInFront = false;
                if(blockUnder.get(SlopedRailBlock.SHAPE)== RailShape.ASCENDING_NORTH){
                    airInFront = thisObject.getEntityWorld().getBlockState(new BlockPos(i,j,k-1)).isOf(Blocks.AIR);
                }
                if(blockUnder.get(SlopedRailBlock.SHAPE)== RailShape.ASCENDING_EAST){
                    airInFront = thisObject.getEntityWorld().getBlockState(new BlockPos(i+1,j,k)).isOf(Blocks.AIR);
                }
                else if(blockUnder.get(SlopedRailBlock.SHAPE)== RailShape.ASCENDING_SOUTH){
                    airInFront = thisObject.getEntityWorld().getBlockState(new BlockPos(i,j,k+1)).isOf(Blocks.AIR);
                }
                else if(blockUnder.get(SlopedRailBlock.SHAPE)== RailShape.ASCENDING_WEST){
                    airInFront = thisObject.getEntityWorld().getBlockState(new BlockPos(i-1,j,k)).isOf(Blocks.AIR);
                }

                if(airInFront){
                    cir.setReturnValue(0.8d);
                }
                else {
                    cir.setReturnValue(0.5d);
                }
            }

            if(thisObject.getVelocity().length() > MinecartTuning.ascendingCopperRailSpeed()) {
                Vec3d vec3 =thisObject.getVelocity();
                BlockState frontBlockState;
                BlockPos blockpos;
                BlockPos pos = thisObject.getBlockPos();
                if (vec3.x > 0) {
                    blockpos = new BlockPos(pos.getX() + 1, pos.getY(), pos.getZ());
                    frontBlockState = thisObject.getEntityWorld().getBlockState(blockpos);
                } else if (vec3.x < 0) {
                    blockpos = new BlockPos(pos.getX() - 1, pos.getY(), pos.getZ());
                    frontBlockState = thisObject.getEntityWorld().getBlockState(blockpos);
                } else if (vec3.z > 0) {
                    blockpos = new BlockPos(pos.getX(), pos.getY(), pos.getZ() + 1);
                    frontBlockState = thisObject.getEntityWorld().getBlockState(blockpos);
                } else {
                    blockpos = new BlockPos(pos.getX(), pos.getY(), pos.getZ() - 1);
                    frontBlockState = thisObject.getEntityWorld().getBlockState(blockpos);
                }

                if (frontBlockState.isIn(BlockTags.RAILS)) {
                    try {
                        final EnumProperty<RailShape> SHAPE = Properties.RAIL_SHAPE;

                        if (frontBlockState.get(SHAPE).isAscending()) {
                            if (frontBlockState.isOf(ModBlocks.RAIL_JUMP)) {
                                cir.setReturnValue(modernminecarts$getRailJumpApproachSpeed(thisObject, blockpos, frontBlockState));
                            }
                            else if(block.isOf(ModBlocks.COPPER_RAIL)) {
                                cir.setReturnValue(MinecartTuning.ascendingCopperRailSpeed());
                            }
                            else if(block.isOf(ModBlocks.EXPOSED_COPPER_RAIL)) {
                                cir.setReturnValue(MinecartTuning.ascendingCopperRailSpeed());
                            }
                            else if(block.isOf(ModBlocks.WEATHERED_COPPER_RAIL)) {
                                cir.setReturnValue(MinecartTuning.weatheredCopperRailSpeed());
                            }
                            else if(block.isOf(ModBlocks.OXIDIZED_COPPER_RAIL)) {
                                cir.setReturnValue(MinecartTuning.oxidizedCopperRailSpeed());
                            }
                        }
                    } catch (Exception e) {
                        try {
                            final EnumProperty<RailShape> SHAPE = Properties.STRAIGHT_RAIL_SHAPE;
                            if (frontBlockState.get(SHAPE).isAscending()) {
                                if (frontBlockState.isOf(ModBlocks.RAIL_JUMP)) {
                                    cir.setReturnValue(modernminecarts$getRailJumpApproachSpeed(thisObject, blockpos, frontBlockState));
                                }
                                else if(block.isOf(ModBlocks.COPPER_RAIL)) {
                                    cir.setReturnValue(MinecartTuning.ascendingCopperRailSpeed());
                                }
                                else if(block.isOf(ModBlocks.EXPOSED_COPPER_RAIL)) {
                                    cir.setReturnValue(MinecartTuning.ascendingCopperRailSpeed());
                                }
                                else if(block.isOf(ModBlocks.WEATHERED_COPPER_RAIL)) {
                                    cir.setReturnValue(MinecartTuning.weatheredCopperRailSpeed());
                                }
                                else if(block.isOf(ModBlocks.OXIDIZED_COPPER_RAIL)) {
                                    cir.setReturnValue(MinecartTuning.oxidizedCopperRailSpeed());
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        if (thisObject instanceof ContainerMinecartInteface furnaceMinecart) {
            cir.setReturnValue(furnaceMinecart.limitTrainSpeed(cir.getReturnValue()));
        }

    }

    @Unique
    private void modernminecarts$applyPoweredDetectorMotion(
            AbstractMinecartEntity cart,
            BlockState state,
            PoweredDetectorRailBlock rail,
            boolean powered
    ) {
        if (getLinkedParent() != null) {
            return;
        }

        Vec3d velocity = cart.getVelocity();
        RailShape shape = state.get(PoweredDetectorRailBlock.SHAPE);
        boolean northSouth = shape == RailShape.NORTH_SOUTH
                || shape == RailShape.ASCENDING_NORTH
                || shape == RailShape.ASCENDING_SOUTH;

        PoweredDetectorMotion.Motion adjusted = PoweredDetectorMotion.adjust(
                velocity.x,
                velocity.z,
                northSouth,
                state.get(PoweredDetectorRailBlock.INVERTED),
                powered
        );
        cart.setVelocity(adjusted.x(), velocity.y, adjusted.z());
    }

    /**
     * @author LordKipama
     * @reason Still scary
     */
    @Unique
    private @Nullable Vec3d modernminecarts$snapPositionToRail(double x, double y, double z) {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity) (Object) this;
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);
        BlockState blockState = thisObject.getEntityWorld().getBlockState(new BlockPos(i, j, k));
        if (thisObject.getEntityWorld().getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }
        if (AbstractRailBlock.isRail(blockState)) {
            double p;
            RailShape railShape = blockState.get(((AbstractRailBlock)blockState.getBlock()).getShapeProperty());
            if(blockState.isOf(ModBlocks.RAIL_CROSSING) && blockState.getBlock() instanceof AbstractRailBlock AbstractRail){
                Direction direction = Direction.getFacing(thisObject.getVelocity().getX(), 0, thisObject.getVelocity().getZ());
                if(railShape == RailShape.NORTH_SOUTH && (direction == Direction.EAST || direction == Direction.WEST)) {
                    thisObject.getEntityWorld().setBlockState(new BlockPos(i,j,k), blockState.with(AbstractRail.getShapeProperty(), RailShape.EAST_WEST));
                }

                if(railShape == RailShape.EAST_WEST && (direction == Direction.NORTH || direction == Direction.SOUTH)) {
                    thisObject.getEntityWorld().setBlockState(new BlockPos(i,j,k), blockState.with(AbstractRail.getShapeProperty(), RailShape.NORTH_SOUTH));
                }
            }
            Pair<Vec3i, Vec3i> pair = MinecartInvoker.invokeGetAdjacentRailPositionsByShape(railShape);
            Vec3i vec3i = pair.getFirst();
            Vec3i vec3i2 = pair.getSecond();
            double d = (double)i + 0.5 + (double)vec3i.getX() * 0.5;
            double e = (double)j + 0.0625 + (double)vec3i.getY() * 0.5;
            double f = (double)k + 0.5 + (double)vec3i.getZ() * 0.5;
            double g = (double)i + 0.5 + (double)vec3i2.getX() * 0.5;
            double h = (double)j + 0.0625 + (double)vec3i2.getY() * 0.5;
            double l = (double)k + 0.5 + (double)vec3i2.getZ() * 0.5;
            double m = g - d;
            double n = (h - e) * 2.0;
            double o = l - f;
            if (m == 0.0) {
                p = z - (double)k;
            } else if (o == 0.0) {
                p = x - (double)i;
            } else {
                double q = x - d;
                double r = z - f;
                p = (q * m + r * o) * 2.0;
            }
            x = d + m * p;
            y = e + n * p;
            z = f + o * p;
            if (n < 0.0) {
                y += 1.0;
            } else if (n > 0.0) {
                y += 0.5;
            }
            return new Vec3d(x, y, z);
        }
        return null;
    }

    /**
     * @author LordKipama
     * @reason Basically rewrote the entire thing
    */
    @Overwrite
    public void moveOffRail(ServerWorld world) {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity) (Object) this;

        double d0 = thisObject.isOnGround()
                ? ((MinecartInvoker) thisObject).invokeGetMaxSpeed(world)
                : MinecartTuning.copperRailSpeed();
        Vec3d vec3 = thisObject.getVelocity();

        //Get hindblock
        int x = (int) Math.floor(thisObject.getX());
        int y = (int) Math.floor(thisObject.getY());
        int z = (int) Math.floor(thisObject.getZ());
        BlockState hindBlockState;
        double lateralMomentum;
        Vec3d newVec3;

        //Calculate new momentum
        if(vec3.x>0){
            BlockPos blockpos = new BlockPos(x-1, y-1, z);
            lateralMomentum = Math.abs(vec3.x);
            hindBlockState = thisObject.getEntityWorld().getBlockState(blockpos);
            newVec3 = new Vec3d(Math.min(vec3.x + 0.1D, maxSpeed), Math.min(lateralMomentum, maxSpeed), 0);
        }
        else if(vec3.x<0){
            BlockPos blockpos = new BlockPos(x+1, y-1, z);
            lateralMomentum = Math.abs(vec3.x);
            hindBlockState = thisObject.getEntityWorld().getBlockState(blockpos);
            newVec3 = new Vec3d(Math.max(vec3.x - 0.1D, -maxSpeed), Math.min(lateralMomentum, maxSpeed), 0);
        }
        else if(vec3.z>0){
            BlockPos blockpos = new BlockPos(x, y-1, z-1);
            lateralMomentum = Math.abs(vec3.z);
            hindBlockState = thisObject.getEntityWorld().getBlockState(blockpos);
            newVec3 = new Vec3d(0, Math.min(lateralMomentum, maxSpeed), Math.min(vec3.z + 0.1D, maxSpeed));
        }
        else {
            BlockPos blockpos = new BlockPos(x, y-1, z+1);
            lateralMomentum = Math.abs(vec3.z);
            hindBlockState = thisObject.getEntityWorld().getBlockState(blockpos);
            newVec3 = new Vec3d(0, Math.min(lateralMomentum, maxSpeed), Math.max(vec3.z - 0.1D, -maxSpeed));
        }

        //Check if cart should jump
        BlockPos belowBlock = new BlockPos(x, y-1,z);
        if(!jumpedOffSlope && hindBlockState.isOf(ModBlocks.RAIL_JUMP) && thisObject.getEntityWorld().getBlockState(belowBlock).isOf(Blocks.AIR)){
            //modify vec3 if rail is ramp
            vec3 = newVec3;
            jumpedOffSlope = true;
        }
        else if(!hindBlockState.isOf(ModBlocks.RAIL_JUMP)){
            jumpedOffSlope = false;
        }

        //Set new speed
        thisObject.setVelocity(MathHelper.clamp(vec3.x, -d0, d0), vec3.y, MathHelper.clamp(vec3.z, -d0, d0));

        //Slow if on ground
        if (thisObject.isOnGround() && vec3.y <= 0) {
            thisObject.setVelocity(thisObject.getVelocity().multiply(0.5D));
        }

        //Actually move minecart
        thisObject.move(MovementType.SELF, thisObject.getVelocity());

        //Apply air drag
        if (!thisObject.isOnGround()) {
            thisObject.setVelocity(thisObject.getVelocity().multiply(0.975)); //AIR DRAG
        }
    }


    @Inject(method = "tick", at = @At("HEAD"))
    public void modernminecarts$tick(CallbackInfo info) {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity) (Object) this;
        if(!thisObject.getEntityWorld().isClient()) {
            if(getLinkedParent() != null) {
                double distance = getLinkedParent().distanceTo(thisObject) - 1;

                if(distance <= 4) {
                    Vec3d direction = getLinkedParent().getEntityPos().subtract(thisObject.getEntityPos()).normalize();

                    if(distance > 1) {
                        Vec3d parentVelocity = getLinkedParent().getVelocity();

                        if(parentVelocity.length() == 0) {
                            thisObject.setVelocity(direction.multiply(0.05));
                        }
                        else {
                            thisObject.setVelocity(direction.multiply(parentVelocity.length()));
                            thisObject.setVelocity(thisObject.getVelocity().multiply(distance));
                        }
                    }
                    else if(distance < 0.8)
                        thisObject.setVelocity(direction.multiply(-0.05));
                    else
                        thisObject.setVelocity(Vec3d.ZERO);
                }
                else {
                    ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) getLinkedParent(), this);
                    thisObject.dropStack((ServerWorld) thisObject.getEntityWorld(), new ItemStack(Items.IRON_CHAIN));
                    return;
                }

                if(getLinkedParent().isRemoved())
                    ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) getLinkedParent(), this);
            }


            if(getLinkedChild() != null && getLinkedChild().isRemoved())
                ChainMinecartInterface.unsetParentChild(this, (ChainMinecartInterface) getLinkedChild());

        }
    }
    @Inject(method = "readCustomData", at = @At("HEAD"))
    public void injectReadCustomData(ReadView view, CallbackInfo info) {
        parentUuid = view.read("ParentUuid", Uuids.CODEC).orElse(null);
        childUuid = view.read("ChildUuid", Uuids.CODEC).orElse(null);
    }

    @Inject(method = "writeCustomData", at = @At("HEAD"))
    public void injectWriteCustomData(WriteView view, CallbackInfo info) {
        if (this.parentUuid != null) {
            view.put("ParentUuid", Uuids.CODEC, this.parentUuid);
        }

        if (this.childUuid != null) {
            view.put("ChildUuid", Uuids.CODEC, this.childUuid);
        }
    }

    @Override
    public AbstractMinecartEntity getLinkedParent() {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity) (Object) this;
        var entity = thisObject.getEntityWorld() instanceof ServerWorld serverWorld && this.parentUuid != null ? serverWorld.getEntity(this.parentUuid) : thisObject.getEntityWorld().getEntityById(this.parentIdClient);
        return entity instanceof AbstractMinecartEntity abstractMinecartEntity ? abstractMinecartEntity : null;
    }

    @Override
    public void setLinkedParent(@Nullable AbstractMinecartEntity parent) {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity) (Object) this;
        if (parent != null) {
            this.parentUuid = parent.getUuid();
            this.parentIdClient = parent.getId();
        } else {
            this.parentUuid = null;
            this.parentIdClient = -1;
        }

        if (!thisObject.getEntityWorld().isClient()) {
            PlayerLookup.tracking(thisObject).forEach(player -> net.lordkipama.modernminecarts.SyncChainedMinecartPacket.send(this.getLinkedParent(), (AbstractMinecartEntity) (Object) this, player));
        }
    }

    @Override
    public void setLinkedParentClient(int id) {
        this.parentIdClient = id;
    }


    @Override
    public AbstractMinecartEntity getLinkedChild() {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity) (Object) this;
        var entity = thisObject.getEntityWorld() instanceof ServerWorld serverWorld && this.childUuid != null ? ((ServerWorld) thisObject.getEntityWorld()).getEntity(this.childUuid) : thisObject.getEntityWorld().getEntityById(this.childIdClient);
        return entity instanceof AbstractMinecartEntity abstractMinecartEntity ? abstractMinecartEntity : null;
    }
    @Override
    public void setLinkedChild(@Nullable AbstractMinecartEntity child) {
        if (child != null) {
            this.childUuid = child.getUuid();
            this.childIdClient = child.getId();
        } else {
            this.childUuid = null;
            this.childIdClient = -1;
        }
    }

    @Override
    public void setLinkedChildClient(int id) {
        this.childIdClient = id;
    }


    /**
     * @author
     * @reason
    */
    @Overwrite
    public Vec3d applySlowdown(Vec3d velocity) {
        AbstractMinecartEntity thisObject = (AbstractMinecartEntity) (Object) this;
        double d = 0.96; //REMOVED Slowdown for minecart with passangers thisObject.hasPassengers() ? 0.997 : 0.96;
        Vec3d vec3d = velocity.multiply(d, 0.0, d);
        if (thisObject.isTouchingWater()) {
            vec3d = vec3d.multiply(0.949999988079071);
        }

        return vec3d;
    }

    @Unique
    private static double modernminecarts$getRailJumpApproachSpeed(
            AbstractMinecartEntity cart,
            BlockPos rampPos,
            BlockState rampState
    ) {
        RailShape shape = rampState.get(SlopedRailBlock.SHAPE);
        BlockPos frontPos = switch (shape) {
            case ASCENDING_NORTH -> rampPos.north();
            case ASCENDING_SOUTH -> rampPos.south();
            case ASCENDING_EAST -> rampPos.east();
            case ASCENDING_WEST -> rampPos.west();
            default -> rampPos;
        };

        return cart.getEntityWorld().getBlockState(frontPos).isAir()
                ? MinecartTuning.copperRailSpeed()
                : MinecartTuning.ascendingCopperRailSpeed();
    }
}
