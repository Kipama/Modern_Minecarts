package net.lordkipama.modernminecarts.Item;


import net.lordkipama.modernminecarts.ModernMinecarts;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ModernMinecarts.MOD_ID);

    public static final RegistryObject<Item> COPPER_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("copper_upgrade_smithing_template", CustomSmithingTemplateItem::createCopperUpgradeTemplate);
    public static final RegistryObject<Item> CHIPPED_COPPER_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("chipped_copper_upgrade_smithing_template", CustomSmithingTemplateItem::createChippedCopperUpgradeTemplate);
    public static final RegistryObject<Item> DAMAGED_COPPER_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("damaged_copper_upgrade_smithing_template", CustomSmithingTemplateItem::createDamagedCopperUpgradeTemplate);


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

