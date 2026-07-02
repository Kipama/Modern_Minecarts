package net.lordkipama.modernminecarts.mixin;

import java.util.List;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.interfaces.ContainerMinecartInteface;
import net.lordkipama.modernminecarts.logic.MinecartTuning;
import net.lordkipama.modernminecarts.logic.TrainEngineLogic;
import net.lordkipama.modernminecarts.screen.FurnaceMinecartScreenHandler;
import net.lordkipama.modernminecarts.util.TrainInventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartFurnace.class)
public abstract class FurnaceMinecartMixin implements Container, MenuProvider, ContainerMinecartInteface {
    @Shadow private int fuel;
    @Shadow public Vec3 push;
    @Shadow protected abstract void setHasFuel(boolean fuel);

    @Unique private NonNullList<ItemStack> modernminecarts$inventory = NonNullList.withSize(1, ItemStack.EMPTY);
    @Unique private int modernminecarts$fuelBurnTime;
    @Unique private int modernminecarts$numberOfChildren;
    @Unique private int modernminecarts$burningFurnaces = 1;
    @Unique private int modernminecarts$speedForDisplay;
    @Unique
    private final ContainerData modernminecarts$properties = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FurnaceMinecartMixin.this.fuel;
                case 1 -> FurnaceMinecartMixin.this.modernminecarts$fuelBurnTime;
                case 2 -> FurnaceMinecartMixin.this.calculateActualSpeedForDisplay();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                FurnaceMinecartMixin.this.fuel = value;
            } else if (index == 1) {
                FurnaceMinecartMixin.this.modernminecarts$fuelBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    @Inject(method = "tick", at = @At("HEAD"))
    private void modernminecarts$prepareFuelAndDirection(CallbackInfo ci) {
        MinecartFurnace cart = modernminecarts$self();
        if (cart.level().isClientSide()) {
            return;
        }

        modernminecarts$numberOfChildren = modernminecarts$countChildren(cart);
        boolean hasChild = modernminecarts$getChild(cart) != null;
        boolean isMoving = cart.getDeltaMovement().horizontalDistanceSqr() > 0.001D;
        boolean isRoot = modernminecarts$getParent(cart) == null;
        boolean hasDirection = push.horizontalDistanceSqr() > 1.0E-7D;
        boolean mayStart = modernminecarts$railAllowsMovement(cart) && !modernminecarts$isActivelyPoweredRail(cart) && isRoot && (hasChild || isMoving);

        if (fuel <= 0 && mayStart) {
            modernminecarts$burningFurnaces = burnFuelTrain();
        }

        if (fuel > 0 && !hasDirection) {
            AbstractMinecart child = modernminecarts$getChild(cart);
            if (child != null) {
                push = new Vec3(cart.getX() - child.getX(), 0.0D, cart.getZ() - child.getZ());
            } else if (isMoving) {
                push = new Vec3(cart.getDeltaMovement().x, 0.0D, cart.getDeltaMovement().z);
            }
        }

        boolean waitingForDirection = fuel > 0 && push.horizontalDistanceSqr() <= 1.0E-7D && isRoot;
        if (waitingForDirection) {
            fuel++;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void modernminecarts$finishFuelTick(CallbackInfo ci) {
        MinecartFurnace cart = modernminecarts$self();
        if (cart.level().isClientSide()) {
            return;
        }

        if (fuel > 0 && ModernMinecartsConfig.enableFurnaceMinecartChunkloading() && cart.level() instanceof ServerLevel serverLevel) {
            ChunkPos chunkPos = new ChunkPos(cart.blockPosition().getX() >> 4, cart.blockPosition().getZ() >> 4);
            serverLevel.getChunkSource().addTicketWithRadius(TicketType.PORTAL, chunkPos, 3);
        }

        if (!modernminecarts$railAllowsMovement(cart)) {
            push = Vec3.ZERO;
            cart.setDeltaMovement(Vec3.ZERO);
        } else {
            Vec3 movement = cart.getDeltaMovement();
            double maxSpeed = MinecartTuning.FURNACE_MINECART_MAX_SPEED;
            cart.setDeltaMovement(Mth.clamp(movement.x, -maxSpeed, maxSpeed), movement.y, Mth.clamp(movement.z, -maxSpeed, maxSpeed));
        }

        setHasFuel(fuel > 0);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void modernminecarts$openFuelScreen(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (!player.level().isClientSide()) {
            player.openMenu(this);
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void modernminecarts$writeInventory(ValueOutput output, CallbackInfo ci) {
        output.store("ModernMinecartsInventory", ItemStack.CODEC.listOf(), List.copyOf(modernminecarts$inventory));
        output.putInt("ModernMinecartsFuelBurnTime", modernminecarts$fuelBurnTime);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void modernminecarts$readInventory(ValueInput input, CallbackInfo ci) {
        modernminecarts$inventory = NonNullList.withSize(1, ItemStack.EMPTY);
        input.read("ModernMinecartsInventory", ItemStack.CODEC.listOf()).ifPresent(items -> {
            if (!items.isEmpty()) {
                modernminecarts$inventory.set(0, items.getFirst());
            }
        });
        modernminecarts$fuelBurnTime = input.getIntOr("ModernMinecartsFuelBurnTime", 0);
    }

    @Inject(method = "applyNaturalSlowdown", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$capEngineSpeed(Vec3 movement, CallbackInfoReturnable<Vec3> cir) {
        MinecartFurnace cart = modernminecarts$self();
        if (!(cart.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double maxSpeed = ((MinecartInvoker) cart).invokeGetMaxSpeed(serverLevel);
        Vec3 slowed = cir.getReturnValue();
        cir.setReturnValue(new Vec3(Mth.clamp(slowed.x, -maxSpeed, maxSpeed), slowed.y, Mth.clamp(slowed.z, -maxSpeed, maxSpeed)));
    }

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$useForgeFurnaceSpeed(ServerLevel world, CallbackInfoReturnable<Double> cir) {
        double railSpeed = modernminecarts$getRailSpeed();
        if (modernminecarts$getParent(modernminecarts$self()) != null) {
            cir.setReturnValue(railSpeed);
        } else {
            cir.setReturnValue(limitTrainSpeed(railSpeed));
        }
    }

    @Override
    public int burnFuelTrain() {
        if (modernminecarts$isActivelyPoweredRail(modernminecarts$self())) {
            AbstractMinecart child = modernminecarts$getChild(modernminecarts$self());
            if (child instanceof MinecartFurnace && child instanceof ContainerMinecartInteface furnaceChild) {
                return furnaceChild.burnFuelTrain();
            }
            return 0;
        }

        int burnTime = modernminecarts$consumeFuelItem();
        if (burnTime <= 0) {
            return 0;
        }

        fuel = burnTime;
        modernminecarts$fuelBurnTime = burnTime;

        AbstractMinecart child = modernminecarts$getChild(modernminecarts$self());
        if (child instanceof MinecartFurnace && child instanceof ContainerMinecartInteface furnaceChild) {
            return 1 + furnaceChild.burnFuelTrain();
        }
        return 1;
    }

    @Unique
    private int modernminecarts$consumeFuelItem() {
        ItemStack stack = getItem(0);
        int burnTime = modernminecarts$self().level().fuelValues().burnDuration(stack);
        if (stack.isEmpty() || burnTime <= 0) {
            return 0;
        }

        Item consumedFuel = stack.getItem();
        var remainderTemplate = stack.getCraftingRemainder();
        ItemStack remainder = remainderTemplate != null ? remainderTemplate.create() : ItemStack.EMPTY;
        stack.shrink(1);
        if (stack.isEmpty()) {
            setItem(0, remainder);
        }
        modernminecarts$refillFuelSlot(consumedFuel);
        return burnTime;
    }

    @Unique
    private void modernminecarts$refillFuelSlot(Item consumedFuel) {
        ItemStack fuelStack = getItem(0);
        for (Container inventory : getChainInventories()) {
            for (int slot = 0; slot < inventory.getContainerSize() && fuelStack.getCount() < fuelStack.getMaxStackSize(); slot++) {
                ItemStack candidate = inventory.getItem(slot);
                if (!candidate.is(consumedFuel)) {
                    continue;
                }

                if (fuelStack.isEmpty()) {
                    fuelStack = candidate.copy();
                    fuelStack.setCount(0);
                    setItem(0, fuelStack);
                } else if (fuelStack.is(Items.BUCKET) && consumedFuel == Items.LAVA_BUCKET) {
                    setItem(0, new ItemStack(Items.LAVA_BUCKET));
                    candidate.shrink(1);
                    if (candidate.isEmpty()) {
                        inventory.setItem(slot, new ItemStack(Items.BUCKET));
                    } else {
                        modernminecarts$storeRemainder(inventory, new ItemStack(Items.BUCKET));
                    }
                    inventory.setChanged();
                    return;
                } else if (!ItemStack.isSameItemSameComponents(fuelStack, candidate)) {
                    continue;
                }

                int moved = Math.min(fuelStack.getMaxStackSize() - fuelStack.getCount(), candidate.getCount());
                fuelStack.grow(moved);
                candidate.shrink(moved);
                inventory.setChanged();
            }
        }
        setChanged();
    }

    @Unique
    private static void modernminecarts$storeRemainder(Container inventory, ItemStack remainder) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                inventory.setItem(slot, remainder);
                return;
            }
            if (ItemStack.isSameItemSameComponents(stack, remainder) && stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(1);
                return;
            }
        }
    }

    @Override
    public List<Container> getChainInventories() {
        return TrainInventoryUtil.collectStorageInventories(modernminecarts$self(), false, true, true);
    }

    @Override
    public void setNumberOfChildren(int number) {
        modernminecarts$numberOfChildren = Math.max(0, number);
    }

    @Override
    public int calculateActualSpeedForDisplay() {
        AbstractMinecart parent = modernminecarts$getParent(modernminecarts$self());
        if (parent instanceof ContainerMinecartInteface parentFurnace) {
            return parentFurnace.calculateActualSpeedForDisplay();
        }
        return TrainEngineLogic.calculateSpeedometerValue(modernminecarts$self().getDeltaMovement().horizontalDistance(), modernminecarts$speedForDisplay / (double) MinecartTuning.SPEEDOMETER_SCALE);
    }

    @Override
    public double limitTrainSpeed(double railSpeed) {
        AbstractMinecart parent = modernminecarts$getParent(modernminecarts$self());
        if (parent instanceof ContainerMinecartInteface parentFurnace) {
            return parentFurnace.limitTrainSpeed(railSpeed);
        }

        double furnaceSpeed = Math.min(railSpeed, MinecartTuning.FURNACE_MINECART_MAX_SPEED);
        double result = TrainEngineLogic.calculateSpeedLimit(furnaceSpeed, modernminecarts$numberOfChildren, modernminecarts$burningFurnaces);
        modernminecarts$speedForDisplay = TrainEngineLogic.speedToDisplayUnits(result);
        return result;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new FurnaceMinecartScreenHandler(syncId, playerInventory, this, modernminecarts$properties);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.modernminecarts.furnace_minecart");
    }

    @Override
    public int getContainerSize() {
        return modernminecarts$inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return modernminecarts$inventory.getFirst().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return modernminecarts$inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = modernminecarts$inventory.get(slot).split(amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = modernminecarts$inventory.get(slot);
        modernminecarts$inventory.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        modernminecarts$inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        MinecartFurnace cart = modernminecarts$self();
        return !cart.isRemoved() && player.distanceToSqr(cart) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return modernminecarts$self().level().fuelValues().isFuel(stack);
    }

    @Override
    public void clearContent() {
        modernminecarts$inventory.clear();
    }

    @Unique
    private static int modernminecarts$countChildren(AbstractMinecart cart) {
        int count = 0;
        AbstractMinecart current = modernminecarts$getChild(cart);
        while (current != null && count < 128) {
            count++;
            current = modernminecarts$getChild(current);
        }
        return count;
    }

    @Unique
    private static boolean modernminecarts$railAllowsMovement(MinecartFurnace cart) {
        BlockState state = modernminecarts$getCurrentRailState(cart);
        if (state == null) {
            return false;
        }
        if (state.is(Blocks.POWERED_RAIL) && state.hasProperty(PoweredRailBlock.POWERED)) {
            return state.getValue(PoweredRailBlock.POWERED);
        }
        if (state.getBlock() instanceof PoweredDetectorRailBlock) {
            return state.getValue(PoweredDetectorRailBlock.POWERED);
        }
        return true;
    }

    @Unique
    private static boolean modernminecarts$isActivelyPoweredRail(MinecartFurnace cart) {
        BlockState state = modernminecarts$getCurrentRailState(cart);
        if (state == null) {
            return false;
        }
        if (state.is(Blocks.POWERED_RAIL) && state.hasProperty(PoweredRailBlock.POWERED)) {
            return state.getValue(PoweredRailBlock.POWERED);
        }
        return state.getBlock() instanceof PoweredDetectorRailBlock && state.getValue(PoweredDetectorRailBlock.POWERED);
    }

    @Unique
    private static @Nullable BlockState modernminecarts$getCurrentRailState(MinecartFurnace cart) {
        if (!cart.isOnRails()) {
            return null;
        }
        BlockState state = cart.level().getBlockState(cart.blockPosition());
        if (!(state.getBlock() instanceof BaseRailBlock)) {
            state = cart.level().getBlockState(cart.blockPosition().below());
        }
        return state.getBlock() instanceof BaseRailBlock ? state : null;
    }

    @Unique
    private double modernminecarts$getRailSpeed() {
        MinecartFurnace cart = modernminecarts$self();
        BlockPos railPos = cart.blockPosition();
        BlockState railState = cart.level().getBlockState(railPos);
        if (!(railState.getBlock() instanceof BaseRailBlock)) {
            railPos = railPos.below();
            railState = cart.level().getBlockState(railPos);
        }
        if (!(railState.getBlock() instanceof BaseRailBlock)) {
            return MinecartTuning.VANILLA_RAIL_SPEED;
        }

        double railSpeed = modernminecarts$getParent(cart) != null ? MinecartTuning.copperRailSpeed() : modernminecarts$getSpeedForRail(cart, railPos, railState);
        if (cart.isInWater() && !railState.is(Blocks.POWERED_RAIL)) {
            railSpeed /= 2.0D;
        }

        if (railSpeed > 0.5D) {
            BlockPos nextPos = modernminecarts$getNextRailPos(cart, railPos);
            BlockState nextState = cart.level().getBlockState(nextPos);
            if (!(nextState.getBlock() instanceof BaseRailBlock)) {
                nextPos = nextPos.below();
                nextState = cart.level().getBlockState(nextPos);
            }
            if (nextState.getBlock() instanceof BaseRailBlock nextRail && nextState.getValue(nextRail.getShapeProperty()).isSlope()) {
                railSpeed = modernminecarts$getSpeedForRail(cart, nextPos, nextState);
            }
        }

        return railSpeed;
    }

    @Unique
    private static double modernminecarts$getSpeedForRail(MinecartFurnace cart, BlockPos pos, BlockState state) {
        if (state.is(ModBlocks.RAIL_CROSSING)) {
            return MinecartTuning.copperRailSpeed();
        }
        if (state.is(ModBlocks.RAIL_JUMP)) {
            RailShape shape = state.getValue(SlopedRailBlock.SHAPE);
            BlockPos launchPos = switch (shape) {
                case ASCENDING_NORTH -> pos.north().above();
                case ASCENDING_SOUTH -> pos.south().above();
                case ASCENDING_EAST -> pos.east().above();
                case ASCENDING_WEST -> pos.west().above();
                default -> pos;
            };
            return cart.level().getBlockState(launchPos).isAir() ? MinecartTuning.copperRailSpeed() : MinecartTuning.ascendingCopperRailSpeed();
        }
        if (state.getBlock() instanceof CopperRailBlock || state.getBlock() instanceof WaxedCopperRailBlock) {
            RailShape shape = state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty());
            if (shape.isSlope() && (state.is(ModBlocks.COPPER_RAIL) || state.is(ModBlocks.WAXED_COPPER_RAIL) || state.is(ModBlocks.EXPOSED_COPPER_RAIL) || state.is(ModBlocks.WAXED_EXPOSED_COPPER_RAIL))) {
                return MinecartTuning.ascendingCopperRailSpeed();
            }
            if (state.is(ModBlocks.COPPER_RAIL) || state.is(ModBlocks.WAXED_COPPER_RAIL)) {
                return MinecartTuning.copperRailSpeed();
            }
            if (state.is(ModBlocks.EXPOSED_COPPER_RAIL) || state.is(ModBlocks.WAXED_EXPOSED_COPPER_RAIL)) {
                return MinecartTuning.exposedCopperRailSpeed();
            }
            if (state.is(ModBlocks.WEATHERED_COPPER_RAIL) || state.is(ModBlocks.WAXED_WEATHERED_COPPER_RAIL)) {
                return MinecartTuning.weatheredCopperRailSpeed();
            }
            return MinecartTuning.oxidizedCopperRailSpeed();
        }
        return MinecartTuning.VANILLA_RAIL_SPEED;
    }

    @Unique
    private static BlockPos modernminecarts$getNextRailPos(MinecartFurnace cart, BlockPos railPos) {
        Vec3 movement = cart.getDeltaMovement();
        if (Math.abs(movement.x) > Math.abs(movement.z)) {
            return movement.x >= 0.0D ? railPos.east() : railPos.west();
        }
        return movement.z >= 0.0D ? railPos.south() : railPos.north();
    }

    @Unique
    private static @Nullable AbstractMinecart modernminecarts$getParent(AbstractMinecart cart) {
        return ((ChainMinecartInterface) cart).getLinkedParent();
    }

    @Unique
    private static @Nullable AbstractMinecart modernminecarts$getChild(AbstractMinecart cart) {
        return ((ChainMinecartInterface) cart).getLinkedChild();
    }

    @Unique
    private MinecartFurnace modernminecarts$self() {
        return (MinecartFurnace) (Object) this;
    }
}
