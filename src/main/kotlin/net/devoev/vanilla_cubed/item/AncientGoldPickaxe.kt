package net.devoev.vanilla_cubed.item

import net.devoev.vanilla_cubed.tool_materials.AncientGold
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.ItemGroup
import net.minecraft.item.PickaxeItem

object AncientGoldPickaxe : PickaxeItem(AncientGold, 1, 1F, FabricItemSettings().group(ItemGroup.TOOLS))