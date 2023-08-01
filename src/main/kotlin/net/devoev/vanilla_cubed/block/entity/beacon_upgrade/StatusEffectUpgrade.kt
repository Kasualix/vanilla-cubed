package net.devoev.vanilla_cubed.block.entity.beacon_upgrade

import net.devoev.vanilla_cubed.block.entity.ModBeaconBlockEntity
import net.devoev.vanilla_cubed.client.gui.screen.ingame.BeaconUpgradeTier
import net.minecraft.block.BlockState
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * A beacon upgrade that applies the given [effect] to all players in the range.
 * @property effect Status effect to apply.
 * @property amplifier Level of the effect.
 */
class StatusEffectUpgrade(private val effect: StatusEffect, private val amplifier: Int) : BeaconUpgrade {
    override fun ModBeaconBlockEntity.accept(world: World, pos: BlockPos, state: BlockState) {
        val tier = BeaconUpgradeTier.levelToTier(currentLevel)
        val duration: Int = (9 + tier * 2) * 20

        val players = world.getNonSpectatingEntities(PlayerEntity::class.java, boxRange)
        for (playerEntity in players) {
            playerEntity.addStatusEffect(StatusEffectInstance(effect, duration, amplifier, true, true))
        }
    }
}