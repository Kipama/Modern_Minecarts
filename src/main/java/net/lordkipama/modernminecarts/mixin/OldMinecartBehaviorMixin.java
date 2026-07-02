package net.lordkipama.modernminecarts.mixin;

import com.mojang.datafixers.util.Pair;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.interfaces.TrackedMinecartSpeed;
import net.lordkipama.modernminecarts.logic.PoweredDetectorMotion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(OldMinecartBehavior.class)
public abstract class OldMinecartBehaviorMixin extends MinecartBehavior {
    protected OldMinecartBehaviorMixin(AbstractMinecart minecart) {
        super(minecart);
    }

    /**
     * @author LordKipama
     * @reason Apply custom rail behavior to the actual 26.1 old minecart movement path.
     */
    @Overwrite
    public void moveAlongTrack(ServerLevel world) {
        AbstractMinecart cart = this.minecart;
        BlockPos pos = cart.getCurrentBlockPosOrRailBelow();
        BlockState state = world.getBlockState(pos);
        cart.resetFallDistance();

        double x = cart.getX();
        double y = cart.getY();
        double z = cart.getZ();
        Vec3 snappedStart = modernminecarts$snapPositionToRail(cart, x, y, z);
        y = pos.getY();

        boolean poweredRail = false;
        boolean shouldBrake = false;
        if (state.getBlock() instanceof PoweredRailBlock && !state.is(Blocks.ACTIVATOR_RAIL)) {
            poweredRail = state.getValue(PoweredRailBlock.POWERED);
            shouldBrake = !poweredRail;
        } else if (state.getBlock() instanceof PoweredDetectorRailBlock) {
            poweredRail = state.getValue(PoweredDetectorRailBlock.POWERED);
            shouldBrake = !poweredRail;
            modernminecarts$applyPoweredDetectorMotion(cart, state, poweredRail);
        }

        double slopeAdjustment = 0.0078125D;
        if (cart.isInWater()) {
            slopeAdjustment *= 0.2D;
        }

        Vec3 movement = cart.getDeltaMovement();
        RailShape railShape = modernminecarts$getEffectiveShape(cart.level(), pos, state, movement);
        switch (railShape) {
            case ASCENDING_EAST -> {
                cart.setDeltaMovement(movement.add(-slopeAdjustment, 0.0D, 0.0D));
                y += 1.0D;
            }
            case ASCENDING_WEST -> {
                cart.setDeltaMovement(movement.add(slopeAdjustment, 0.0D, 0.0D));
                y += 1.0D;
            }
            case ASCENDING_NORTH -> {
                cart.setDeltaMovement(movement.add(0.0D, 0.0D, slopeAdjustment));
                y += 1.0D;
            }
            case ASCENDING_SOUTH -> {
                cart.setDeltaMovement(movement.add(0.0D, 0.0D, -slopeAdjustment));
                y += 1.0D;
            }
            default -> {
            }
        }

        movement = cart.getDeltaMovement();
        Pair<Vec3i, Vec3i> exits = MinecartInvoker.invokeGetAdjacentRailPositionsByShape(railShape);
        Vec3i start = exits.getFirst();
        Vec3i end = exits.getSecond();
        double dx = end.getX() - start.getX();
        double dz = end.getZ() - start.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        double alignedDot = movement.x * dx + movement.z * dz;
        if (alignedDot < 0.0D) {
            dx = -dx;
            dz = -dz;
        }

        double horizontalSpeed = Math.min(2.0D, movement.horizontalDistance());
        movement = new Vec3(horizontalSpeed * dx / length, movement.y, horizontalSpeed * dz / length);
        cart.setDeltaMovement(movement);

        Entity passenger = cart.getFirstPassenger();
        if (passenger instanceof Player) {
            Vec3 passengerMovement = passenger.getDeltaMovement();
            double passengerHorizontal = passengerMovement.horizontalDistanceSqr();
            double cartHorizontal = cart.getDeltaMovement().horizontalDistanceSqr();
            if (passengerHorizontal > 1.0E-4D && cartHorizontal < 0.01D) {
                cart.setDeltaMovement(cart.getDeltaMovement().add(passengerMovement.x * 0.1D, 0.0D, passengerMovement.z * 0.1D));
                shouldBrake = false;
            }
        }

        if (shouldBrake) {
            double brakeSpeed = cart.getDeltaMovement().horizontalDistance();
            if (brakeSpeed < 0.03D) {
                cart.setDeltaMovement(Vec3.ZERO);
            } else {
                cart.setDeltaMovement(cart.getDeltaMovement().multiply(0.5D, 0.0D, 0.5D));
            }
        }

        double startX = pos.getX() + 0.5D + start.getX() * 0.5D;
        double startZ = pos.getZ() + 0.5D + start.getZ() * 0.5D;
        double endX = pos.getX() + 0.5D + end.getX() * 0.5D;
        double endZ = pos.getZ() + 0.5D + end.getZ() * 0.5D;
        dx = endX - startX;
        dz = endZ - startZ;

        double progress;
        if (dx == 0.0D) {
            progress = z - pos.getZ();
        } else if (dz == 0.0D) {
            progress = x - pos.getX();
        } else {
            double relX = x - startX;
            double relZ = z - startZ;
            progress = (relX * dx + relZ * dz) * 2.0D;
        }

        x = startX + dx * progress;
        z = startZ + dz * progress;
        cart.setPos(x, y, z);

        double topSpeed = ((MinecartInvoker) cart).invokeGetMaxSpeed(world);
        movement = cart.getDeltaMovement();
        movement = new Vec3(Mth.clamp(movement.x, -topSpeed, topSpeed), 0.0D, Mth.clamp(movement.z, -topSpeed, topSpeed));
        cart.setDeltaMovement(movement);
        ((TrackedMinecartSpeed) cart).modernminecarts$setTrackedMaxSpeed(topSpeed);
        cart.move(MoverType.SELF, new Vec3(Mth.clamp(movement.x, -topSpeed, topSpeed), 0.0D, Mth.clamp(movement.z, -topSpeed, topSpeed)));

        if (start.getY() != 0 && Mth.floor(cart.getX()) - pos.getX() == start.getX() && Mth.floor(cart.getZ()) - pos.getZ() == start.getZ()) {
            cart.setPos(cart.getX(), cart.getY() + start.getY(), cart.getZ());
        } else if (end.getY() != 0 && Mth.floor(cart.getX()) - pos.getX() == end.getX() && Mth.floor(cart.getZ()) - pos.getZ() == end.getZ()) {
            cart.setPos(cart.getX(), cart.getY() + end.getY(), cart.getZ());
        }

        cart.setDeltaMovement(((MinecartInvoker) cart).invokeApplyNaturalSlowdown(cart.getDeltaMovement()));
        Vec3 snappedEnd = modernminecarts$snapPositionToRail(cart, cart.getX(), cart.getY(), cart.getZ());
        if (snappedEnd != null && snappedStart != null) {
            double slopeDelta = (snappedStart.y - snappedEnd.y) * 0.05D;
            Vec3 slowed = cart.getDeltaMovement();
            double slowedHorizontal = slowed.horizontalDistance();
            if (slowedHorizontal > 0.0D) {
                cart.setDeltaMovement(slowed.multiply((slowedHorizontal + slopeDelta) / slowedHorizontal, 1.0D, (slowedHorizontal + slopeDelta) / slowedHorizontal));
            }
            cart.setPos(cart.getX(), snappedEnd.y, cart.getZ());
        }

        int floorX = Mth.floor(cart.getX());
        int floorZ = Mth.floor(cart.getZ());
        if (floorX != pos.getX() || floorZ != pos.getZ()) {
            Vec3 adjusted = cart.getDeltaMovement();
            double adjustedHorizontal = adjusted.horizontalDistance();
            cart.setDeltaMovement(adjustedHorizontal * (floorX - pos.getX()), adjusted.y, adjustedHorizontal * (floorZ - pos.getZ()));
        }

        if (poweredRail) {
            Vec3 poweredMovement = cart.getDeltaMovement();
            double poweredHorizontal = poweredMovement.horizontalDistance();
            if (poweredHorizontal > 0.01D) {
                cart.setDeltaMovement(poweredMovement.add(poweredMovement.x / poweredHorizontal * 0.06D, 0.0D, poweredMovement.z / poweredHorizontal * 0.06D));
            } else {
                double poweredX = poweredMovement.x;
                double poweredZ = poweredMovement.z;
                if (railShape == RailShape.EAST_WEST) {
                    if (cart.isRedstoneConductor(pos.west())) {
                        poweredX = 0.02D;
                    } else if (cart.isRedstoneConductor(pos.east())) {
                        poweredX = -0.02D;
                    }
                } else if (railShape == RailShape.NORTH_SOUTH) {
                    if (cart.isRedstoneConductor(pos.north())) {
                        poweredZ = 0.02D;
                    } else if (cart.isRedstoneConductor(pos.south())) {
                        poweredZ = -0.02D;
                    }
                } else {
                    return;
                }
                cart.setDeltaMovement(poweredX, poweredMovement.y, poweredZ);
            }
        }

        Vec3 finalMovement = cart.getDeltaMovement();
        cart.setDeltaMovement(
                Mth.clamp(finalMovement.x, -topSpeed, topSpeed),
                finalMovement.y,
                Mth.clamp(finalMovement.z, -topSpeed, topSpeed)
        );
    }

