package net.devoev.vanilla_cubed.item.behavior

import net.devoev.vanilla_cubed.entity.effect.StatusEffectHelper
import net.devoev.vanilla_cubed.item.tool.ModTools
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ToolItem
import kotlin.random.Random

/**
 * Applies a random harmful [StatusEffectInstance] to a target with a given [probability].
 * @see ModTools.ANCIENT_GOLD
 */
class ApplyHarmfulEffectBehavior(private val probability: Double,
                                 private val durationRange: IntRange,
                                 private val amplifierRange: IntRange) : PostHitBehavior<ToolItem> {

    override fun accept(item: ToolItem, params: PostHitParams) {
        val effect = StatusEffectHelper.randomHarmful(durationRange, amplifierRange)
        if (Random.nextDouble() < 1 - probability || effect.effectType.isInstant) return

        params.target?.addStatusEffect(effect)
    }
}