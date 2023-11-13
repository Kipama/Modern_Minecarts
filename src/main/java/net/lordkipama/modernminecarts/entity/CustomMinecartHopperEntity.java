package net.lordkipama.modernminecarts.entity;


import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class CustomMinecartHopperEntity extends CustomAbstractMinecartContainerEntity implements Hopper {
    private boolean enabled = true;
    private List<ContainerEntity> otherContainers;

    public CustomMinecartHopperEntity(EntityType<? extends net.lordkipama.modernminecarts.entity.CustomMinecartHopperEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CustomMinecartHopperEntity(Level pLevel, double pX, double pY, double pZ) {
        super(VanillaEntities.HOPPER_MINECART_ENTITY.get(), pX, pY, pZ, pLevel);
    }

    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.HOPPER;
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.HOPPER.defaultBlockState();
    }

    public int getDefaultDisplayOffset() {
        return 1;
    }

    /**
     * Returns the number of slots in the inventory.
     */
    public int getContainerSize() {
        return 5;
    }

    /**
     * Called every tick the minecart is on an activator rail.
     */
    public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
        boolean flag = !pReceivingPower;
        if (flag != this.isEnabled()) {
            this.setEnabled(flag);
        }

    }

    /**
     * Get whether this hopper minecart is being blocked by an activator rail.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set whether this hopper minecart is being blocked by an activator rail.
     */
    public void setEnabled(boolean pEnabled) {
        this.enabled = pEnabled;
    }

    /**
     * Gets the world X position for this hopper entity.
     */
    public double getLevelX() {
        return this.getX();
    }

    /**
     * Gets the world Y position for this hopper entity.
     */
    public double getLevelY() {
        return this.getY() + 0.5D;
    }

    /**
     * Gets the world Z position for this hopper entity.
     */
    public double getLevelZ() {
        return this.getZ();
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.isAlive() && this.isEnabled() && this.suckInItems()) {
            this.setChanged();
        }

    }
    @Override
    public void setChestVehicleItem(int pSlot, ItemStack pStack) {
        super.setChestVehicleItem(pSlot, pStack);
        System.out.println(AbstractContainerMenu.getRedstoneSignalFromContainer(this));
        if(AbstractContainerMenu.getRedstoneSignalFromContainer(this)>=3){
            moveToStorage();
        }
        else {
            getFromStorage();
        }
    }

    public boolean suckInItems() {
        if (HopperBlockEntity.suckInItems(this.level(), this)) {
            return true;
        } else {
            for(ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.25D, 0.0D, 0.25D), EntitySelector.ENTITY_STILL_ALIVE)) {
                if (HopperBlockEntity.addItem(this, itementity)) {
                    return true;
                }
            }
            return false;
        }
    }

    public List<ContainerEntity> getChainInventories(){
        otherContainers = this.getContainerMinecartItemstacks(new ArrayList<>(),true);
        otherContainers.addAll(this.getContainerMinecartItemstacks(new ArrayList<>(),false));
        return otherContainers;
    }

    public void moveToStorage(){
        if(otherContainers==null){
            getChainInventories();
        }
        ItemStack sourceStack=null;
        for(int i=0; i<4; i++){
            ItemStack currentStack = this.getItem(i);// .getChestVehicleItem(i);
            if(currentStack.getMaxStackSize()==currentStack.getCount() && otherContainers!=null) {
                sourceStack=currentStack.copy();

                //Find empty slot in train
                for(ContainerEntity currentContainer : otherContainers){
                    for(int j=0; j<27;j++){
                        if(currentContainer.canPlaceItem(j,sourceStack) && currentContainer.getItem(j).isEmpty()){
                            currentContainer.getItemStacks().set(j, sourceStack); //.setChestVehicleItem(i,sourceStack);
                            this.setChestVehicleItem(i, ItemStack.EMPTY);
                            return;
                        }
                    }
                }
            }
        }


    }
    public void getFromStorage() {
        if (otherContainers == null) {
            getChainInventories();
        }
        ItemStack currentStack = this.getItem(4);
        if (currentStack.isEmpty() && otherContainers != null) {
            //Find empty slot in train
            for (ContainerEntity currentContainer : otherContainers) {
                for (int j = 0; j < 27; j++) {
                    if (currentContainer.getItem(j).getCount() == currentContainer.getItem(j).getMaxStackSize()) {
                        this.setChestVehicleItem(4, currentContainer.getItem(j));
                        currentContainer.setItem(j, ItemStack.EMPTY);
                        currentContainer.setChanged();
                        break;
                    }
                }
                break;
            }
        }

    }

    @Override
    public void setChanged(){
        super.setChanged();
        getFromStorage();
    }

    protected Item getDropItem() {
        if(getLinkedParent() != null || getLinkedChild() != null){
            level().addFreshEntity(new ItemEntity(level(),this.getX(), this.getY(), this.getZ(), new ItemStack(Items.CHAIN)));
        }
        return VanillaItems.HOPPER_MINECART_ITEM.get();
    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("Enabled", this.enabled);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.enabled = pCompound.contains("Enabled") ? pCompound.getBoolean("Enabled") : true;
    }

    public AbstractContainerMenu createMenu(int pId, Inventory pPlayerInventory) {
        return new HopperMenu(pId, pPlayerInventory, this);
    }
    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        this.getFromStorage();
        return this.removeChestVehicleItem(pIndex, pCount);
    }
}