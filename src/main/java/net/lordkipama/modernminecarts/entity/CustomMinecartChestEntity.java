package net.lordkipama.modernminecarts.entity;


import net.lordkipama.modernminecarts.Item.VanillaItems;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class CustomMinecartChestEntity extends CustomAbstractMinecartContainerEntity {
    public CustomMinecartChestEntity(EntityType<? extends CustomAbstractMinecartEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CustomMinecartChestEntity(Level pLevel, double pX, double pY, double pZ) {
        super(VanillaEntities.CHEST_MINECART_ENTITY.get(), pX, pY, pZ, pLevel);
    }


    /**
     * Returns the number of slots in the inventory.
     */
    public int getContainerSize() {
        return 27;
    }

    public AbstractMinecart.Type getMinecartType() {
        return AbstractMinecart.Type.CHEST;
    }

    public BlockState getDefaultDisplayBlockState() {
        return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
    }

    public int getDefaultDisplayOffset() {
        return 8;
    }

    public AbstractContainerMenu createMenu(int pId, Inventory pPlayerInventory) {
        return ChestMenu.threeRows(pId, pPlayerInventory, this);
    }

    public void stopOpen(Player pPlayer) {
        this.level.gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(pPlayer));
    }

    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        InteractionResult interactionresult = this.interactWithChestVehicle(this::gameEvent, pPlayer);
        if (interactionresult.consumesAction()) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, pPlayer);
            PiglinAi.angerNearbyPiglins(pPlayer, true);
        }

        return interactionresult;
    }

    protected Item getDropItem() {
        if(getLinkedParent() != null || getLinkedChild() != null){
            level.addFreshEntity(new ItemEntity(level,this.getX(), this.getY(), this.getZ(), new ItemStack(Items.CHAIN)));
        }

        return VanillaItems.CHEST_MINECART_ITEM.get();
    }


}