    @Unique
    private void modernminecarts$applyPoweredDetectorMotion(AbstractMinecart cart, BlockState state, boolean powered) {
        if (((ChainMinecartInterface) cart).getLinkedParent() != null) {
            return;
        }

        Vec3 velocity = cart.getDeltaMovement();
        RailShape shape = state.getValue(PoweredDetectorRailBlock.SHAPE);
        boolean northSouth = shape == RailShape.NORTH_SOUTH || shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH;
        PoweredDetectorMotion.Motion adjusted = PoweredDetectorMotion.adjust(
                velocity.x,
                velocity.z,
                northSouth,
                state.getValue(PoweredDetectorRailBlock.INVERTED),
                powered
        );
        cart.setDeltaMovement(adjusted.x(), velocity.y, adjusted.z());
    }

    @Unique
    private static RailShape modernminecarts$getEffectiveShape(Level level, BlockPos pos, BlockState state, Vec3 movement) {
        if (state.is(ModBlocks.RAIL_CROSSING)) {
            return Math.abs(movement.z) > Math.abs(movement.x) ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST;
        }
        if (state.is(ModBlocks.RAIL_JUMP)) {
            return state.getValue(SlopedRailBlock.CONST_SHAPE);
        }
        return state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty());
    }

    @Unique
    private static @Nullable Vec3 modernminecarts$snapPositionToRail(AbstractMinecart cart, double x, double y, double z) {
        int blockX = Mth.floor(x);
        int blockY = Mth.floor(y);
        int blockZ = Mth.floor(z);
        if (cart.level().getBlockState(new BlockPos(blockX, blockY - 1, blockZ)).is(BlockTags.RAILS)) {
            blockY--;
        }

        BlockPos pos = new BlockPos(blockX, blockY, blockZ);
        BlockState state = cart.level().getBlockState(pos);
        if (!BaseRailBlock.isRail(state)) {
            return null;
        }

        BaseRailBlock baseRailBlock = (BaseRailBlock) state.getBlock();
        RailShape shape = modernminecarts$getEffectiveShape(cart.level(), pos, state, cart.getDeltaMovement());
        if (state.is(ModBlocks.RAIL_CROSSING)) {
            Direction direction = Math.abs(cart.getDeltaMovement().x) > Math.abs(cart.getDeltaMovement().z)
                    ? (cart.getDeltaMovement().x >= 0.0 ? Direction.EAST : Direction.WEST)
                    : (cart.getDeltaMovement().z >= 0.0 ? Direction.SOUTH : Direction.NORTH);
            if (shape == RailShape.NORTH_SOUTH && (direction == Direction.EAST || direction == Direction.WEST)) {
                cart.level().setBlock(pos, state.setValue(baseRailBlock.getShapeProperty(), RailShape.EAST_WEST), 3);
                shape = RailShape.EAST_WEST;
            } else if (shape == RailShape.EAST_WEST && (direction == Direction.NORTH || direction == Direction.SOUTH)) {
                cart.level().setBlock(pos, state.setValue(baseRailBlock.getShapeProperty(), RailShape.NORTH_SOUTH), 3);
                shape = RailShape.NORTH_SOUTH;
            }
        }

        Pair<Vec3i, Vec3i> exits = MinecartInvoker.invokeGetAdjacentRailPositionsByShape(shape);
        Vec3i start = exits.getFirst();
        Vec3i end = exits.getSecond();
        double startX = blockX + 0.5 + start.getX() * 0.5;
        double startY = blockY + 0.0625 + start.getY() * 0.5;
        double startZ = blockZ + 0.5 + start.getZ() * 0.5;
        double endX = blockX + 0.5 + end.getX() * 0.5;
        double endY = blockY + 0.0625 + end.getY() * 0.5;
        double endZ = blockZ + 0.5 + end.getZ() * 0.5;

        double diffX = endX - startX;
        double diffY = (endY - startY) * 2.0;
        double diffZ = endZ - startZ;
        double progress;
        if (diffX == 0.0) {
            progress = z - blockZ;
        } else if (diffZ == 0.0) {
            progress = x - blockX;
        } else {
            double relX = x - startX;
            double relZ = z - startZ;
            progress = (relX * diffX + relZ * diffZ) * 2.0;
        }

        x = startX + diffX * progress;
        y = startY + diffY * progress;
        z = startZ + diffZ * progress;
        if (diffY < 0.0) {
            y += 1.0;
        } else if (diffY > 0.0) {
            y += 0.5;
        }

        return new Vec3(x, y, z);
    }
}
