package net.lordkipama.modernminecarts.Item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

public class CustomSmithingTemplateItem extends SmithingTemplateItem{ // Item{
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final Component COPPER_UPGRADE_APPLIES_TO = Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("modernminecarts","smithing_template.copper_upgrade.applies_to"))).withStyle(DESCRIPTION_FORMAT);
    private static final Component COPPER_UPGRADE_INGREDIENTS = Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("modernminecarts","smithing_template.copper_upgrade.ingredients"))).withStyle(DESCRIPTION_FORMAT);
    private static final Component COPPER_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", new ResourceLocation("modernminecarts","copper_upgrade"))).withStyle(TITLE_FORMAT);
    private static final Component CHIPPED_COPPER_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", new ResourceLocation("modernminecarts","chipped_copper_upgrade"))).withStyle(TITLE_FORMAT);
    private static final Component DAMAGED_COPPER_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", new ResourceLocation("modernminecarts","damaged_copper_upgrade"))).withStyle(TITLE_FORMAT);
    private static final Component COPPER_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("modernminecarts","smithing_template.copper_upgrade.base_slot_description")));
    private static final Component COPPER_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", new ResourceLocation("modernminecarts","smithing_template.copper_upgrade.additions_slot_description")));
    private static final ResourceLocation EMPTY_SLOT_RAIL = new ResourceLocation("modernminecarts","item/empty_slot_rail");
    private static final ResourceLocation EMPTY_SLOT_INGOT = new ResourceLocation("item/empty_slot_ingot");

    public CustomSmithingTemplateItem(Component pAppliesTo, Component pIngredients, Component pUpdradeDescription, Component pBaseSlotDescription, Component pAdditionsSlotDescription, List<ResourceLocation> pBaseSlotEmptyIcons, List<ResourceLocation> pAdditonalSlotEmptyIcons) {
        super(pAppliesTo, pIngredients, pUpdradeDescription, pBaseSlotDescription, pAdditionsSlotDescription, pBaseSlotEmptyIcons, pAdditonalSlotEmptyIcons);
    }

    public void addUse(ItemStack pStack){
        if(pStack.getTag()!=null){
            double uses = pStack.getTag().getDouble("modernminecarts.times_used");
            uses = uses +  1 /(double)pStack.getCount();
            CompoundTag newTag = new CompoundTag();
            newTag.putDouble("modernminecarts.times_used", uses);
            pStack.setTag(newTag);
        }
        else{
            double uses = 1 /(double)pStack.getCount();
            CompoundTag newTag = new CompoundTag();
            newTag.putDouble("modernminecarts.times_used", uses);
            pStack.setTag(newTag);
        }
    }

    public double getUses(ItemStack pStack){
        return pStack.getTag().getDouble("modernminecarts.times_used");
    }

    public static CustomSmithingTemplateItem createCopperUpgradeTemplate() {
        return new CustomSmithingTemplateItem(COPPER_UPGRADE_APPLIES_TO, COPPER_UPGRADE_INGREDIENTS, COPPER_UPGRADE, COPPER_UPGRADE_BASE_SLOT_DESCRIPTION, COPPER_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, createCopperUpgradeIconList(), createCopperUpgradeMaterialList());
    }
    public static CustomSmithingTemplateItem createChippedCopperUpgradeTemplate() {
        return new CustomSmithingTemplateItem(COPPER_UPGRADE_APPLIES_TO, COPPER_UPGRADE_INGREDIENTS, CHIPPED_COPPER_UPGRADE, COPPER_UPGRADE_BASE_SLOT_DESCRIPTION, COPPER_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, createCopperUpgradeIconList(), createCopperUpgradeMaterialList());
    }
    public static CustomSmithingTemplateItem createDamagedCopperUpgradeTemplate() {
        return new CustomSmithingTemplateItem(COPPER_UPGRADE_APPLIES_TO, COPPER_UPGRADE_INGREDIENTS, DAMAGED_COPPER_UPGRADE, COPPER_UPGRADE_BASE_SLOT_DESCRIPTION, COPPER_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, createCopperUpgradeIconList(), createCopperUpgradeMaterialList());
    }

    private static List<ResourceLocation> createCopperUpgradeIconList() {
        return List.of(EMPTY_SLOT_RAIL);
    }

    private static List<ResourceLocation> createCopperUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_INGOT);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack)
    {
        if(itemStack.getTag()!=null) {
            double uses = itemStack.getTag().getDouble("modernminecarts.times_used");
            CompoundTag newTag = new CompoundTag();
            newTag.putDouble("modernminecarts.times_used", uses);
            ItemStack newStack = new ItemStack(this.asItem());
            newStack.setTag(newTag);
            return newStack;
        }
        else
        {
            return new ItemStack(this.asItem());
        }
    }

    @Override
    public boolean hasCraftingRemainingItem()
    {
        return true;
    }

}
