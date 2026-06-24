package net.lordkipama.modernminecarts.mixin;

import net.lordkipama.modernminecarts.interfaces.ChainMinecartInterface;
import net.lordkipama.modernminecarts.interfaces.ContainerMinecartInteface;
import net.lordkipama.modernminecarts.logic.MinecartTuning;
import net.lordkipama.modernminecarts.logic.TrainEngineLogic;
import net.lordkipama.modernminecarts.block.Custom.CopperRailBlock;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.block.Custom.SlopedRailBlock;
import net.lordkipama.modernminecarts.block.Custom.WaxedCopperRailBlock;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.screen.FurnaceMinecartScreenHandler;
import net.lordkipama.modernminecarts.util.TrainInventoryUtil;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FurnaceMinecartEntity.class)
public abstract class FurnaceMinecartMixin implements Inventory, NamedScreenHandlerFactory, ContainerMinecartInteface {
    @Shadow
    private int fuel;

    @Shadow
    public Vec3d pushVec;

    @Shadow
    protected abstract void setLit(boolean lit);

    @Unique
    private DefaultedList<ItemStack> modernminecarts$inventory =
            DefaultedList.ofSize(1, ItemStack.EMPTY);

    @Unique
    private int modernminecarts$fuelBurnTime;

    @Unique
    private int modernminecarts$numberOfChildren;

    @Unique
    private int modernminecarts$burningFurnaces = 1;

    @Unique
    private int modernminecarts$speedForDisplay;

    @Unique
    private final PropertyDelegate modernminecarts$properties = new PropertyDelegate() {
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
        public int size() {
            return 3;
        }
    };

