package net.devoev.vanilla_cubed

import net.devoev.vanilla_cubed.block.ModBlocks
import net.devoev.vanilla_cubed.block.entity.ModBlockEntityTypes
import net.devoev.vanilla_cubed.enchantment.ModEnchantments
import net.devoev.vanilla_cubed.entity.ModEntityTypes
import net.devoev.vanilla_cubed.entity.effect.ModStatusEffects
import net.devoev.vanilla_cubed.item.ModItemGroup
import net.devoev.vanilla_cubed.item.ModItems
import net.devoev.vanilla_cubed.loot.ModLootTables
import net.devoev.vanilla_cubed.networking.ModServerPlayNetworking
import net.devoev.vanilla_cubed.potion.ModPotions
import net.devoev.vanilla_cubed.recipe.ModBrewingRecipes
import net.devoev.vanilla_cubed.recipe.ModCraftingRecipes
import net.devoev.vanilla_cubed.screen.ModScreenHandlerTypes
import net.devoev.vanilla_cubed.world.gen.ModWorldGeneration
import net.devoev.vanilla_cubed.world.gen.feature.ModPlacedFeatures
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier

object VanillaCubed : ModInitializer {

    const val MOD_ID = "vanilla_cubed"

    override fun onInitialize() {
        ModItemGroup.init()
        ModItems.init()
        ModBlocks.init()
        ModEnchantments.init()
        ModStatusEffects.init()
        ModPotions.init()
        ModLootTables.init()
        ModCraftingRecipes.init()
        ModBrewingRecipes
        ModPlacedFeatures.init()
        ModWorldGeneration.init()
        ModEntityTypes.init()
        ModBlockEntityTypes.init()
        ModServerPlayNetworking.init()
        ModScreenHandlerTypes.init()
    }

    /**
     * Returns an [Identifier] for the given [name].
     */
    fun id(name: String): Identifier = Identifier(MOD_ID, name)
}

