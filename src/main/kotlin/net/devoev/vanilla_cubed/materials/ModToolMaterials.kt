package net.devoev.vanilla_cubed.materials

import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.ToolMaterial
import net.minecraft.recipe.Ingredient

enum class ModToolMaterials(
    private val miningLevel: Int,
    private val itemDurability: Int,
    private val miningSpeed: Float,
    private val attackDamage: Float,
    private val enchantability: Int,
    private val repairIngredient: Ingredient
) : ToolMaterial {

    AMETHYST(4, 1561, 6F, 3F, 10, Items.AMETHYST_SHARD);

    constructor(miningLevel: Int, itemDurability: Int, miningSpeed: Float, attackDamage: Float, enchantability: Int, vararg repairItems: Item)
            : this(miningLevel, itemDurability, miningSpeed, attackDamage, enchantability, Ingredient.ofItems(*repairItems))

    override fun getDurability(): Int = itemDurability

    override fun getMiningSpeedMultiplier(): Float = miningSpeed

    override fun getAttackDamage(): Float = attackDamage

    override fun getMiningLevel(): Int = miningLevel

    override fun getEnchantability(): Int = enchantability

    override fun getRepairIngredient(): Ingredient = repairIngredient
}