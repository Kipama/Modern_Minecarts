package net.lordkipama.modernminecarts.inventory;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;

import net.lordkipama.modernminecarts.Item.CustomSmithingTemplateItem;
import net.lordkipama.modernminecarts.Item.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CustomSmithingMenu extends ItemCombinerMenu{
        public static final int TEMPLATE_SLOT = 0;
        public static final int BASE_SLOT = 1;
        public static final int ADDITIONAL_SLOT = 2;
        public static final int RESULT_SLOT = 3;
        public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
        public static final int BASE_SLOT_X_PLACEMENT = 26;
        public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
        private static final int RESULT_SLOT_X_PLACEMENT = 98;
        public static final int SLOT_Y_PLACEMENT = 48;
        private final Level level;
        @Nullable
        private RecipeHolder<SmithingRecipe> selectedRecipe;
        private final List<RecipeHolder<SmithingRecipe>> recipes;

        public CustomSmithingMenu(int pContainerId, Inventory pPlayerInventory) {
            this(pContainerId, pPlayerInventory, ContainerLevelAccess.NULL);
        }

        public CustomSmithingMenu(int pContainerId, Inventory pPlayerInventory, ContainerLevelAccess pAccess) {
            super(ModMenus.CUSTOM_SMITHING_MENU.get(), pContainerId, pPlayerInventory, pAccess);
            this.level = pPlayerInventory.player.level();
            this.recipes = this.level.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
        }


    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 8, 48, (p_266643_) -> {
            return this.recipes.stream().anyMatch((p_296885_) -> {
                return p_296885_.value().isTemplateIngredient(p_266643_);
            });
        }).withSlot(1, 26, 48, (p_286208_) -> {
            return this.recipes.stream().anyMatch((p_296880_) -> {
                return p_296880_.value().isBaseIngredient(p_286208_);
            });
        }).withSlot(2, 44, 48, (p_286207_) -> {
            return this.recipes.stream().anyMatch((p_296878_) -> {
                return p_296878_.value().isAdditionIngredient(p_286207_);
            });
        }).withResultSlot(3, 98, 48).build();
    }

        protected boolean isValidBlock(BlockState pState) {
            return pState.is(Blocks.SMITHING_TABLE);
        }

    protected boolean mayPickup(Player pPlayer, boolean pHasStack) {
        return this.selectedRecipe != null && this.selectedRecipe.value().matches(this.inputSlots, this.level);
    }

        protected void onTake(Player pPlayer, ItemStack pStack) {
            pStack.onCraftedBy(pPlayer.level(), pPlayer, pStack.getCount());
            this.resultSlots.awardUsedRecipes(pPlayer, this.getRelevantItems());
            this.shrinkStackInSlot(0);
            this.shrinkStackInSlot(1);
            this.shrinkStackInSlot(2);
            this.access.execute((p_40263_, p_40264_) -> {
                p_40263_.levelEvent(1044, p_40264_, 0);
            });
        }
        private List<ItemStack> getRelevantItems() {
            return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
        }

        private void shrinkStackInSlot(int pIndex) {
            ItemStack itemstack = this.inputSlots.getItem(pIndex);
            if(itemstack.getItem() instanceof CustomSmithingTemplateItem cstItem){ //itemstack.is(ModItems.COPPER_UPGRADE_SMITHING_TEMPLATE.get())
                cstItem.addUse(itemstack);
                if(cstItem.getUses(itemstack)>=64){
                    if(itemstack.is(ModItems.COPPER_UPGRADE_SMITHING_TEMPLATE.get())){
                        this.inputSlots.setItem(pIndex, new ItemStack(ModItems.CHIPPED_COPPER_UPGRADE_SMITHING_TEMPLATE.get(),itemstack.getCount()));
                    }
                    else if(itemstack.is(ModItems.CHIPPED_COPPER_UPGRADE_SMITHING_TEMPLATE.get())){
                        this.inputSlots.setItem(pIndex, new ItemStack(ModItems.DAMAGED_COPPER_UPGRADE_SMITHING_TEMPLATE.get(),itemstack.getCount()));
                    }
                    else if(itemstack.is(ModItems.DAMAGED_COPPER_UPGRADE_SMITHING_TEMPLATE.get())){
                        itemstack.shrink(1);
                        this.inputSlots.setItem(pIndex, itemstack);
                    }
                }
                System.out.println("Uses: " + cstItem.getUses(itemstack));
            }
            else if (!itemstack.isEmpty()) {
                itemstack.shrink(1);
                this.inputSlots.setItem(pIndex, itemstack);
            }

        }

        /**
         * Called when the Anvil Input Slot changes, calculates the new result and puts it in the output slot.
         */
        public void createResult() {
            List<RecipeHolder<SmithingRecipe>> list = this.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level);
            if (list.isEmpty()) {
                this.resultSlots.setItem(0, ItemStack.EMPTY);
            } else {
                RecipeHolder<SmithingRecipe> recipeholder = list.get(0);
                ItemStack itemstack = recipeholder.value().assemble(this.inputSlots, this.level.registryAccess());
                if (itemstack.isItemEnabled(this.level.enabledFeatures())) {
                    this.selectedRecipe = recipeholder;
                    this.resultSlots.setRecipeUsed(recipeholder);
                    this.resultSlots.setItem(0, itemstack);
                }
            }

        }

    public int getSlotToQuickMoveTo(ItemStack pStack) {
        return this.findSlotToQuickMoveTo(pStack).orElse(0);
    }

    private OptionalInt findSlotToQuickMoveTo(ItemStack pStack) {
        return this.recipes.stream().flatMapToInt((p_296882_) -> {
            return findSlotMatchingIngredient(p_296882_.value(), pStack).stream();
        }).filter((p_296883_) -> {
            return !this.getSlot(p_296883_).hasItem();
        }).findFirst();
    }

    private static OptionalInt findSlotMatchingIngredient(SmithingRecipe pRecipe, ItemStack pStack) {
        if (pRecipe.isTemplateIngredient(pStack)) {
            return OptionalInt.of(0);
        } else if (pRecipe.isBaseIngredient(pStack)) {
            return OptionalInt.of(1);
        } else {
            return pRecipe.isAdditionIngredient(pStack) ? OptionalInt.of(2) : OptionalInt.empty();
        }
    }

        /**
         * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
         * null for the initial slot that was double-clicked.
         */
        public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
            return pSlot.container != this.resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
        }

    public boolean canMoveIntoInputSlots(ItemStack pStack) {
        return this.findSlotToQuickMoveTo(pStack).isPresent();
    }

}