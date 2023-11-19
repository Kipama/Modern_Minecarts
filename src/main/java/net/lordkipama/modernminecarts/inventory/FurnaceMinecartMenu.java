package net.lordkipama.modernminecarts.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

public class FurnaceMinecartMenu extends RecipeBookMenu<Container> {
    public static final int INGREDIENT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int DATA_COUNT = 4;
    private static final int INV_SLOT_START = 3;
    private static final int INV_SLOT_END = 30;
    private static final int USE_ROW_SLOT_START = 30;
    private static final int USE_ROW_SLOT_END = 39;
    private final Container container;
    private final ContainerData data;
    protected final Level level;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;
    private final RecipeBookType recipeBookType;
    private boolean isDrivingDelay=true;


    public FurnaceMinecartMenu(int pContainerId, Inventory pPlayerInventory) {
        this(ModMenus.FURNACE_MINECART_MENU.get(), RecipeType.SMELTING, RecipeBookType.FURNACE, pContainerId, pPlayerInventory, new SimpleContainer(1), new SimpleContainerData(3));
    }
    public FurnaceMinecartMenu(int pContainerId, Inventory pPlayerInventory, ContainerData pFurnaceData) {
        this(ModMenus.FURNACE_MINECART_MENU.get(), RecipeType.SMELTING, RecipeBookType.FURNACE, pContainerId, pPlayerInventory, new SimpleContainer(1), pFurnaceData);
    }

    public FurnaceMinecartMenu(int pContainerId, Inventory pPlayerInventory, Container pContainer, ContainerData pFurnaceData) {
        this(ModMenus.FURNACE_MINECART_MENU.get(), RecipeType.SMELTING, RecipeBookType.FURNACE, pContainerId, pPlayerInventory, pContainer, pFurnaceData);
    }



    protected FurnaceMinecartMenu(MenuType<?> pMenuType, RecipeType<? extends AbstractCookingRecipe> pRecipeType, RecipeBookType p_38968_, int pContainerId, Inventory pPlayerInventory, Container pFurnaceContainer, ContainerData pFurnaceData) {

        super(pMenuType, pContainerId);
        this.recipeType = pRecipeType;
        this.recipeBookType = p_38968_;
        checkContainerSize(pFurnaceContainer, 1);
        checkContainerDataCount(pFurnaceData, 3);
        this.container = pFurnaceContainer;
        this.data = pFurnaceData;
        this.level = pPlayerInventory.player.level();
        this.addSlot(new CustomFurnaceFuelSlot(this, pFurnaceContainer, 0, 80, 45));

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
        }

        this.addDataSlots(pFurnaceData);
    }

    public void fillCraftSlotsStackedContents(StackedContents p_38976_) {
        if (this.container instanceof StackedContentsCompatible) {
            ((StackedContentsCompatible)this.container).fillStackedContents(p_38976_);
        }

    }

    public void clearCraftingContent() {
        //this.getSlot(0).set(ItemStack.EMPTY);
        //this.getSlot(2).set(ItemStack.EMPTY);
    }

    public boolean recipeMatches(Recipe<? super Container> p_38980_) {
        return p_38980_.matches(this.container, this.level);
    }

    @Override
    public int getResultSlotIndex() {
        return 0;
    }

    public int getGridWidth() {
        return 1;
    }

    public int getGridHeight() {
        return 1;
    }

    public int getSize() {
        return 1;//Was 3
    }

    public boolean stillValid(Player p_38974_) {
        return this.container.stillValid(p_38974_);
    }

    public ItemStack quickMoveStack(Player pPlayer, int pSlot) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pSlot);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pSlot ==0) {
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }






    protected boolean canSmelt(ItemStack p_38978_) {
        return this.level.getRecipeManager().getRecipeFor((RecipeType<AbstractCookingRecipe>)this.recipeType, new SimpleContainer(p_38978_), this.level).isPresent();
    }

    protected boolean isFuel(ItemStack p_38989_) {
        return AbstractFurnaceBlockEntity.isFuel(p_38989_);
    }

    public int getSpeed() {
        return this.data.get(2);
    }

    public int getLitProgress() {
        int i = this.data.get(1);
        if (i == 0) {
            i = 200;
        }

        return this.data.get(0) * 13 / i;
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    public RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }

    public boolean shouldMoveToInventory(int p_150463_) {
        return p_150463_ != 1;
    }

}
