package net.devoev.vanilla_cubed.block.entity

import net.devoev.vanilla_cubed.block.entity.beacon_upgrade.BeaconUpgrade
import net.devoev.vanilla_cubed.block.entity.beacon_upgrade.BeaconUpgrades
import net.devoev.vanilla_cubed.client.gui.screen.ingame.BeaconUpgradeTier
import net.devoev.vanilla_cubed.screen.ModBeaconScreenHandler
import net.devoev.vanilla_cubed.screen.levels
import net.devoev.vanilla_cubed.screen.upgrade
import net.devoev.vanilla_cubed.util.math.boxOf
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.Stainable
import net.minecraft.block.entity.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.ContainerLock
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.Packet
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.screen.NamedScreenHandlerFactory
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.Heightmap
import net.minecraft.world.World
import kotlin.properties.Delegates


/**
 * Block entity for the modded beacon.
 *
 * @param pos Block position.
 * @param state Block state.
 *
 * @property upgrade Activated beacon upgrade.
 * @property levels Amount of placed iron, gold, emerald or diamond blocks.
 * @property propertyDelegate Delegate of properties to sync with the screen handler.
 * @property beamSegments Segments of the beacon beam.
 * @property beaconRange Range of the beacon effect.
 * @property currentLevel The required level for the currently activated [upgrade].
 */
class ModBeaconBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(ModBlockEntityTypes.MOD_BEACON, pos, state), NamedScreenHandlerFactory {

    private var lock: ContainerLock = ContainerLock.EMPTY
    var customName: Text? = null

    var upgrade: BeaconUpgrade? by Delegates.observable(null) { _, old, new ->
        old?.deactivate(this)
        new?.activate(this)
    }

    private val levels: IntArray = intArrayOf(0,0,0,0)
    private val propertyDelegate = BeaconPropertyDelegate()

    private var _beamSegments: MutableList<ModBeamSegment> = mutableListOf()
    var beamSegments: MutableList<ModBeamSegment>
        get() = if (activeBase) _beamSegments else mutableListOf()
        private set(value) { _beamSegments = value }

    private val range: Int
        get() = BeaconUpgradeTier.levelToTier(levels.sum())*10 + 10

    val beaconRange: Box?
        get() = world?.run {
            Box(pos).expand(range.toDouble()).stretch(0.0, height.toDouble(), 0.0)
        }

    val currentLevel: Int
        get() = BeaconUpgrades.dataOf(upgrade)?.tier?.type?.idx.let { if (it != null) levels[it] else 0  }

    /**
     * Whether at least one of the [levels] is greater than 0, meaning the base is build properly.
     */
    private val activeBase: Boolean
        get() = levels.any { it > 0 }

    /**
     * Whether the [beamSegments] ar not empty, meaning a beam is active.
     */
    private val activeBeam: Boolean
        get() = beamSegments.isNotEmpty()

    /**
     * Whether the [levels] are high enough to activate the current [upgrade].
     */
    private val activeLevel: Boolean
        get() = BeaconUpgrades.dataOf(upgrade)?.tier?.checkLevel(currentLevel) == true

    private var minY: Int = 0

    override fun getDisplayName(): Text = customName ?: Text.translatable("container.beacon")

    override fun setWorld(world: World) {
        super.setWorld(world)
        minY = world.bottomY - 1
    }

    override fun markRemoved() {
        deactivate(world!!, pos)
        super.markRemoved()
    }

    override fun createMenu(i: Int, playerInventory: PlayerInventory?, playerEntity: PlayerEntity?): ScreenHandler? {
        requireNotNull(playerInventory) { "playerInventory must not be null!" }
        return if (LockableContainerBlockEntity.checkUnlocked(playerEntity, lock, displayName))
            ModBeaconScreenHandler(i, playerInventory, propertyDelegate, ScreenHandlerContext.create(world, pos))
        else null;
    }

    override fun toUpdatePacket(): Packet<ClientPlayPacketListener>? {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        return createNbt()
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        upgrade = BeaconUpgrades[nbt.getInt("upgrade")]
        if (nbt.contains("CustomName", 8)) {
            customName = Text.Serializer.fromJson(nbt.getString("CustomName"))
        }
        lock = ContainerLock.fromNbt(nbt)
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        nbt.putInt("upgrade", BeaconUpgrades.indexOf(upgrade))
        if (customName != null) {
            nbt.putString("CustomName", Text.Serializer.toJson(customName))
        }
        lock.writeNbt(nbt)
    }

