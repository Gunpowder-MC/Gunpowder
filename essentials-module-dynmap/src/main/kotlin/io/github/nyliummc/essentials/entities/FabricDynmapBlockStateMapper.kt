/*
 * MIT License
 *
 * Copyright (c) NyliumMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
