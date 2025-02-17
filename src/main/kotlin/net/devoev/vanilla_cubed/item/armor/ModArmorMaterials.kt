package net.devoev.vanilla_cubed.item.armor

import net.devoev.vanilla_cubed.VanillaCubed
import net.devoev.vanilla_cubed.item.ModItems
import net.minecraft.item.ArmorItem
import net.minecraft.item.ArmorMaterial
import net.minecraft.item.ArmorMaterials
import net.minecraft.item.Item
import net.minecraft.recipe.Ingredient
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents

/**
 * All modded armor materials.
 */
enum class ModArmorMaterials(
    private val materialName: String,
    private val durabilityMultiplier: Int,
    private val protectionAmounts: IntArray,
    private val enchantability: Int,
    private val equipSound: SoundEvent,
    private val toughness: Float,
    private val knockbackResistance: Float,
    private val repairIngredient: Lazy<Ingredient>
) : ArmorMaterial {

    AMETHYST(name = "amethyst", protectionAmounts = intArrayOf(4, 7, 9, 4), equipSound = SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, repairItem = { ModItems.CHARGED_AMETHYST_CRYSTAL }),
    ANCIENT_GOLD(name = "ancient_gold", enchantability = 50, equipSound = SoundEvents.ITEM_ARMOR_EQUIP_GOLD, repairItem = { ModItems.ANCIENT_GOLD_INGOT }),
    ENDERITE(name = "enderite", toughness = 3F, knockbackResistance = 0.1F, enchantability = 15, durabilityMultiplier = 37, equipSound = SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, repairItem = { ModItems.ENDERITE_INGOT }),
    DRAGON_SCALE(name = "dragon_scale", equipSound = SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE, repairItem = { ModItems.INFUSED_DRAGON_SCALE });

    /**
     * Constructs a new [ArmorMaterial]. Default values are the stats of [ArmorMaterials.DIAMOND].
     */
    constructor(name: String,
                durabilityMultiplier: Int = 33,
                protectionAmounts: IntArray = intArrayOf(3, 6, 8, 3),
                enchantability: Int = 10,
                equipSound: SoundEvent,
                toughness: Float = 2F,
                knockbackResistance: Float = 0F,
                repairItem: () -> Item)
        : this(name, durabilityMultiplier, protectionAmounts, enchantability, equipSound, toughness, knockbackResistance, lazy { Ingredient.ofItems(repairItem()) })

    override fun getDurability(type: ArmorItem.Type): Int = BASE_DURABILITY[type.equipmentSlot.entitySlotId] * durabilityMultiplier

    override fun getProtection(type: ArmorItem.Type): Int = protectionAmounts[type.equipmentSlot.entitySlotId]

    override fun getEnchantability(): Int = enchantability

    override fun getEquipSound(): SoundEvent = equipSound

    override fun getRepairIngredient(): Ingredient = repairIngredient.value

    override fun getName(): String = "${VanillaCubed.MOD_ID}:$materialName"

    override fun getToughness(): Float = toughness

    override fun getKnockbackResistance(): Float = knockbackResistance

    companion object {

        /**
         * The base durability values for boots, leggings, chestplate and helmet.
         */
        private val BASE_DURABILITY: IntArray = intArrayOf(13, 15, 16, 11)
    }
}