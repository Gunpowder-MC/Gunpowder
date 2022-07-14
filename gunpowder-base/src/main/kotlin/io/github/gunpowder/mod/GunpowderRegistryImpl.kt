package io.github.gunpowder.mod

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableBiMap
import com.martmists.commons.logging.logger
import com.martmists.commons.config.ConfigLoader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.serialization.Dynamic
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Lifecycle
import io.github.gunpowder.api.types.Delegate
import io.github.gunpowder.api.GunpowderMod
import io.github.gunpowder.api.GunpowderRegistry
import io.github.gunpowder.api.types.ServerArgumentType
import io.github.gunpowder.api.types.SignType
import io.github.gunpowder.mixinterfaces.SyncPlayer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.datafixer.DataFixTypes
import net.minecraft.datafixer.Schemas
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.resource.DataPackSettings
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.dynamic.RegistryOps
import net.minecraft.util.registry.DynamicRegistryManager
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import net.minecraft.world.World
import net.minecraft.world.border.WorldBorder
import net.minecraft.world.border.WorldBorderListener
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.level.LevelInfo
import net.minecraft.world.level.LevelProperties
import net.minecraft.world.level.storage.LevelStorage
import net.minecraft.world.level.storage.SaveVersionInfo
import net.minecraft.world.spawner.Spawner
import org.apache.commons.io.FileUtils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

object GunpowderRegistryImpl : GunpowderRegistry, KoinComponent {
    private val mod by inject<GunpowderMod>()
    val server: MinecraftServer
        get() = mod.server
    private val dimTypeRegistry: SimpleRegistry<DimensionType>
        get() {
            return server.registryManager[Registry.DIMENSION_TYPE_KEY] as SimpleRegistry<DimensionType>
        }
    private val logger by logger()

    val netherMap = HashBiMap.create<RegistryKey<World>, RegistryKey<World>>()
    private val linkedWorldSet = mutableSetOf<RegistryKey<World>>(World.OVERWORLD, World.NETHER, World.END)
    private val custom = mutableSetOf<RegistryKey<World>>()

    init {
        netherMap[World.OVERWORLD] = World.NETHER
    }

    override fun hasDimensionType(dimensionTypeId: RegistryKey<DimensionType>): Boolean {
        return dimTypeRegistry.keyToEntry.containsKey(dimensionTypeId)
    }

    override fun addDimensionType(dimensionTypeId: RegistryKey<DimensionType>, dimensionType: DimensionType) {
        if (hasDimensionType(dimensionTypeId)) {
            throw IllegalArgumentException("DimensionType ${dimensionTypeId.value} already registered!")
        }

        Registry.register(dimTypeRegistry, dimensionTypeId.value, dimensionType)
    }

    override fun removeDimensionType(dimensionTypeId: RegistryKey<DimensionType>) {
        if (dimensionTypeId == DimensionType.OVERWORLD_REGISTRY_KEY || dimensionTypeId == DimensionType.THE_NETHER_REGISTRY_KEY || dimensionTypeId == DimensionType.THE_END_REGISTRY_KEY) {
            return  // don't remove default
        }

        if (!hasDimensionType(dimensionTypeId)) {
            return
        }

        val dtype = dimTypeRegistry.idToEntry[dimensionTypeId.value]
        dimTypeRegistry.idToEntry.remove(dimensionTypeId.value)
        dimTypeRegistry.keyToEntry.remove(dimensionTypeId)
        dimTypeRegistry.entryToLifecycle.remove(dtype?.value())
        dimTypeRegistry.rawIdToEntry.remove(dtype)
        dimTypeRegistry.entryToRawId.removeInt(dtype)
    }

    private fun getProps(key: RegistryKey<World>) : LevelProperties? {
        val target = File(server.session.getWorldDirectory(key).toFile(), "level.dat")
        if (target.exists()) {
            val tag = NbtIo.readCompressed(target)
            val dataFixer = Schemas.getFixer()
            val compoundTag3 = if (tag.contains("Player", 10)) tag.getCompound("Player") else null
            tag.remove("Player")
            val i = if (tag.contains("DataVersion", 99)) tag.getInt("DataVersion") else -1
            val dynamicOps: DynamicOps<NbtElement> =
                RegistryOps.of(NbtOps.INSTANCE, DynamicRegistryManager.createAndLoad())
            val dynamic = dataFixer.update(
                DataFixTypes.LEVEL.typeReference,
                Dynamic(dynamicOps, tag),
                i,
                SharedConstants.getGameVersion().saveVersion.id
            )
            val pair = LevelStorage.readGeneratorProperties(dynamic, dataFixer, i)
            val saveVersionInfo = SaveVersionInfo.fromDynamic(dynamic)
            val levelInfo = LevelInfo.fromDynamic(dynamic, DataPackSettings.SAFE_MODE)
            return LevelProperties.readProperties(
                dynamic,
                dataFixer,
                i,
                compoundTag3,
                levelInfo,
                saveVersionInfo,
                pair.first,
                pair.second
            )
        }
        return null
    }

