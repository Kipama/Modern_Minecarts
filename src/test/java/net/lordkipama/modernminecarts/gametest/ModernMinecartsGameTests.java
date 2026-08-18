package net.lordkipama.modernminecarts.gametest;

import net.lordkipama.modernminecarts.ModernMinecarts;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.ModBlocks;
import net.lordkipama.modernminecarts.util.FurnaceMinecartHelper;
import net.lordkipama.modernminecarts.util.MinecartLinkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Headless regression coverage for the deterministic NeoForge v1.2.1 and
 * v1.2.2 minecart fixes. The production mod registers it only for dedicated
 * GameTest runs; it is never included in the published mod jar.
 */
public final class ModernMinecartsGameTests {
    private static final int RAIL_Y = 1;
    private static final int TRACK_Z = 3;
    private static final int TRACK_START = 1;
    private static final int TRACK_END = 20;

    private static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS = DeferredRegister.create(
            BuiltInRegistries.TEST_FUNCTION,
            ModernMinecarts.MOD_ID
    );

    static {
        TEST_FUNCTIONS.register("copper_rail_accelerates_minecart", () -> ModernMinecartsGameTests::copperRailAcceleratesMinecart);
        TEST_FUNCTIONS.register("unlit_furnace_matches_powered_rail_speed", () -> ModernMinecartsGameTests::unlitFurnaceMinecartMatchesPoweredRailSpeed);
        TEST_FUNCTIONS.register("burning_furnace_uses_configured_speed", () -> ModernMinecartsGameTests::burningFurnaceMinecartUsesConfiguredSpeed);
        TEST_FUNCTIONS.register("furnace_stops_and_restarts_with_power", () -> ModernMinecartsGameTests::furnaceMinecartStopsAndRestartsWithPoweredRail);
        TEST_FUNCTIONS.register("burning_furnace_does_not_drive_off_rails", () -> ModernMinecartsGameTests::burningFurnaceMinecartDoesNotPropelOffRails);
    }

    public static void register(IEventBus modEventBus) {
        TEST_FUNCTIONS.register(modEventBus);
    }

    private static void copperRailAcceleratesMinecart(GameTestHelper helper) {
        buildTrack(helper, TRACK_Z, ModBlocks.COPPER_RAIL.get());
        BlockPos powerPos = new BlockPos(8, RAIL_Y + 1, TRACK_Z);
        helper.setBlock(powerPos, Blocks.REDSTONE_BLOCK);

        AbstractMinecart minecart = helper.spawn(EntityType.MINECART, new Vec3(3.5D, RAIL_Y + 0.0625D, TRACK_Z + 0.5D));
        minecart.setDeltaMovement(0.03D, 0.0D, 0.0D);
        double startX = minecart.getX();

        helper.runAfterDelay(8, () -> {
            helper.assertTrue(helper.getBlockState(new BlockPos(8, RAIL_Y, TRACK_Z)).getValue(PoweredRailBlock.POWERED),
                    "Copper rail did not receive redstone power");
            helper.assertTrue(minecart.getX() > startX + 0.5D,
                    "Minecart was not accelerated by a powered copper rail");
            helper.assertTrue(minecart.getDeltaMovement().horizontalDistance() > 0.1D,
                    "Minecart did not gain meaningful speed on a powered copper rail");
            helper.succeed();
        });
    }

    private static void unlitFurnaceMinecartMatchesPoweredRailSpeed(GameTestHelper helper) {
        buildTrack(helper, 2, Blocks.POWERED_RAIL);
        buildTrack(helper, 5, Blocks.POWERED_RAIL);
        helper.setBlock(new BlockPos(8, RAIL_Y + 1, 2), Blocks.REDSTONE_BLOCK);
        helper.setBlock(new BlockPos(8, RAIL_Y + 1, 5), Blocks.REDSTONE_BLOCK);

        AbstractMinecart regular = helper.spawn(EntityType.MINECART, new Vec3(3.5D, RAIL_Y + 0.0625D, 2.5D));
        MinecartFurnace furnace = helper.spawn(EntityType.FURNACE_MINECART, new Vec3(3.5D, RAIL_Y + 0.0625D, 5.5D));
        regular.setDeltaMovement(0.39D, 0.0D, 0.0D);
        furnace.setDeltaMovement(0.39D, 0.0D, 0.0D);

        helper.runAfterDelay(3, () -> {
            double regularSpeed = regular.getDeltaMovement().horizontalDistance();
            double furnaceSpeed = furnace.getDeltaMovement().horizontalDistance();
            helper.assertTrue(furnaceSpeed >= regularSpeed * 0.9D,
                    "Unlit furnace minecart is slower than a regular minecart on a powered rail");
            helper.succeed();
        });
    }

    private static void burningFurnaceMinecartUsesConfiguredSpeed(GameTestHelper helper) {
        buildTrack(helper, TRACK_Z, Blocks.RAIL);

        MinecartFurnace furnace = helper.spawn(EntityType.FURNACE_MINECART, new Vec3(3.5D, RAIL_Y + 0.0625D, TRACK_Z + 0.5D));
        FurnaceMinecartHelper.setFuel(furnace, 200);
        furnace.push = new Vec3(1.0D, 0.0D, 0.0D);
        furnace.setDeltaMovement(0.05D, 0.0D, 0.0D);
        double startX = furnace.getX();

        helper.runAfterDelay(2, () -> {
            double configuredSpeed = ModernMinecartsConfig.furnaceMinecartSpeed();
            helper.assertTrue(Math.abs(FurnaceMinecartHelper.getAppliedRailSpeed(furnace) - configuredSpeed) < 1.0E-4D,
                    "Burning furnace minecart did not use the configured furnace speed");
            helper.assertTrue(furnace.getX() > startX + 0.3D,
                    "Burning furnace minecart did not propel itself at its configured speed");
            helper.succeed();
        });
    }