    @Inject(method = "tick", at = @At("HEAD"))
    private void modernminecarts$prepareFuelAndDirection(CallbackInfo ci) {
        FurnaceMinecartEntity cart = modernminecarts$self();
        if (cart.getEntityWorld().isClient()) {
            return;
        }

        modernminecarts$numberOfChildren = modernminecarts$countChildren(cart);
        boolean hasChild = modernminecarts$getChild(cart) != null;
        boolean isMoving = cart.getVelocity().horizontalLengthSquared() > 0.001D;
        boolean isRoot = modernminecarts$getParent(cart) == null;
        boolean hasDirection = pushVec.horizontalLengthSquared() > 1.0E-7D;
        boolean mayStart = modernminecarts$railAllowsMovement(cart) && isRoot && (hasChild || isMoving);

        if (fuel <= 0 && mayStart) {
            modernminecarts$burningFurnaces = burnFuelTrain();
        }

        if (fuel > 0 && !hasDirection) {
            AbstractMinecartEntity child = modernminecarts$getChild(cart);
            if (child != null) {
                pushVec = new Vec3d(cart.getX() - child.getX(), 0.0D, cart.getZ() - child.getZ());
            } else if (isMoving) {
                pushVec = new Vec3d(cart.getVelocity().x, 0.0D, cart.getVelocity().z);
            }
        }

        boolean waitingForDirection = fuel > 0
                && pushVec.horizontalLengthSquared() <= 1.0E-7D
                && modernminecarts$getParent(cart) == null;
        if (waitingForDirection) {
            fuel++;
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void modernminecarts$finishFuelTick(CallbackInfo ci) {
        FurnaceMinecartEntity cart = modernminecarts$self();
        if (!cart.getEntityWorld().isClient()) {
            if (!modernminecarts$railAllowsMovement(cart)) {
                pushVec = Vec3d.ZERO;
                cart.setVelocity(Vec3d.ZERO);
            }
            setLit(fuel > 0);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void modernminecarts$openFuelScreen(
            PlayerEntity player,
            Hand hand,
            CallbackInfoReturnable<ActionResult> cir
    ) {
        if (!player.getEntityWorld().isClient()) {
            player.openHandledScreen(this);
        }
        cir.setReturnValue(player.getEntityWorld().isClient()
                ? ActionResult.SUCCESS
                : ActionResult.SUCCESS_SERVER);
    }

    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void modernminecarts$writeInventory(WriteView view, CallbackInfo ci) {
        Inventories.writeData(view, modernminecarts$inventory);
        view.putInt("ModernMinecartsFuelBurnTime", modernminecarts$fuelBurnTime);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void modernminecarts$readInventory(ReadView view, CallbackInfo ci) {
        modernminecarts$inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
        Inventories.readData(view, modernminecarts$inventory);
        modernminecarts$fuelBurnTime = view.getInt("ModernMinecartsFuelBurnTime", 0);
    }

    @Inject(method = "applySlowdown", at = @At("TAIL"), cancellable = true)
    private void modernminecarts$capEngineSpeed(
            Vec3d velocity,
            CallbackInfoReturnable<Vec3d> cir
    ) {
        FurnaceMinecartEntity cart = modernminecarts$self();
        if (!(cart.getEntityWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        double maxSpeed = ((MinecartInvoker) cart).invokeGetMaxSpeed(serverWorld);
        Vec3d slowed = cir.getReturnValue();
        cir.setReturnValue(new Vec3d(
                Math.max(-maxSpeed, Math.min(maxSpeed, slowed.x)),
                slowed.y,
                Math.max(-maxSpeed, Math.min(maxSpeed, slowed.z))
        ));
    }

    @Inject(method = "getMaxSpeed", at = @At("RETURN"), cancellable = true)
    private void modernminecarts$useForgeFurnaceSpeed(
            ServerWorld world,
            CallbackInfoReturnable<Double> cir
    ) {
        double railSpeed = modernminecarts$getRailSpeed();
        if (modernminecarts$getParent(modernminecarts$self()) != null) {
            cir.setReturnValue(railSpeed);
        } else {
            cir.setReturnValue(limitTrainSpeed(railSpeed));
        }
    }

    @Override
    public int burnFuelTrain() {
        int burnTime = modernminecarts$consumeFuelItem();
        if (burnTime <= 0) {
            return 0;
        }

        fuel = burnTime;
        modernminecarts$fuelBurnTime = burnTime;

        AbstractMinecartEntity child = modernminecarts$getChild(modernminecarts$self());
        if (child instanceof FurnaceMinecartEntity
                && child instanceof ContainerMinecartInteface furnaceChild) {
            return 1 + furnaceChild.burnFuelTrain();
        }
        return 1;
    }

    @Unique
    private int modernminecarts$consumeFuelItem() {
        ItemStack stack = getStack(0);
        int burnTime = modernminecarts$self().getEntityWorld().getFuelRegistry().getFuelTicks(stack);
        if (stack.isEmpty() || burnTime <= 0) {
            return 0;
        }

        Item consumedFuel = stack.getItem();
        ItemStack remainder = stack.getRecipeRemainder();
        stack.decrement(1);
        if (stack.isEmpty()) {
            setStack(0, remainder);
        }
        modernminecarts$refillFuelSlot(consumedFuel);
        return burnTime;
    }

    @Unique
    private void modernminecarts$refillFuelSlot(Item consumedFuel) {
        ItemStack fuelStack = getStack(0);

        for (Inventory inventory : getChainInventories()) {
            for (int slot = 0; slot < inventory.size() && fuelStack.getCount() < fuelStack.getMaxCount(); slot++) {
                ItemStack candidate = inventory.getStack(slot);
                if (!candidate.isOf(consumedFuel)) {
                    continue;
                }

                if (fuelStack.isEmpty()) {
                    fuelStack = candidate.copy();
                    fuelStack.setCount(0);
                    setStack(0, fuelStack);
                } else if (fuelStack.isOf(Items.BUCKET) && consumedFuel == Items.LAVA_BUCKET) {
                    setStack(0, new ItemStack(Items.LAVA_BUCKET));
                    candidate.decrement(1);
                    if (candidate.isEmpty()) {
                        inventory.setStack(slot, new ItemStack(Items.BUCKET));
                    } else {
                        modernminecarts$storeRemainder(inventory, new ItemStack(Items.BUCKET));
                    }
                    inventory.markDirty();
                    return;
                } else if (!ItemStack.areItemsAndComponentsEqual(fuelStack, candidate)) {
                    continue;
                }

                int moved = Math.min(
                        fuelStack.getMaxCount() - fuelStack.getCount(),
                        candidate.getCount()
                );
                fuelStack.increment(moved);
                candidate.decrement(moved);
                inventory.markDirty();
            }
        }
        markDirty();
    }

    @Unique
    private static void modernminecarts$storeRemainder(Inventory inventory, ItemStack remainder) {
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isEmpty()) {
                inventory.setStack(slot, remainder);
                return;
            }
            if (ItemStack.areItemsAndComponentsEqual(stack, remainder) && stack.getCount() < stack.getMaxCount()) {
                stack.increment(1);
                return;
            }
        }
    }

    @Override
    public List<Inventory> getChainInventories() {
        return TrainInventoryUtil.collectStorageInventories(
                modernminecarts$self(),
                false,
                true,
                true
        );
    }

    @Override
    public void setNumberOfChildren(int number) {
        modernminecarts$numberOfChildren = Math.max(0, number);
    }

    @Override
    public int calculateActualSpeedForDisplay() {
        AbstractMinecartEntity parent = modernminecarts$getParent(modernminecarts$self());
        if (parent instanceof ContainerMinecartInteface parentFurnace) {
            return parentFurnace.calculateActualSpeedForDisplay();
        }

        return TrainEngineLogic.calculateSpeedometerValue(
                modernminecarts$self().getVelocity().horizontalLength(),
                modernminecarts$speedForDisplay / (double) MinecartTuning.SPEEDOMETER_SCALE
        );
    }

    @Override
    public double limitTrainSpeed(double railSpeed) {
        AbstractMinecartEntity parent = modernminecarts$getParent(modernminecarts$self());
        if (parent instanceof ContainerMinecartInteface parentFurnace) {
            return parentFurnace.limitTrainSpeed(railSpeed);
        }

        double result = TrainEngineLogic.calculateSpeedLimit(
                railSpeed,
                modernminecarts$numberOfChildren,
                modernminecarts$burningFurnaces
        );
        modernminecarts$speedForDisplay = TrainEngineLogic.speedToDisplayUnits(result);
        return result;
    }

    @Override
    public @Nullable ScreenHandler createMenu(
            int syncId,
            PlayerInventory playerInventory,
            PlayerEntity player
    ) {
        return new FurnaceMinecartScreenHandler(syncId, playerInventory, this, modernminecarts$properties);
    }

    @Override
    public int size() {
        return modernminecarts$inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return modernminecarts$inventory.get(0).isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return modernminecarts$inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(modernminecarts$inventory, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(modernminecarts$inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        modernminecarts$inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        FurnaceMinecartEntity cart = modernminecarts$self();
        return !cart.isRemoved() && player.squaredDistanceTo(cart) <= 64.0D;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return modernminecarts$self().getEntityWorld().getFuelRegistry().isFuel(stack);
    }

    @Override
    public void clear() {
        modernminecarts$inventory.clear();
    }

    @Unique
    private static int modernminecarts$countChildren(AbstractMinecartEntity cart) {
        int count = 0;
        AbstractMinecartEntity current = modernminecarts$getChild(cart);
        while (current != null && count < 128) {
            count++;
            current = modernminecarts$getChild(current);
        }
        return count;
    }

    @Unique
    private static boolean modernminecarts$railAllowsMovement(FurnaceMinecartEntity cart) {
        if (!cart.isOnRail()) {
            return false;
        }
        var state = cart.getEntityWorld().getBlockState(cart.getBlockPos());
        if (!(state.getBlock() instanceof net.minecraft.block.AbstractRailBlock)) {
            state = cart.getEntityWorld().getBlockState(cart.getBlockPos().down());
        }
        if (state.getBlock() instanceof PoweredRailBlock
                && !state.isOf(net.minecraft.block.Blocks.ACTIVATOR_RAIL)
                && state.contains(PoweredRailBlock.POWERED)) {
            return state.get(PoweredRailBlock.POWERED);
        }
        if (state.getBlock() instanceof PoweredDetectorRailBlock) {
            return state.get(PoweredDetectorRailBlock.POWERED);
        }
        return true;
    }

    @Unique
    private double modernminecarts$getRailSpeed() {
        FurnaceMinecartEntity cart = modernminecarts$self();
        BlockPos railPos = cart.getBlockPos();
        BlockState railState = cart.getEntityWorld().getBlockState(railPos);
        if (!(railState.getBlock() instanceof AbstractRailBlock)) {
            railPos = railPos.down();
            railState = cart.getEntityWorld().getBlockState(railPos);
        }

        if (!(railState.getBlock() instanceof AbstractRailBlock)) {
            return MinecartTuning.VANILLA_RAIL_SPEED;
        }

        double railSpeed = modernminecarts$getParent(cart) != null
                ? MinecartTuning.COPPER_RAIL_SPEED
                : modernminecarts$getSpeedForRail(cart, railPos, railState);
        if (cart.isTouchingWater() && !railState.isOf(Blocks.POWERED_RAIL)) {
            railSpeed /= 2.0D;
        }

        if (railSpeed > 0.5D) {
            BlockPos nextPos = modernminecarts$getNextRailPos(cart, railPos);
            BlockState nextState = cart.getEntityWorld().getBlockState(nextPos);
            if (!(nextState.getBlock() instanceof AbstractRailBlock)) {
                nextPos = nextPos.down();
                nextState = cart.getEntityWorld().getBlockState(nextPos);
            }

            if (nextState.getBlock() instanceof AbstractRailBlock nextRail
                    && nextState.get(nextRail.getShapeProperty()).isAscending()) {
                railSpeed = modernminecarts$getSpeedForRail(cart, nextPos, nextState);
            }
        }

        return railSpeed;
    }

    @Unique
    private static double modernminecarts$getSpeedForRail(
            FurnaceMinecartEntity cart,
            BlockPos pos,
            BlockState state
    ) {
        if (state.isOf(ModBlocks.RAIL_CROSSING)) {
            return MinecartTuning.COPPER_RAIL_SPEED;
        }

        if (state.isOf(ModBlocks.RAIL_JUMP)) {
            RailShape shape = state.get(SlopedRailBlock.SHAPE);
            BlockPos launchPos = switch (shape) {
                case ASCENDING_NORTH -> pos.north().up();
                case ASCENDING_SOUTH -> pos.south().up();
                case ASCENDING_EAST -> pos.east().up();
                case ASCENDING_WEST -> pos.west().up();
                default -> pos;
            };
            return cart.getEntityWorld().getBlockState(launchPos).isAir()
                    ? MinecartTuning.MAXIMUM_JUMP_SPEED
                    : MinecartTuning.ASCENDING_COPPER_RAIL_SPEED;
        }

        if (state.getBlock() instanceof CopperRailBlock
                || state.getBlock() instanceof WaxedCopperRailBlock) {
            RailShape shape = state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty());
            if (shape.isAscending()) {
                if (state.isOf(ModBlocks.COPPER_RAIL)
                        || state.isOf(ModBlocks.WAXED_COPPER_RAIL)
                        || state.isOf(ModBlocks.EXPOSED_COPPER_RAIL)
                        || state.isOf(ModBlocks.WAXED_EXPOSED_COPPER_RAIL)) {
                    return MinecartTuning.ASCENDING_COPPER_RAIL_SPEED;
                }
            }

            if (state.isOf(ModBlocks.COPPER_RAIL) || state.isOf(ModBlocks.WAXED_COPPER_RAIL)) {
                return MinecartTuning.COPPER_RAIL_SPEED;
            }
            if (state.isOf(ModBlocks.EXPOSED_COPPER_RAIL)
                    || state.isOf(ModBlocks.WAXED_EXPOSED_COPPER_RAIL)) {
                return MinecartTuning.EXPOSED_COPPER_RAIL_SPEED;
            }
            if (state.isOf(ModBlocks.WEATHERED_COPPER_RAIL)
                    || state.isOf(ModBlocks.WAXED_WEATHERED_COPPER_RAIL)) {
                return MinecartTuning.WEATHERED_COPPER_RAIL_SPEED;
            }
            return MinecartTuning.OXIDIZED_COPPER_RAIL_SPEED;
        }

        return MinecartTuning.VANILLA_RAIL_SPEED;
    }

    @Unique
    private static BlockPos modernminecarts$getNextRailPos(
            FurnaceMinecartEntity cart,
            BlockPos railPos
    ) {
        Vec3d velocity = cart.getVelocity();
        if (Math.abs(velocity.x) > Math.abs(velocity.z)) {
            return velocity.x >= 0.0D ? railPos.east() : railPos.west();
        }
        return velocity.z >= 0.0D ? railPos.south() : railPos.north();
    }

    @Unique
    private static @Nullable AbstractMinecartEntity modernminecarts$getParent(AbstractMinecartEntity cart) {
        return ((ChainMinecartInterface) cart).getLinkedParent();
    }

    @Unique
    private static @Nullable AbstractMinecartEntity modernminecarts$getChild(AbstractMinecartEntity cart) {
        return ((ChainMinecartInterface) cart).getLinkedChild();
    }

    @Unique
    private FurnaceMinecartEntity modernminecarts$self() {
        return (FurnaceMinecartEntity) (Object) this;
    }
}
