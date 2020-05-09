package io.github.nyliummc.essentials.entities

import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.registry.Registry
import org.dynmap.renderer.DynmapBlockState
import java.util.*


class FabricDynmapBlockStateMapper {
    private val dynmapBlockStateMap: MutableMap<BlockState, DynmapBlockState> = HashMap<BlockState, DynmapBlockState>()
    operator fun get(state: BlockState): DynmapBlockState? {
        return dynmapBlockStateMap[state]
    }

    fun init() {
        dynmapBlockStateMap.clear()
        for (block in Registry.BLOCK) {
            val blockName: String = Registry.BLOCK.getId(block).toString()
            val states: MutableList<BlockState> = block.stateManager.states

            // first, add default state
            var defaultState: DynmapBlockState? = null
            for (stateIdx in states.indices) {
                val state = states[stateIdx]
                val dynmapState = DynmapBlockState(
                        defaultState, stateIdx,
                        blockName, BlockStateUtil.propertiesToString(state),
                        state.material.toString()
                )
                dynmapBlockStateMap[state] = dynmapState
                if (defaultState == null) {
                    defaultState = dynmapState
                }
                if (state.isAir) dynmapState.setAir() else {
                    if (state.material.isSolid) dynmapState.setSolid()
                    if (state.material == Material.WOOD) dynmapState.setLog() else if (state.material == Material.LEAVES) dynmapState.setLeaves()
                }
                if (state.properties.contains(Properties.WATERLOGGED) && state[Properties.WATERLOGGED]) {
                    dynmapState.setWaterlogged()
                }
            }
        }
    }

    companion object {
        val INSTANCE = FabricDynmapBlockStateMapper()
    }
}
