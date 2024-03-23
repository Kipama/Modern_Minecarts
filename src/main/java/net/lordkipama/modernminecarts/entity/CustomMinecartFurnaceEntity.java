package net.lordkipama.modernminecarts.entity;

import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.lordkipama.modernminecarts.ModernMinecartsConfig;
import net.lordkipama.modernminecarts.block.Custom.PoweredDetectorRailBlock;
import net.lordkipama.modernminecarts.inventory.FurnaceMinecartMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

import java.util.ArrayList;
import java.util.List;

public class CustomMinecartFurnaceEntity extends CustomAbstractMinecartContainerEntity {
    private int fuel;
    private int fuelBurnTime;
    public double xPush;
    public double zPush;
    private List<ContainerEntity> otherContainers;

    public int numberOfChildren=-1;
    public int numBurningFurni=1;
    private int speedForDisplay;

    protected final ContainerData dataAccess = new ContainerData() {
        public int get(int p_58431_) {
            return switch (p_58431_) {
                case 0 -> CustomMinecartFurnaceEntity.this.fuel;
                case 1 -> CustomMinecartFurnaceEntity.this.fuelBurnTime;
                case 2 -> (CustomMinecartFurnaceEntity.this.calculateActualSpeedForDisplay());
                default -> 0;
            };
        }
        @Override
        public void set(int pIndex, int pValue) {
            switch (pIndex) {
                case 0:
                    CustomMinecartFurnaceEntity.this.fuel = pValue;
                case 1:
                    CustomMinecartFurnaceEntity.this.fuelBurnTime = pValue;
            }
        }
        @Override
        public int getCount() {
            return 3;
        }
    };

    public int calculateActualSpeedForDisplay(){
        if(this.getLinkedParent() instanceof CustomMinecartFurnaceEntity furnaceMC){
            return furnaceMC.calculateActualSpeedForDisplay();
        }
        if(this.getDeltaMovement().length()> ((double) speedForDisplay /80)){
            return speedForDisplay;
        }
        else{
            return (int)Math.round(this.getDeltaMovement().length()*80+0.2);
        }
    }

    public CustomMinecartFurnaceEntity(EntityType<? extends CustomAbstractMinecartEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CustomMinecartFurnaceEntity(Level pLevel, double pX, double pY, double pZ) {
        super(VanillaEntities.FURNACE_MINECART_ENTITY.get(), pX, pY, pZ, pLevel);
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory) {
        return new FurnaceMinecartMenu(pContainerId, pPlayerInventory,this, this.dataAccess);
    }

    @Override
    protected Item getDropItem() {
        if(getLinkedParent() != null || getLinkedChild() != null){
            level.addFreshEntity(new ItemEntity(level,this.getX(), this.getY(), this.getZ(), new ItemStack(Items.CHAIN)));
        }
        return VanillaItems.FURNACE_MINECART_ITEM.get();
    }
    @Override
    public Type getMinecartType() {
        return AbstractMinecart.Type.FURNACE;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, Boolean.FALSE);//this.hasFuel()));
    }

    public void tick() {
        if(numberOfChildren==-1){
            numberOfChildren = getNumberOfChildren();
        }
        super.tick();

        if(this.level instanceof ServerLevel server) {
            if (fuel > 0) {
                --fuel;
            }
            else if (this.getLinkedChild() != null) {
                Block block = level.getBlockState(this.getOnPos()).getBlock();
                boolean consumeFuel = (((!(block instanceof PoweredRailBlock) || ((PoweredRailBlock) block).isActivatorRail()) && !(block instanceof PoweredDetectorRailBlock)) || level.getBlockState(this.getOnPos()).getValue(PoweredRailBlock.POWERED))
                        && level.getBlockState(this.getOnPos()).is(BlockTags.RAILS)
                        && this.getLinkedParent() == null;


                if(consumeFuel) {
                    numBurningFurni = tryBurnFuel();
                }
                ItemStack fuelSlot = this.getSlot(0).get();
                if(AbstractFurnaceBlockEntity.isFuel(fuelSlot) && fuelSlot.getCount()<fuelSlot.getMaxStackSize()){
                    fillFuelSlot(fuelSlot);
                }
            }
            if (this.fuel > 0 && this.getLinkedChild()!=null && this.xPush==0 && this.zPush==0) {
                this.xPush = this.getX() - this.getLinkedChild().getX();
                this.zPush = this.getZ() - this.getLinkedChild().getZ();
                this.setDisplayBlockState(Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, Boolean.TRUE));
            }
            if (this.fuel <= 0) {
                this.xPush = 0.0D;
                this.zPush = 0.0D;
                this.setDisplayBlockState(Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, Boolean.FALSE));
            }



