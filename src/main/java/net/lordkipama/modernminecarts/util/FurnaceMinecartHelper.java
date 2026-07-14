package net.lordkipama.modernminecarts.util;

import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.attachment.FurnaceMinecartData;
import net.lordkipama.modernminecarts.attachment.MinecartAttachmentTypes;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartContainer;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartDataAccess;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class FurnaceMinecartHelper {
    private static final Field FUEL_FIELD = ObfuscationReflectionHelper.findField(MinecartFurnace.class, "fuel");
    private static final Field CURRENT_SPEED_CAP_ON_RAIL_FIELD = ObfuscationReflectionHelper.findField(AbstractMinecart.class, "currentSpeedCapOnRail");

    private FurnaceMinecartHelper() {
    }

    public static FurnaceMinecartData getData(MinecartFurnace minecart) {
        return minecart.getData(MinecartAttachmentTypes.FURNACE_DATA.get());
    }

    public static int getFuel(MinecartFurnace minecart) {
        try {
            return FUEL_FIELD.getInt(minecart);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to read furnace minecart fuel", e);
        }
    }

    public static void setFuel(MinecartFurnace minecart, int fuel) {
        try {
            FUEL_FIELD.setInt(minecart, fuel);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set furnace minecart fuel", e);
        }
    }

    public static int calculateActualSpeedForDisplay(MinecartFurnace minecart) {
        AbstractMinecart parent = MinecartLinkHelper.getLinkedParent(minecart);
        if (parent instanceof MinecartFurnace parentFurnace) {
            return calculateActualSpeedForDisplay(parentFurnace);
        }
        int speedForDisplay = getSpeedForDisplay(minecart);
        if (minecart.getDeltaMovement().length() > (double) speedForDisplay / 80.0D) {
            return speedForDisplay;
        }
        return (int) Math.round(minecart.getDeltaMovement().length() * 80.0D + 0.2D);
    }

    public static MenuProvider createMenuProvider(MinecartFurnace minecart) {
        return new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, net.minecraft.world.entity.player.Player player) {
                return new FurnaceMinecartMenu(containerId, playerInventory, new FurnaceMinecartContainer(minecart), new FurnaceMinecartDataAccess(minecart));
            }

            @Override
            public net.minecraft.network.chat.Component getDisplayName() {
                return minecart.getDisplayName();
            }
        };
    }

    public static void tick(MinecartFurnace minecart) {
        FurnaceMinecartData data = getData(minecart);
        TrainStats stats = getTrainStats(minecart);
        int fuel = getFuel(minecart);
        BlockState railState = minecart.level().getBlockState(minecart.getOnPos());
        Block block = railState.getBlock();
        Vec3 movement = minecart.getDeltaMovement();
        boolean isMoving = movement.horizontalDistanceSqr() > 0.001D;
        boolean hasChild = MinecartLinkHelper.getLinkedChild(minecart) != null;
        boolean isPoweredRail = block instanceof PoweredRailBlock poweredRail && !poweredRail.isActivatorRail();
        boolean isDetectorRail = ModernMinecartsConfig.enablePoweredDetectorRail() && block instanceof PoweredDetectorRailBlock;
        boolean isRailPowered = (isPoweredRail || isDetectorRail) && railState.getValue(PoweredRailBlock.POWERED);
        boolean consumeFuel = railState.is(BlockTags.RAILS)
                && MinecartLinkHelper.getLinkedParent(minecart) == null
                && !isRailPowered
                && (hasChild || isMoving);

        if (fuel <= 0 && consumeFuel) {
            tryBurnFuel(minecart, data);
            fuel = getFuel(minecart);
        }

        ItemStack fuelSlot = data.getFuelSlot();
        if (AbstractFurnaceBlockEntity.isFuel(fuelSlot) && fuelSlot.getCount() < fuelSlot.getMaxStackSize()) {
            fillFuelSlot(minecart, fuelSlot);
        }

        if (fuel > 0 && minecart.xPush == 0.0D && minecart.zPush == 0.0D) {
            AbstractMinecart child = MinecartLinkHelper.getLinkedChild(minecart);
            if (child != null) {
                minecart.xPush = minecart.getX() - child.getX();
                minecart.zPush = minecart.getZ() - child.getZ();
            } else if (isMoving) {
                minecart.xPush = movement.x;
                minecart.zPush = movement.z;
            }
        }

        if (fuel <= 0) {
            minecart.xPush = 0.0D;
            minecart.zPush = 0.0D;
        } else if (MinecartLinkHelper.getLinkedParent(minecart) == null) {
            setCurrentSpeedCapOnRailUnchecked(minecart, (float) getTargetSpeed(minecart, stats));
            applyTargetSpeed(minecart, getTargetSpeed(minecart, stats), hasChild || isMoving);
        }

        minecart.setDisplayBlockState(Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, fuel > 0));

        if (fuel > 0 && minecart.level() instanceof ServerLevel server && ModernMinecartsConfig.enableFurnaceMinecartChunkloading()) {
            ChunkPos chunkPos = new ChunkPos(BlockPos.containing(minecart.getX(), minecart.getY(), minecart.getZ()));
            server.getChunkSource().addRegionTicket(TicketType.PORTAL, chunkPos, 3, minecart.blockPosition());
        }
    }

    public static int tryBurnFuel(MinecartFurnace minecart, FurnaceMinecartData data) {
        ItemStack fuelSlot = data.getFuelSlot();
        if (fuelSlot.is(Items.LAVA_BUCKET)) {
            setFuel(minecart, fuelSlot.getBurnTime(RecipeType.SMELTING));
            data.setFuelBurnTime(getFuel(minecart));
            data.setFuelSlot(new ItemStack(Items.BUCKET));
            if (MinecartLinkHelper.getLinkedChild(minecart) instanceof MinecartFurnace childFurnace) {
                return tryBurnFuel(childFurnace, getData(childFurnace)) + 1;
            }
            return 1;
        }

        if (AbstractFurnaceBlockEntity.isFuel(fuelSlot)) {
            ItemStack template = fuelSlot.copy();
            setFuel(minecart, fuelSlot.getBurnTime(RecipeType.SMELTING));
            data.setFuelBurnTime(getFuel(minecart));
            fuelSlot.shrink(1);
            if (fuelSlot.isEmpty()) {
                fuelSlot = fillFuelSlot(minecart, template);
                fuelSlot.shrink(1);
            }
            data.setFuelSlot(fuelSlot);
            if (MinecartLinkHelper.getLinkedChild(minecart) instanceof MinecartFurnace childFurnace) {
                return tryBurnFuel(childFurnace, getData(childFurnace)) + 1;
            }
        }

        return 1;
    }

    public static ItemStack fillFuelSlot(MinecartFurnace minecart, ItemStack fuelSlot) {
        for (ContainerEntity container : getChainInventories(minecart, true)) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack candidate = container.getItem(slot);
                if (candidate.isEmpty() || candidate.getItem() != fuelSlot.getItem()) {
                    continue;
                }

                int transfer = Math.min(fuelSlot.getMaxStackSize() - fuelSlot.getCount(), candidate.getCount());
                if (transfer <= 0) {
                    continue;
                }

                fuelSlot.grow(transfer);
                candidate.shrink(transfer);
                if (candidate.isEmpty()) {
                    container.setItem(slot, ItemStack.EMPTY);
                }
                container.setChanged();

                if (fuelSlot.getCount() >= fuelSlot.getMaxStackSize()) {
                    return fuelSlot;
                }
            }
        }

        return fuelSlot;
    }

    public static List<ContainerEntity> getChainInventories(AbstractMinecart minecart, boolean checkHoppers) {
        List<ContainerEntity> containers = new ArrayList<>();
        AbstractMinecart head = minecart;
        while (MinecartLinkHelper.getLinkedParent(head) != null) {
            head = MinecartLinkHelper.getLinkedParent(head);
        }

        AbstractMinecart current = head;
        while (current != null) {
            if (current instanceof ContainerEntity containerEntity) {
                boolean skip = !checkHoppers && current.getMinecartType() == AbstractMinecart.Type.HOPPER;
                if (!skip) {
                    containers.add(containerEntity);
                }
            }
            current = MinecartLinkHelper.getLinkedChild(current);
        }

        return containers;
    }

    public static double getAppliedRailSpeed(MinecartFurnace minecart) {
        AbstractMinecart parent = MinecartLinkHelper.getLinkedParent(minecart);
        if (parent instanceof MinecartFurnace parentFurnace) {
            return getAppliedRailSpeed(parentFurnace);
        }
        return getTargetSpeed(minecart, getTrainStats(minecart));
    }

    public static double getTargetSpeed(MinecartFurnace minecart, TrainStats stats) {
        double baseSpeed = Math.min(getBaseRailSpeed(minecart), 0.4D);
        int nonBurningCarts = Math.max(0, stats.totalCarts - stats.burningFurnaces);
        if (stats.burningFurnaces > 0 && nonBurningCarts > 2 * stats.burningFurnaces) {
            double penaltySteps = nonBurningCarts - 2.0D * stats.burningFurnaces;
            return Math.max(baseSpeed - ((baseSpeed / (10.0D * stats.burningFurnaces)) * penaltySteps), 0.2D);
        }

        return baseSpeed;
    }

    private static int getSpeedForDisplay(MinecartFurnace minecart) {
        return (int) Math.round(getTargetSpeed(minecart, getTrainStats(minecart)) * 80.0D);
    }

    private static double getBaseRailSpeed(MinecartFurnace minecart) {
        BlockPos railPos = minecart.getOnPos();
        BlockState railState = minecart.level().getBlockState(railPos);
        if (railState.getBlock() instanceof BaseRailBlock railBlock) {
            if (railState.is(Blocks.POWERED_RAIL)) {
                return ModernMinecartsConfig.poweredRailSpeed();
            }
            if (usesDefaultFurnaceRailSpeed(railBlock)) {
                return minecart.isInWater() ? 0.2D : 0.4D;
            }
            return railBlock.getRailMaxSpeed(railState, minecart.level(), railPos, minecart);
        }
        return 0.4D;
    }

    private static boolean usesDefaultFurnaceRailSpeed(BaseRailBlock railBlock) {
        try {
            Method method = railBlock.getClass().getMethod("getRailMaxSpeed", BlockState.class, Level.class, BlockPos.class, AbstractMinecart.class);
            return "net.neoforged.neoforge.common.extensions.IBaseRailBlockExtension".equals(method.getDeclaringClass().getName());
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static void applyTargetSpeed(MinecartFurnace minecart, double targetSpeed, boolean shouldMove) {
        if (!shouldMove) {
            return;
        }

        Vec3 movement = minecart.getDeltaMovement();
        double horizontalSpeed = movement.horizontalDistance();
        if (horizontalSpeed <= 1.0E-5D) {
            Vec3 pushDirection = new Vec3(minecart.xPush, 0.0D, minecart.zPush);
            if (pushDirection.horizontalDistanceSqr() <= 1.0E-5D) {
                return;
            }

            Vec3 boosted = pushDirection.normalize().scale(targetSpeed);
            minecart.setDeltaMovement(boosted.x, movement.y, boosted.z);
            return;
        }

        Vec3 adjusted = movement.scale(targetSpeed / horizontalSpeed);
        if (Math.abs(horizontalSpeed - targetSpeed) <= 0.005D) {
            return;
        }
        minecart.setDeltaMovement(adjusted.x, movement.y, adjusted.z);
    }

    private static void setCurrentSpeedCapOnRailUnchecked(AbstractMinecart minecart, float speedCap) {
        try {
            CURRENT_SPEED_CAP_ON_RAIL_FIELD.setFloat(minecart, speedCap);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set minecart rail speed cap", e);
        }
    }

    public static TrainStats getTrainStats(AbstractMinecart minecart) {
        int totalCarts = 0;
        int burningFurnaces = 0;
        AbstractMinecart head = minecart;
        while (MinecartLinkHelper.getLinkedParent(head) != null) {
            head = MinecartLinkHelper.getLinkedParent(head);
        }

        AbstractMinecart current = head;
        while (current != null) {
            totalCarts++;
            if (current instanceof MinecartFurnace furnaceMinecart && getFuel(furnaceMinecart) > 0) {
                burningFurnaces++;
            }
            current = MinecartLinkHelper.getLinkedChild(current);
        }

        return new TrainStats(totalCarts, Math.max(1, burningFurnaces));
    }

    public record TrainStats(int totalCarts, int burningFurnaces) {
    }
}