    /**
     * Called when activating the beacon.
     */
    private fun activate(world: World, pos: BlockPos) {
        BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_ACTIVATE)
        upgrade?.activate(this)
        val players = world.getNonSpectatingEntities(
            ServerPlayerEntity::class.java,
            boxOf(pos.x, pos.y, pos.z, pos.x, (pos.y - 4), pos.z).expand(10.0, 5.0, 10.0)
        )
        for (player in players) {
            Criteria.CONSTRUCT_BEACON.trigger(player, currentLevel) // TODO: Update level
        }
    }

    /**
     * Called when deactivating the beacon.
     */
    private fun deactivate(world: World, pos: BlockPos) {
        BeaconBlockEntity.playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE)
        upgrade?.deactivate(this)
    }

    /**
     * Ticks the [ModBeaconBlockEntity].
     */
    private fun tick(world: World, pos: BlockPos, state: BlockState) {
        // TODO: Possibly send levels value by networking to screen, in order to prevent flicker

        val activeBaseOld = activeBase
        val activeBeamOld = activeBeam
        val activeLevelOld = activeLevel

        tickLevels(world, pos)
        if (activeBase) {
            tickBeam(world, pos)
            if (world.time % 80L == 0L && activeBeam) {
                tickUpgrade(world, pos, state)
                playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT)
            }
        }
        tickActivation(world, pos, activeBaseOld, activeBeamOld, activeLevelOld)
    }

    /**
     * Ticks the [levels] property by updating its values.
     */
    private fun tickLevels(world: World, pos: BlockPos) {

        /**
         * Checks whether the levels contain any other block than valid base blocks.
         */
        fun Map<Block, Int>.invalid(): Boolean {
            return this.any {
                it.key != Blocks.IRON_BLOCK
                        && it.key != Blocks.GOLD_BLOCK
                        && it.key != Blocks.EMERALD_BLOCK
                        && it.key != Blocks.DIAMOND_BLOCK
            }
        }

        // Clear old levels
        levels[0] = 0
        levels[1] = 0
        levels[2] = 0
        levels[3] = 0

        // Set new levels
        val base = baseBlocks(world, pos)
        for (baseLevel in base) {
            if (baseLevel.invalid()) break
            levels[0] += baseLevel[Blocks.IRON_BLOCK] ?: 0
            levels[1] += baseLevel[Blocks.GOLD_BLOCK] ?: 0
            levels[2] += baseLevel[Blocks.EMERALD_BLOCK] ?: 0
            levels[3] += baseLevel[Blocks.DIAMOND_BLOCK] ?: 0
        }
    }

    /**
     * Counts the amount of blocks that make up the beacon base.
     * @return A list of all blocks per y-level. Sorted from highest to lowest y value.
     */
    private fun baseBlocks(world: World, pos: BlockPos): List<Map<Block, Int>> {
        val res: List<MutableMap<Block, Int>> = listOf(
            mutableMapOf(), mutableMapOf(), mutableMapOf(), mutableMapOf()
        )
        for (dy in 1..4) {
            val level = res[dy-1]
            for (dz in -dy..dy) {
                for (dx in -dy..dy) {
                    val block = world.getBlockState(BlockPos(
                        pos.x + dx,
                        pos.y - dy,
                        pos.z + dz)
                    ).block
                    level[block] = level.getOrPut(block) { 0 } + 1
                }
            }
        }
        return res
    }

    /**
     * Ticks the beacon beam updating beam segments [beamSegments].
     */
    private fun tickBeam(world: World, pos: BlockPos) {
        var blockPos: BlockPos
        if (minY < pos.y) {
            blockPos = pos
            beamSegments.clear()
            minY = pos.y - 1
        } else {
            blockPos = BlockPos(pos.x, minY + 1, pos.z)
        }

        var beamSegment: ModBeamSegment? = beamSegments.lastOrNull()
        val maxY: Int = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.x, pos.z)

        while (blockPos.y <= maxY) {
            val blockState = world.getBlockState(blockPos)
            val block = blockState.block

            if (block is Stainable) {
                val color = block.color.colorComponents
                if (beamSegments.size <= 1) {
                    beamSegment = ModBeamSegment(color)
                    beamSegments += beamSegment
                } else if (beamSegment != null) {
                    if (color contentEquals beamSegment.color) {
                        beamSegment.increaseHeight()
                    } else {
                        beamSegment = ModBeamSegment(
                            floatArrayOf(
                                (beamSegment.color[0] + color[0]) / 2,
                                (beamSegment.color[1] + color[1]) / 2,
                                (beamSegment.color[2] + color[2]) / 2
                            )
                        )
                        beamSegments += beamSegment
                    }
                }
            } else {
                if (beamSegment == null || blockState.getOpacity(world, blockPos) >= 15 && !blockState.isOf(Blocks.BEDROCK)) {
                    beamSegments.clear()
                    minY = maxY
                    break
                }

                beamSegment.increaseHeight()
            }

            blockPos = blockPos.up()
            minY++
        }
    }

    /**
     * Ticks the active [upgrade] by invoking it and plays the [SoundEvents.BLOCK_BEACON_AMBIENT] sound.
     */
    private fun tickUpgrade(world: World, pos: BlockPos, state: BlockState) {
        if (world.isClient || upgrade == null) return

        if (activeLevel) {
            upgrade!!(world, pos, state, this)
        }

    }

    /**
     * Ticks the activation and deactivation of the beacon, by comparing the
     * beacons properties with the provided old values.
     * @param activeBaseOld The old property value.
     * @param activeBeamOld The old property value.
     * @param activeLevelOld The old property value.
     */
    private fun tickActivation(world: World, pos: BlockPos, activeBaseOld: Boolean, activeBeamOld: Boolean, activeLevelOld: Boolean) {
        minY = world.bottomY - 1
        if (world.isClient) return

        if (!activeBeamOld && activeBeam || !activeLevelOld && activeLevel) {
            upgrade?.activate(this)
        } else if (activeBeamOld && !activeBeam || activeLevelOld && !activeLevel) {
            upgrade?.deactivate(this)
        }

        if (!activeBaseOld && activeBase) {
            activate(world, pos)
        } else if (activeBaseOld && !activeBase) {
            deactivate(world, pos)
        }
    }

    /**
     * Plays the given [sound] at the [pos] if the current [world] is server side.
     */
    private fun playSound(world: World?, pos: BlockPos, sound: SoundEvent) {
        if (world?.isClient == false) {
            world.playSound(null, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f)
        }
    }

    companion object {

        /**
         * Index of the [upgrade] property in [BeaconPropertyDelegate].
         */
        const val IDX_UPGRADE = 4

        /**
         * Provides the [tick] function of a [ModBeaconBlockEntity].
         */
        fun <T : BlockEntity> ticker(type: BlockEntityType<T>): BlockEntityTicker<T>? {
            return if (type == ModBlockEntityTypes.MOD_BEACON)
                BlockEntityTicker { world, pos, state, blockEntity -> (blockEntity as ModBeaconBlockEntity).tick(world, pos, state) }
            else null
        }
    }

    /**
     * The property delegate of a [ModBeaconBlockEntity].
     * The stored properties are:
     * @property levels [ModBeaconBlockEntity.levels] at indices 0-3.
     * @property upgrade [ModBeaconBlockEntity.upgrade] at index 4.
     */
    inner class BeaconPropertyDelegate : PropertyDelegate {

        init {
            // Set initial values
            this.levels = this@ModBeaconBlockEntity.levels
            this.upgrade = this@ModBeaconBlockEntity.upgrade
        }

        override fun get(i: Int): Int {
            return when(i) {
                in 0..3 -> this@ModBeaconBlockEntity.levels[i]
                IDX_UPGRADE -> BeaconUpgrades.indexOf(this@ModBeaconBlockEntity.upgrade)
                else -> error("Index $i out of bounds.")
            }
        }

        override fun set(i: Int, value: Int) {
            when(i) {
                in 0..3 -> { this@ModBeaconBlockEntity.levels[i] = value }
                IDX_UPGRADE -> {
                    if (activeBeam) playSound(world, pos, SoundEvents.BLOCK_BEACON_POWER_SELECT)
                    this@ModBeaconBlockEntity.upgrade = BeaconUpgrades[value]
                }
            }
        }

        override fun size(): Int = 5
    }

    /**
     * Segment of the beacons beam with the given [color] and [height].
     */
    data class ModBeamSegment(val color: FloatArray, internal var height: Int = 1) {

        internal fun increaseHeight() { ++height }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ModBeamSegment

            if (!color.contentEquals(other.color)) return false
            return height == other.height
        }

        override fun hashCode(): Int {
            var result = color.contentHashCode()
            result = 31 * result + height
            return result
        }
    }
}