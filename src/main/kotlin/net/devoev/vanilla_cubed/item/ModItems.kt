package net.devoev.vanilla_cubed.item

import net.devoev.vanilla_cubed.materials.ModArmorMaterials
import net.devoev.vanilla_cubed.materials.ModToolMaterials
import net.devoev.vanilla_cubed.util.RegistryManager
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ArmorItem
import net.minecraft.item.Item
import net.minecraft.util.registry.Registry

/**
 * All modded items.
 */
object ModItems : RegistryManager<Item>(Registry.ITEM) {

    val ELDER_GUARDIAN_SHARD = create("elder_guardian_shard", Item(ModItemGroup.MATERIALS.toSettings()))

    val GILDED_CLUSTER = create("gilded_cluster", Item(ModItemGroup.MATERIALS.toSettings()))
    val ANCIENT_GOLD_INGOT = create("ancient_gold_ingot", Item(ModItemGroup.MATERIALS.toSettings()))
    val ANCIENT_GOLD_PICKAXE = create("ancient_gold_pickaxe", ModPickaxe(ModToolMaterials.ANCIENT_GOLD, 1, 1F, ModItemGroup.TOOLS.toSettings()))
    val ANCIENT_GOLD_CHESTPLATE = create("ancient_gold_chestplate", ArmorItem(ModArmorMaterials.ANCIENT_GOLD, EquipmentSlot.CHEST, ModItemGroup.COMBAT.toSettings()))

    val AMETHYST_CRYSTAL = create("amethyst_crystal", Item(FabricItemSettings().group(ModItemGroup.MATERIALS)))
    val AMETHYST_PICKAXE = create("amethyst_pickaxe", ModPickaxe(ModToolMaterials.AMETHYST, 1, 1F, ModItemGroup.TOOLS.toSettings()))
}