            if(fuel > 0 && ModernMinecartsConfig.allowFurnaceMinecartChunkloading) {
                ChunkPos chunkpos = new ChunkPos(new BlockPos(this.getX(), this.getY(), this.getZ()));
                server.getChunkSource().addRegionTicket(TicketType.PORTAL, chunkpos, 3, blockPosition());
            }

        }
        //Clientside
        else{
            //FUEL doesnt change clientside!
            if(this.getDisplayBlockState().getValue(FurnaceBlock.LIT)&& this.random.nextInt(6) == 0) {
                this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public int tryBurnFuel(){
        ItemStack fuelSlot = this.getSlot(0).get();
        if(fuelSlot.is(Items.LAVA_BUCKET)){
            fuel = ForgeHooks.getBurnTime(fuelSlot, RecipeType.SMELTING);
            fuelBurnTime = fuel;
            fuelSlot.getCraftingRemainingItem();
            this.getSlot(0).set(fuelSlot);
            replaceEmptyBucket();
            if(getLinkedChild() instanceof CustomMinecartFurnaceEntity childFurnace){
                return childFurnace.tryBurnFuel() + 1;
            }
            return 1;
        }
        else if (AbstractFurnaceBlockEntity.isFuel(fuelSlot)) {
            this.getNumberOfChildren();
            ItemStack tempFuelSlot = fuelSlot.copy();


            fuel = ForgeHooks.getBurnTime(fuelSlot, RecipeType.SMELTING);
            fuelBurnTime = fuel;
            fuelSlot.setCount(fuelSlot.getCount() - 1);
            if(fuelSlot.getCount()<1){
                fuelSlot = fillFuelSlot(tempFuelSlot);
                fuelSlot.setCount(fuelSlot.getCount() - 1);
            }

            this.getSlot(0).set(fuelSlot);
            if(getLinkedChild() instanceof CustomMinecartFurnaceEntity childFurnace){
                return childFurnace.tryBurnFuel() + 1;
            }
            return 1;
        }
        return 1;
    }

    public ItemStack fillFuelSlot(ItemStack fuelSlot) {
        if (otherContainers == null) {
            getChainInventories();
        }
        //Find same item in train
        if(otherContainers!=null) {
            for (ContainerEntity currentContainer : otherContainers) {
                for (int j = 0; j < currentContainer.getContainerSize(); j++) {
                    if (fuelSlot.getItem() == currentContainer.getItem(j).getItem()) {
                        if (fuelSlot.getCount() + currentContainer.getItem(j).getCount() <= fuelSlot.getMaxStackSize()) {
                            fuelSlot.setCount(fuelSlot.getCount() + currentContainer.getItem(j).copy().getCount());
                            this.getSlot(0).set(fuelSlot);
                            currentContainer.setItem(j, ItemStack.EMPTY);
                            currentContainer.setChanged();
                        } else {
                            currentContainer.getItem(j).setCount(currentContainer.getItem(j).getCount() - fuelSlot.getMaxStackSize() + fuelSlot.getCount());
                            fuelSlot.setCount(fuelSlot.getMaxStackSize());
                            this.getSlot(0).set(fuelSlot);
                            currentContainer.setChanged();
                            break;
                        }
                    }
                }
            }
        }
        return fuelSlot;
    }

    public void replaceEmptyBucket(){
        if (otherContainers == null) {
            getChainInventories();
        }
        //Find Lava Bucket item in train
        if(otherContainers!=null) {
            boolean pBreak=false;
            for (ContainerEntity currentContainer : otherContainers) {
                for (int j = 0; j < 27; j++) {
                    if (currentContainer.getItem(j).is(Items.LAVA_BUCKET)) {
                        this.getSlot(0).set(currentContainer.getItem(j));
                        currentContainer.setItem(j,new ItemStack(Items.BUCKET,1));
                        currentContainer.setChanged();
                        pBreak=true;
                        break;
                    }
                }
                //Recursion somehow registers containerentities multiple times, and this is easier than fixing the problem (:
                if(pBreak){
                    break;
                }
            }
        }
    }

    @Override
    protected double getMaxSpeed() {
        if(getLinkedParent()!=null){
            return getLinkedParent().getMaxSpeed();
        }
        //I know numberOfChildren-numBurningFurni+1 is the number of children that arent burning furni
        else if(numberOfChildren-numBurningFurni+1>2*numBurningFurni){
            return Math.max(0.4f-((0.4f/(10*numBurningFurni))*((numberOfChildren-numBurningFurni+1)-2*numBurningFurni)),0.2f);}
        else {
            return 0.4f;
        }
    }
    @Override
    public float getMaxCartSpeedOnRail() {
        if(getLinkedParent()!=null){
            return getLinkedParent().getMaxCartSpeedOnRail();
        }
        else if(numberOfChildren-numBurningFurni>2*numBurningFurni){
            return Math.max(0.4f-((0.4f/(10*numBurningFurni))*((numberOfChildren-numBurningFurni+1)-2*numBurningFurni)),0.2f);}
        else {
            return 0.4f;
        }
    }

    @Override
    public double getMaxSpeedWithRail() {
        double superResult = super.getMaxSpeedWithRail();
        if(getLinkedParent()!=null){
            return superResult;
        }
        if(numberOfChildren-numBurningFurni+1>2*numBurningFurni){
            speedForDisplay =  (int)((Math.max(superResult-((superResult/(10*numBurningFurni))*((numberOfChildren-numBurningFurni+1)-2*numBurningFurni)),0.2f))*80);
            return Math.max(superResult-((superResult/(10*numBurningFurni))*((numberOfChildren-numBurningFurni+1)-2*numBurningFurni)),0.2f);}
               //double i=     superResult-((superResult/20*numBurningFurni)*(numberOfChildren-numBurningFurni+1);
        else {
            speedForDisplay = (int)(superResult*80);
            return superResult;
        }
    }

    protected void moveAlongTrack(BlockPos pPos, BlockState pState) {
        BaseRailBlock baserailblock = (BaseRailBlock) pState.getBlock();
        if((baserailblock instanceof PoweredRailBlock && !((PoweredRailBlock) baserailblock).isActivatorRail()) || baserailblock instanceof PoweredDetectorRailBlock){
            if(!pState.getValue(PoweredRailBlock.POWERED)){
                this.xPush = 0;
                this.zPush = 0;
                this.setDeltaMovement(0,0,0);
            }
        }
        super.moveAlongTrack(pPos, pState);
        Vec3 vec3 = this.getDeltaMovement();
        double d2 = vec3.horizontalDistanceSqr();
        double d3 = this.xPush * this.xPush + this.zPush * this.zPush;
        if (d3 > 1.0E-4D && d2 > 0.001D) {
            double d4 = Math.sqrt(d2);
            double d5 = Math.sqrt(d3);
            this.xPush = vec3.x / d4 * d5;
            this.zPush = vec3.z / d4 * d5;
        }

    }

    protected void applyNaturalSlowdown() {
        double d0 = this.xPush * this.xPush + this.zPush * this.zPush;
        if (d0 > 1.0E-7D) {
            d0 = Math.sqrt(d0);
            this.xPush /= d0;
            this.zPush /= d0;
            Vec3 vec3 = this.getDeltaMovement().multiply(0.8D, 0.0D, 0.8D).add(this.xPush, 0.0D, this.zPush);
            if (this.isInWater()) {
                vec3 = vec3.scale(0.1D);
            }

            //this.setDeltaMovement(vec3);
            double maxSpeed = getMaxCartSpeedOnRail();
            this.setDeltaMovement(new Vec3(Math.max(Math.min(vec3.x,maxSpeed),-maxSpeed),vec3.y, Math.max(Math.min(vec3.z,getMaxCartSpeedOnRail()),-maxSpeed)));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.98D, 0.0D, 0.98D));
        }

        super.applyNaturalSlowdown();
    }

    public void getChainInventories(){
        otherContainers = this.getContainerMinecartItemstacks(new ArrayList<>(),false, true);
    }





















    /*private static final EntityDataAccessor<Boolean> DATA_ID_FUEL = SynchedEntityData.defineId(net.minecraft.world.entity.vehicle.MinecartFurnace.class, EntityDataSerializers.BOOLEAN);
    private int fuel;
    public double xPush;
    public double zPush;
    /** The fuel item used to make the minecart move. */
    /*
    private static final Ingredient INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);


    public CustomMinecartFurnaceEntity(EntityType<? extends net.lordkipama.modernminecarts.entity.CustomMinecartFurnaceEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CustomMinecartFurnaceEntity(Level pLevel, double pX, double pY, double pZ) {
        super(VanillaEntities.FURNACE_MINECART_ENTITY.get(), pX, pY, pZ, pLevel);
    }

    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.FURNACE;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_FUEL, false);
    }

    /**
     * Called to update the entity's position/logic.
     */
    /*
    public void tick() {
        super.tick();
        if (!this.level.isClientSide()) {
            if (this.fuel > 0) {
                --this.fuel;
            }

            if (this.fuel <= 0) {
                this.xPush = 0.0D;
                this.zPush = 0.0D;
            }

            this.setHasFuel(this.fuel > 0);
        }

        if (this.hasFuel() && this.random.nextInt(4) == 0) {
            this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(), 0.0D, 0.0D, 0.0D);
        }

    }

    /**
     * Gets the maximum speed for a minecart
     */
    /*
    protected double getMaxSpeed() {
        return (this.isInWater() ? 3.0D : 4.0D) / 20.0D;
    }

    protected Item getDropItem() {
        if(getLinkedParent() != null || getLinkedChild() != null){
            level.addFreshEntity(new ItemEntity(level,this.getX(), this.getY(), this.getZ(), new ItemStack(Items.CHAIN)));
        }
        return VanillaItems.FURNACE_MINECART_ITEM.get();
    }

    protected void moveAlongTrack(BlockPos pPos, BlockState pState) {
        double d0 = 1.0E-4D;
        double d1 = 0.001D;
        super.moveAlongTrack(pPos, pState);
        Vec3 vec3 = this.getDeltaMovement();
        double d2 = vec3.horizontalDistanceSqr();
        double d3 = this.xPush * this.xPush + this.zPush * this.zPush;
        if (d3 > 1.0E-4D && d2 > 0.001D) {
            double d4 = Math.sqrt(d2);
            double d5 = Math.sqrt(d3);
            this.xPush = vec3.x / d4 * d5;
            this.zPush = vec3.z / d4 * d5;
        }

    }

    protected void applyNaturalSlowdown() {
        double d0 = this.xPush * this.xPush + this.zPush * this.zPush;
        if (d0 > 1.0E-7D) {
            d0 = Math.sqrt(d0);
            this.xPush /= d0;
            this.zPush /= d0;
            Vec3 vec3 = this.getDeltaMovement().multiply(0.8D, 0.0D, 0.8D).add(this.xPush, 0.0D, this.zPush);
            if (this.isInWater()) {
                vec3 = vec3.scale(0.1D);
            }

            this.setDeltaMovement(vec3);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.98D, 0.0D, 0.98D));
        }

        super.applyNaturalSlowdown();
    }

    @Override
    protected AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory) {
        return new FurnaceMinecartMenu(pContainerId, pPlayerInventory);//ChestMenu.threeRows(pId, pPlayerInventory, this);
    }

    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        InteractionResult interactionresult = this.interactWithContainerVehicle(pPlayer);
        if (interactionresult.consumesAction()) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
        }

        /*InteractionResult ret = super.interact(pPlayer, pHand);
        if (ret.consumesAction()) return ret;
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (INGREDIENT.test(itemstack) && this.fuel + 3600 <= 32000) {
            if (!pPlayer.getAbilities().instabuild) {
                itemstack.shrink(1);
            }

            this.fuel += 3600;
        }

        if (this.fuel > 0) {
            this.xPush = this.getX() - pPlayer.getX();
            this.zPush = this.getZ() - pPlayer.getZ();
        }

        return InteractionResult.sidedSuccess(this.level.isClientSide);
        */
    /*
        return interactionresult;
    }

    @Override
    public float getMaxCartSpeedOnRail() {
        return 0.4f;
    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putDouble("PushX", this.xPush);
        pCompound.putDouble("PushZ", this.zPush);
        pCompound.putShort("Fuel", (short)this.fuel);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    /*
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.xPush = pCompound.getDouble("PushX");
        this.zPush = pCompound.getDouble("PushZ");
        this.fuel = pCompound.getShort("Fuel");
    }

    protected boolean hasFuel() {
        return this.entityData.get(DATA_ID_FUEL);
    }

    protected void setHasFuel(boolean pHasFuel) {
        this.entityData.set(DATA_ID_FUEL, pHasFuel);
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, Boolean.valueOf(this.hasFuel()));
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    public void stopOpen(Player pPlayer) {
        this.level.gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(pPlayer));
    }*/
}