    override fun addWorld(
        worldId: RegistryKey<World>,
        dimensionTypeId: RegistryKey<DimensionType>,
        chunkGenerator: ChunkGenerator,
        properties: LevelProperties,
        spawners: List<Spawner>
    ): ServerWorld {
        if (hasWorld(worldId)) {
            throw IllegalArgumentException("World ${worldId.value} already registered!")
        }

        val dimensionType = dimTypeRegistry.get(dimensionTypeId)!!
        val dimReference = dimTypeRegistry.idToEntry[dimensionTypeId.value]!!

        val overworld = server.getWorld(World.OVERWORLD)!!
        val worldGenerationProgressListener = overworld.chunkManager.threadedAnvilChunkStorage.worldGenerationProgressListener

        @Suppress("DEPRECATION")
        val seed = chunkGenerator.field_37261

        val worldBorder = object : WorldBorder() {
            override fun getCenterX(): Double {
                return super.getCenterX() / dimensionType.coordinateScale
            }

            override fun getCenterZ(): Double {
                return super.getCenterZ() / dimensionType.coordinateScale
            }
        }

        val targetDir = server.session.getWorldDirectory(worldId).toFile()
        targetDir.mkdirs()

        val props = getProps(worldId) ?: properties

        val world = ServerWorld(server, server.workerExecutor, server.session,
            props, worldId, dimReference,
            worldGenerationProgressListener, chunkGenerator,
            false, seed, spawners, !dimTypeRegistry.get(dimensionTypeId)!!.hasFixedTime())
        world.savingDisabled = false
        worldBorder.addListener(WorldBorderListener.WorldBorderSyncer(world.worldBorder))

        server.worlds[worldId] = world
        custom.add(worldId)

        for (player in server.playerManager.playerList) {
            logger.info("Marking needsSync for player $player")
            (player as SyncPlayer).setNeedsSync(true)
        }

        return world
    }

    override fun hasWorld(worldId: RegistryKey<World>): Boolean {
        return server.worldRegistryKeys.contains(worldId)
    }

    override fun removeWorld(worldId: RegistryKey<World>) {
        if (worldId == World.END || worldId == World.NETHER || worldId == World.OVERWORLD) {
            return  // Not deleting default worlds
        }

        if (!hasWorld(worldId)) {
            return  // No such world
        }

        val world = server.worlds.remove(worldId)
        if (world != null) {

            val path = world.server.session.getWorldDirectory(world.registryKey).toFile()
            FileUtils.deleteDirectory(path)

            for (player in server.playerManager.playerList) {
                if (player.spawnPointDimension.toString() == worldId.toString()) {
                    player.setSpawnPoint(null, null, 0.0f, false, false)
                }

                logger.debug("Marking needsSync for player $player")
                (player as SyncPlayer).setNeedsSync(true)

                // Teleport players in the dimension to be removed
                if (player.world == world) {
                    val sw = server.getWorlds().first {
                        it.registryKey.toString() == player.spawnPointDimension.toString()
                    }
                    val pos = player.spawnPointPosition ?: sw.spawnPos
                    player.teleport(sw, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), player.yaw, player.pitch)
                }
            }
        }
    }

    override fun linkNether(overworld: RegistryKey<World>, nether: RegistryKey<World>) {
        if (overworld in linkedWorldSet || nether in linkedWorldSet) {
            throw IllegalArgumentException("World already linked!")
        }
        linkedWorldSet.addAll(listOf(overworld, nether))
        netherMap[overworld] = nether
    }

    override fun getLinkedWorlds(): BiMap<RegistryKey<World>, RegistryKey<World>> {
        return ImmutableBiMap.copyOf(netherMap)
    }

    private val loader = ConfigLoader {
        ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)).also {
            it.registerModule(
                KotlinModule.Builder()
                    .withReflectionCacheSize(512)
                    .configure(KotlinFeature.NullToEmptyCollection, false)
                    .configure(KotlinFeature.NullToEmptyMap, false)
                    .configure(KotlinFeature.NullIsSameAsDefault, false)
                    .configure(KotlinFeature.SingletonSupport, false)
                    .configure(KotlinFeature.StrictNullChecks, false)
                    .build()
            )
        }
    }

    override fun <T> config(path: String, clazz: Class<T>): Delegate<T> {
        val default = clazz.getResourceAsStream(path) ?: throw IllegalArgumentException("No such resource in classpath: $path")
        val file = File("${FabricLoader.getInstance().configDir.toFile().absolutePath}/$path")
        return Delegate { _, _ ->
            return@Delegate loader.load(file, clazz, default)
        }
    }

    private val signRegistryKey = RegistryKey.ofRegistry<SignType>(Identifier("gunpowder:sign_type"))
    val signRegistry = SimpleRegistry(signRegistryKey, Lifecycle.experimental(), null)

    override fun register(signType: SignType) {
        Registry.register(signRegistry, signType.id, signType)
    }

    private val SAT_BY_TYPE: MutableMap<Class<*>, ServerArgumentType<*>> = HashMap()
    private val SAT_BY_ID: MutableMap<Identifier, ServerArgumentType<*>> = ConcurrentHashMap()
    private val satKnown = WeakHashMap<ServerPlayerEntity, MutableSet<Identifier>>()
    val satIds: Set<Identifier>
        get() = Collections.unmodifiableSet(SAT_BY_ID.keys)

    override fun register(argumentType: ServerArgumentType<*>) {
        SAT_BY_TYPE[argumentType.type] = argumentType
        SAT_BY_ID[argumentType.id] = argumentType
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : ArgumentType<*>> argumentTypeByClass(clazz: Class<T>): ServerArgumentType<T>? {
        return SAT_BY_TYPE[clazz] as? ServerArgumentType<T>
    }

    fun getKnownArgumentTypes(playerEntity: ServerPlayerEntity): Set<Identifier> {
        return satKnown.getOrPut(playerEntity) {
            HashSet()
        }
    }

    fun setKnownArgumentTypes(playerEntity: ServerPlayerEntity, types: Set<Identifier>) {
        satKnown.getOrPut(playerEntity) {
            HashSet()
        }.addAll(types)
        if (types.isNotEmpty()) {
            playerEntity.server.playerManager.sendCommandTree(playerEntity)
        }
    }
}