    private static void furnaceMinecartStopsAndRestartsWithPoweredRail(GameTestHelper helper) {
        buildTrack(helper, TRACK_Z, Blocks.POWERED_RAIL);
        BlockPos powerPos = new BlockPos(8, RAIL_Y + 1, TRACK_Z);

        MinecartFurnace furnace = helper.spawn(EntityType.FURNACE_MINECART, new Vec3(7.5D, RAIL_Y + 0.0625D, TRACK_Z + 0.5D));
        AbstractMinecart trailer = helper.spawn(EntityType.MINECART, new Vec3(6.0D, RAIL_Y + 0.0625D, TRACK_Z + 0.5D));
        MinecartLinkHelper.setParentChild(furnace, trailer);
        FurnaceMinecartHelper.getData(furnace).setFuelSlot(new ItemStack(Items.COAL, 1));

        helper.runAfterDelay(4, () -> {
            helper.assertValueEqual(0, FurnaceMinecartHelper.getFuel(furnace),
                    "Furnace minecart started fuel on an unpowered rail");
            helper.assertValueEqual(1, FurnaceMinecartHelper.getData(furnace).getFuelSlot().getCount(),
                    "Furnace minecart consumed fuel on an unpowered rail");
            helper.setBlock(powerPos, Blocks.REDSTONE_BLOCK);
        });

        AtomicInteger burningFuel = new AtomicInteger();
        AtomicReference<Double> positionBeforeBrake = new AtomicReference<>();
        helper.runAfterDelay(16, () -> {
            int fuel = FurnaceMinecartHelper.getFuel(furnace);
            helper.assertTrue(fuel > 0, "Furnace minecart did not begin burning on a powered rail");
            helper.assertTrue(furnace.push.x > 0.0D, "Furnace minecart did not use its linked cart to choose a direction");
            helper.assertTrue(furnace.getX() > 7.8D, "Furnace minecart did not propel its linked train on a powered rail");
            helper.assertTrue(trailer.getX() > 6.1D, "Linked minecart did not follow the furnace minecart");
            burningFuel.set(fuel);
            positionBeforeBrake.set(furnace.getX());
            helper.setBlock(powerPos, Blocks.AIR);
            furnace.setDeltaMovement(Vec3.ZERO);
        });

        helper.runAfterDelay(24, () -> {
            helper.assertTrue(FurnaceMinecartHelper.getFuel(furnace) < burningFuel.get(),
                    "Burning furnace minecart stopped consuming its current fuel on an unpowered rail");
            helper.assertTrue(furnace.push.horizontalDistanceSqr() < 1.0E-7D,
                    "Furnace minecart continued applying propulsion on an unpowered rail");
            helper.assertTrue(Math.abs(furnace.getX() - positionBeforeBrake.get()) < 0.2D,
                    "Unpowered rail did not hand movement back to normal braking logic");
            helper.setBlock(powerPos, Blocks.REDSTONE_BLOCK);
        });

        helper.runAfterDelay(32, () -> {
            helper.assertTrue(furnace.push.x > 0.0D,
                    "Furnace minecart did not restore linked-cart propulsion after the rail was repowered");
            helper.assertTrue(furnace.getX() > positionBeforeBrake.get() + 0.2D,
                    "Furnace minecart did not move again after the rail was repowered");
            helper.succeed();
        });
    }

    private static void burningFurnaceMinecartDoesNotPropelOffRails(GameTestHelper helper) {
        MinecartFurnace furnace = helper.spawn(EntityType.FURNACE_MINECART, new Vec3(6.5D, RAIL_Y + 0.0625D, TRACK_Z + 0.5D));
        AbstractMinecart trailer = helper.spawn(EntityType.MINECART, new Vec3(5.0D, RAIL_Y + 0.0625D, TRACK_Z + 0.5D));
        MinecartLinkHelper.setParentChild(furnace, trailer);
        FurnaceMinecartHelper.setFuel(furnace, 200);
        double startX = furnace.getX();

        helper.runAfterDelay(5, () -> {
            helper.assertTrue(furnace.push.horizontalDistanceSqr() < 1.0E-7D,
                    "Burning furnace minecart retained propulsion while off rails");
            helper.assertTrue(Math.abs(furnace.getX() - startX) < 0.05D,
                    "Burning furnace minecart drove itself while off rails");
            helper.succeed();
        });
    }

    private static void buildTrack(GameTestHelper helper, int z, Block rail) {
        BlockState railState = rail instanceof RailBlock
                ? rail.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST)
                : rail.defaultBlockState().setValue(PoweredRailBlock.SHAPE, RailShape.EAST_WEST);
        for (int x = TRACK_START; x <= TRACK_END; x++) {
            helper.setBlock(new BlockPos(x, RAIL_Y - 1, z), Blocks.STONE);
            helper.setBlock(new BlockPos(x, RAIL_Y, z), railState);
        }
    }
}
