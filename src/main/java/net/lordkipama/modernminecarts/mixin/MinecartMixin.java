package net.lordkipama.modernminecarts.mixin;

import com.mojang.datafixers.util.Pair;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.lordkipama.modernminecarts.SyncChainedMinecartPacket;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.interfaces.ContainerMinecartInteface;
import net.lordkipama.modernminecarts.interfaces.TrackedMinecartSpeed;
import net.lordkipama.modernminecarts.logic.MinecartTuning;
import net.lordkipama.modernminecarts.logic.PoweredDetectorMotion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public class MinecartMixin implements ChainMinecartInterface, TrackedMinecartSpeed {
    @Unique private @Nullable UUID parentUuid;
    @Unique private @Nullable UUID childUuid;
    @Unique private int parentIdClient;
    @Unique private int childIdClient;
    @Unique private boolean jumpedOffSlope = false;
    @Unique private double maxSpeed = 0.8;

    @Unique
    public void modernminecarts$setTrackedMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * @author LordKipama
     * @reason Port custom rail behavior to 26.1 minecart API.
     */
    @Overwrite
    public void moveAlongTrack(ServerLevel world) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        BlockPos pos = cart.getCurrentBlockPosOrRailBelow();
        BlockState state = world.getBlockState(pos);
        cart.resetFallDistance();

        double x = cart.getX();
        double y = cart.getY();
        double z = cart.getZ();
        Vec3 snappedStart = modernminecarts$snapPositionToRail(x, y, z);
        y = pos.getY();

        boolean poweredRail = false;
        boolean shouldBrake = false;
        if (state.is(Blocks.POWERED_RAIL)) {
            poweredRail = state.getValue(PoweredRailBlock.POWERED);
            shouldBrake = !poweredRail;
        } else if (modernminecarts$isCopperPoweredRail(state)) {
            poweredRail = true;
        } else if (state.getBlock() instanceof PoweredDetectorRailBlock detectorRail) {
            poweredRail = state.getValue(PoweredDetectorRailBlock.POWERED);
            shouldBrake = !poweredRail;
            modernminecarts$applyPoweredDetectorMotion(cart, state, poweredRail);
        }

        double slopeAdjustment = 0.0078125;
        if (cart.isInWater()) {
            slopeAdjustment *= 0.2;
        }

        Vec3 movement = cart.getDeltaMovement();
        RailShape railShape = modernminecarts$getEffectiveShape(cart.level(), pos, state, movement);
        switch (railShape) {
            case ASCENDING_EAST -> {
                cart.setDeltaMovement(movement.add(-slopeAdjustment, 0.0, 0.0));
                y += 1.0;
            }
            case ASCENDING_WEST -> {
                cart.setDeltaMovement(movement.add(slopeAdjustment, 0.0, 0.0));
                y += 1.0;
            }
            case ASCENDING_NORTH -> {
                cart.setDeltaMovement(movement.add(0.0, 0.0, slopeAdjustment));
                y += 1.0;
            }
            case ASCENDING_SOUTH -> {
                cart.setDeltaMovement(movement.add(0.0, 0.0, -slopeAdjustment));
                y += 1.0;
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
        if (alignedDot < 0.0) {
            dx = -dx;
            dz = -dz;
        }

        double horizontalSpeed = Math.min(2.0, movement.horizontalDistance());
        movement = new Vec3(horizontalSpeed * dx / length, movement.y, horizontalSpeed * dz / length);
        cart.setDeltaMovement(movement);

        Entity passenger = cart.getFirstPassenger();
        if (passenger instanceof Player) {
            Vec3 passengerMovement = passenger.getDeltaMovement();
            double passengerHorizontal = passengerMovement.horizontalDistanceSqr();
            double cartHorizontal = cart.getDeltaMovement().horizontalDistanceSqr();
            if (passengerHorizontal > 1.0E-4 && cartHorizontal < 0.01) {
                cart.setDeltaMovement(cart.getDeltaMovement().add(passengerMovement.x * 0.1, 0.0, passengerMovement.z * 0.1));
                shouldBrake = false;
            }
        }

        if (shouldBrake) {
            double brakeSpeed = cart.getDeltaMovement().horizontalDistance();
            if (brakeSpeed < 0.03) {
                cart.setDeltaMovement(Vec3.ZERO);
            } else {
                cart.setDeltaMovement(cart.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }

        double startX = pos.getX() + 0.5 + start.getX() * 0.5;
        double startZ = pos.getZ() + 0.5 + start.getZ() * 0.5;
        double endX = pos.getX() + 0.5 + end.getX() * 0.5;
        double endZ = pos.getZ() + 0.5 + end.getZ() * 0.5;
        dx = endX - startX;
        dz = endZ - startZ;

        double progress;
        if (dx == 0.0) {
            progress = z - pos.getZ();
        } else if (dz == 0.0) {
            progress = x - pos.getX();
        } else {
            double relX = x - startX;
            double relZ = z - startZ;
            progress = (relX * dx + relZ * dz) * 2.0;
        }

        x = startX + dx * progress;
        z = startZ + dz * progress;
        cart.setPos(x, y, z);

        double topSpeed = ((MinecartInvoker) cart).invokeGetMaxSpeed(world);
        maxSpeed = topSpeed;
        movement = cart.getDeltaMovement();
        movement = new Vec3(Mth.clamp(movement.x, -topSpeed, topSpeed), 0.0, Mth.clamp(movement.z, -topSpeed, topSpeed));
        cart.setDeltaMovement(movement);
        cart.move(MoverType.SELF, new Vec3(Mth.clamp(movement.x, -topSpeed, topSpeed), 0.0, Mth.clamp(movement.z, -topSpeed, topSpeed)));

        if (start.getY() != 0 && Mth.floor(cart.getX()) - pos.getX() == start.getX() && Mth.floor(cart.getZ()) - pos.getZ() == start.getZ()) {
            cart.setPos(cart.getX(), cart.getY() + start.getY(), cart.getZ());
        } else if (end.getY() != 0 && Mth.floor(cart.getX()) - pos.getX() == end.getX() && Mth.floor(cart.getZ()) - pos.getZ() == end.getZ()) {
            cart.setPos(cart.getX(), cart.getY() + end.getY(), cart.getZ());
        }

        cart.setDeltaMovement(((MinecartInvoker) cart).invokeApplyNaturalSlowdown(cart.getDeltaMovement()));
        Vec3 snappedEnd = modernminecarts$snapPositionToRail(cart.getX(), cart.getY(), cart.getZ());
        if (snappedEnd != null && snappedStart != null) {
            double slopeDelta = (snappedStart.y - snappedEnd.y) * 0.05;
            Vec3 slowed = cart.getDeltaMovement();
            double slowedHorizontal = slowed.horizontalDistance();
            if (slowedHorizontal > 0.0) {
                cart.setDeltaMovement(slowed.multiply((slowedHorizontal + slopeDelta) / slowedHorizontal, 1.0, (slowedHorizontal + slopeDelta) / slowedHorizontal));
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
            if (poweredHorizontal > 0.01) {
                cart.setDeltaMovement(poweredMovement.add(poweredMovement.x / poweredHorizontal * 0.06, 0.0, poweredMovement.z / poweredHorizontal * 0.06));
            } else {
                double poweredX = poweredMovement.x;
                double poweredZ = poweredMovement.z;
                if (railShape == RailShape.EAST_WEST) {
                    if (cart.isRedstoneConductor(pos.west())) {
                        poweredX = 0.02;
                    } else if (cart.isRedstoneConductor(pos.east())) {
                        poweredX = -0.02;
                    }
                } else if (railShape == RailShape.NORTH_SOUTH) {
                    if (cart.isRedstoneConductor(pos.north())) {
                        poweredZ = 0.02;
                    } else if (cart.isRedstoneConductor(pos.south())) {
                        poweredZ = -0.02;
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

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void injectedGetMaxSpeed(ServerLevel world, CallbackInfoReturnable<Double> cir) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        BlockPos pos = cart.blockPosition();
        BlockState block = cart.level().getBlockState(pos);
        BlockState blockUnder = cart.level().getBlockState(pos.below());

        if (cart.isOnRails()) {
            if (block.is(Blocks.POWERED_RAIL)) {
                cir.setReturnValue(MinecartTuning.poweredRailSpeed());
            }

            if (block.getBlock().getClass() == CopperRailBlock.class || block.getBlock().getClass() == WaxedCopperRailBlock.class) {
                cir.setReturnValue(modernminecarts$getCopperRailSpeed(block, false));
            } else if (blockUnder.getBlock().getClass() == CopperRailBlock.class || blockUnder.getBlock().getClass() == WaxedCopperRailBlock.class) {
                cir.setReturnValue(modernminecarts$getCopperRailSpeed(blockUnder, true));
            } else if (blockUnder.is(ModBlocks.RAIL_JUMP)) {
                boolean airInFront = switch (blockUnder.getValue(SlopedRailBlock.SHAPE)) {
                    case ASCENDING_NORTH -> cart.level().getBlockState(pos.north()).isAir();
                    case ASCENDING_EAST -> cart.level().getBlockState(pos.east()).isAir();
                    case ASCENDING_SOUTH -> cart.level().getBlockState(pos.south()).isAir();
                    case ASCENDING_WEST -> cart.level().getBlockState(pos.west()).isAir();
                    default -> false;
                };
                cir.setReturnValue(airInFront ? MinecartTuning.copperRailSpeed() : MinecartTuning.ascendingCopperRailSpeed());
            }

            if (cart.getDeltaMovement().length() > MinecartTuning.ascendingCopperRailSpeed()) {
                BlockPos frontPos = modernminecarts$getFrontRailPos(cart);
                BlockState frontState = cart.level().getBlockState(frontPos);
                if (frontState.is(BlockTags.RAILS) && frontState.getBlock() instanceof BaseRailBlock baseRailBlock) {
                    RailShape frontShape = frontState.getValue(baseRailBlock.getShapeProperty());
                    if (frontShape.isSlope()) {
                        if (frontState.is(ModBlocks.RAIL_JUMP)) {
                            cir.setReturnValue(modernminecarts$getRailJumpApproachSpeed(cart, frontPos, frontState));
                        } else if (block.is(ModBlocks.COPPER_RAIL) || block.is(ModBlocks.WAXED_COPPER_RAIL) || block.is(ModBlocks.EXPOSED_COPPER_RAIL) || block.is(ModBlocks.WAXED_EXPOSED_COPPER_RAIL)) {
                            cir.setReturnValue(MinecartTuning.ascendingCopperRailSpeed());
                        } else if (block.is(ModBlocks.WEATHERED_COPPER_RAIL) || block.is(ModBlocks.WAXED_WEATHERED_COPPER_RAIL)) {
                            cir.setReturnValue(MinecartTuning.weatheredCopperRailSpeed());
                        } else if (block.is(ModBlocks.OXIDIZED_COPPER_RAIL) || block.is(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL)) {
                            cir.setReturnValue(MinecartTuning.oxidizedCopperRailSpeed());
                        }
                    }
                }
            }
        }

        if ((Object) this instanceof ContainerMinecartInteface furnaceMinecart) {
            cir.setReturnValue(furnaceMinecart.limitTrainSpeed(cir.getReturnValue()));
        }
    }

    @Unique
    private void modernminecarts$applyPoweredDetectorMotion(AbstractMinecart cart, BlockState state, boolean powered) {
        if (getLinkedParent() != null) {
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
    private @Nullable Vec3 modernminecarts$snapPositionToRail(double x, double y, double z) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
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

    /**
     * @author LordKipama
     * @reason Keep custom rail jump launch behavior.
     */
    @Overwrite
    public void comeOffTrack(ServerLevel world) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        double cap = cart.onGround() ? ((MinecartInvoker) cart).invokeGetMaxSpeed(world) : MinecartTuning.copperRailSpeed();
        Vec3 movement = cart.getDeltaMovement();

        int x = Mth.floor(cart.getX());
        int y = Mth.floor(cart.getY());
        int z = Mth.floor(cart.getZ());

        BlockState hindBlockState;
        double lateralMomentum;
        Vec3 jumpMovement;
        if (movement.x > 0) {
            hindBlockState = cart.level().getBlockState(new BlockPos(x - 1, y - 1, z));
            lateralMomentum = Math.abs(movement.x);
            jumpMovement = new Vec3(Math.min(movement.x + 0.1D, maxSpeed), Math.min(lateralMomentum, maxSpeed), 0.0);
        } else if (movement.x < 0) {
            hindBlockState = cart.level().getBlockState(new BlockPos(x + 1, y - 1, z));
            lateralMomentum = Math.abs(movement.x);
            jumpMovement = new Vec3(Math.max(movement.x - 0.1D, -maxSpeed), Math.min(lateralMomentum, maxSpeed), 0.0);
        } else if (movement.z > 0) {
            hindBlockState = cart.level().getBlockState(new BlockPos(x, y - 1, z - 1));
            lateralMomentum = Math.abs(movement.z);
            jumpMovement = new Vec3(0.0, Math.min(lateralMomentum, maxSpeed), Math.min(movement.z + 0.1D, maxSpeed));
        } else {
            hindBlockState = cart.level().getBlockState(new BlockPos(x, y - 1, z + 1));
            lateralMomentum = Math.abs(movement.z);
            jumpMovement = new Vec3(0.0, Math.min(lateralMomentum, maxSpeed), Math.max(movement.z - 0.1D, -maxSpeed));
        }

        BlockPos below = new BlockPos(x, y - 1, z);
        if (!jumpedOffSlope && hindBlockState.is(ModBlocks.RAIL_JUMP) && cart.level().getBlockState(below).isAir()) {
            movement = jumpMovement;
            jumpedOffSlope = true;
        } else if (!hindBlockState.is(ModBlocks.RAIL_JUMP)) {
            jumpedOffSlope = false;
        }

        cart.setDeltaMovement(Mth.clamp(movement.x, -cap, cap), movement.y, Mth.clamp(movement.z, -cap, cap));
        if (cart.onGround() && movement.y <= 0.0) {
            cart.setDeltaMovement(cart.getDeltaMovement().scale(0.5D));
        }

        cart.move(MoverType.SELF, cart.getDeltaMovement());
        if (!cart.onGround()) {
            cart.setDeltaMovement(cart.getDeltaMovement().scale(0.975D));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void modernminecarts$tick(CallbackInfo info) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        if (cart.level().isClientSide()) {
            return;
        }

        if (getLinkedParent() != null) {
            double distance = getLinkedParent().distanceTo(cart) - 1.0;
            if (distance <= 4.0) {
                Vec3 direction = getLinkedParent().position().subtract(cart.position()).normalize();
                if (distance > 1.0) {
                    Vec3 parentVelocity = getLinkedParent().getDeltaMovement();
                    if (parentVelocity.length() == 0.0) {
                        cart.setDeltaMovement(direction.scale(0.05));
                    } else {
                        cart.setDeltaMovement(direction.scale(parentVelocity.length()).scale(distance));
                    }
                } else if (distance < 0.8) {
                    cart.setDeltaMovement(direction.scale(-0.05));
                } else {
                    cart.setDeltaMovement(Vec3.ZERO);
                }
            } else {
                ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) getLinkedParent(), this);
                if (cart.level() instanceof ServerLevel serverLevel) {
                    cart.spawnAtLocation(serverLevel, new ItemStack(Items.IRON_CHAIN));
                }
                return;
            }

            if (getLinkedParent().isRemoved()) {
                ChainMinecartInterface.unsetParentChild((ChainMinecartInterface) getLinkedParent(), this);
            }
        }

        if (getLinkedChild() != null && getLinkedChild().isRemoved()) {
            ChainMinecartInterface.unsetParentChild(this, (ChainMinecartInterface) getLinkedChild());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void injectReadCustomData(ValueInput input, CallbackInfo info) {
        parentUuid = input.read("ParentUuid", UUIDUtil.CODEC).orElse(null);
        childUuid = input.read("ChildUuid", UUIDUtil.CODEC).orElse(null);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void injectWriteCustomData(ValueOutput output, CallbackInfo info) {
        if (parentUuid != null) {
            output.store("ParentUuid", UUIDUtil.CODEC, parentUuid);
        }
        if (childUuid != null) {
            output.store("ChildUuid", UUIDUtil.CODEC, childUuid);
        }
    }

    @Override
    public @Nullable AbstractMinecart getLinkedParent() {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        Entity entity = cart.level() instanceof ServerLevel serverLevel && parentUuid != null ? serverLevel.getEntity(parentUuid) : cart.level().getEntity(parentIdClient);
        return entity instanceof AbstractMinecart minecart ? minecart : null;
    }

    @Override
    public void setLinkedParent(@Nullable AbstractMinecart parent) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        if (parent != null) {
            parentUuid = parent.getUUID();
            parentIdClient = parent.getId();
        } else {
            parentUuid = null;
            parentIdClient = -1;
        }

        if (!cart.level().isClientSide()) {
            PlayerLookup.tracking(cart).forEach(player -> SyncChainedMinecartPacket.send(getLinkedParent(), cart, player));
        }
    }

    @Override
    public void setLinkedParentClient(int id) {
        parentIdClient = id;
    }

    @Override
    public @Nullable AbstractMinecart getLinkedChild() {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        Entity entity = cart.level() instanceof ServerLevel serverLevel && childUuid != null ? serverLevel.getEntity(childUuid) : cart.level().getEntity(childIdClient);
        return entity instanceof AbstractMinecart minecart ? minecart : null;
    }

    @Override
    public void setLinkedChild(@Nullable AbstractMinecart child) {
        if (child != null) {
            childUuid = child.getUUID();
            childIdClient = child.getId();
        } else {
            childUuid = null;
            childIdClient = -1;
        }
    }

    @Override
    public void setLinkedChildClient(int id) {
        childIdClient = id;
    }

    /**
     * @author LordKipama
     * @reason Remove passenger slowdown.
     */
    @Overwrite
    public Vec3 applyNaturalSlowdown(Vec3 movement) {
        AbstractMinecart cart = (AbstractMinecart) (Object) this;
        Vec3 slowed = movement.multiply(0.96, 0.0, 0.96);
        if (cart.isInWater()) {
            slowed = slowed.scale(0.949999988079071);
        }
        return slowed;
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
    private static double modernminecarts$getCopperRailSpeed(BlockState state, boolean ascending) {
        if (state.is(ModBlocks.COPPER_RAIL) || state.is(ModBlocks.WAXED_COPPER_RAIL)) {
            return ascending ? MinecartTuning.ascendingCopperRailSpeed() : MinecartTuning.copperRailSpeed();
        }
        if (state.is(ModBlocks.EXPOSED_COPPER_RAIL) || state.is(ModBlocks.WAXED_EXPOSED_COPPER_RAIL)) {
            return ascending ? MinecartTuning.ascendingCopperRailSpeed() : MinecartTuning.exposedCopperRailSpeed();
        }
        if (state.is(ModBlocks.WEATHERED_COPPER_RAIL) || state.is(ModBlocks.WAXED_WEATHERED_COPPER_RAIL)) {
            return MinecartTuning.weatheredCopperRailSpeed();
        }
        return MinecartTuning.oxidizedCopperRailSpeed();
    }

    @Unique
    private static boolean modernminecarts$isCopperPoweredRail(BlockState state) {
        return state.is(ModBlocks.COPPER_RAIL)
                || state.is(ModBlocks.WAXED_COPPER_RAIL)
                || state.is(ModBlocks.EXPOSED_COPPER_RAIL)
                || state.is(ModBlocks.WAXED_EXPOSED_COPPER_RAIL)
                || state.is(ModBlocks.WEATHERED_COPPER_RAIL)
                || state.is(ModBlocks.WAXED_WEATHERED_COPPER_RAIL)
                || state.is(ModBlocks.OXIDIZED_COPPER_RAIL)
                || state.is(ModBlocks.WAXED_OXIDIZED_COPPER_RAIL);
    }

    @Unique
    private static BlockPos modernminecarts$getFrontRailPos(AbstractMinecart cart) {
        Vec3 movement = cart.getDeltaMovement();
        BlockPos pos = cart.blockPosition();
        if (movement.x > 0) {
            return pos.east();
        }
        if (movement.x < 0) {
            return pos.west();
        }
        if (movement.z > 0) {
            return pos.south();
        }
        return pos.north();
    }

    @Unique
    private static double modernminecarts$getRailJumpApproachSpeed(AbstractMinecart cart, BlockPos rampPos, BlockState rampState) {
        RailShape shape = rampState.getValue(SlopedRailBlock.SHAPE);
        BlockPos frontPos = switch (shape) {
            case ASCENDING_NORTH -> rampPos.north();
            case ASCENDING_SOUTH -> rampPos.south();
            case ASCENDING_EAST -> rampPos.east();
            case ASCENDING_WEST -> rampPos.west();
            default -> rampPos;
        };
        return cart.level().getBlockState(frontPos).isAir() ? MinecartTuning.copperRailSpeed() : MinecartTuning.ascendingCopperRailSpeed();
    }
}
