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

import org.dynmap.DynmapLocation
import org.dynmap.common.DynmapPlayer
import java.net.InetSocketAddress

class FabricDynmapOfflinePlayer(private val name: String) : DynmapPlayer {
    override fun isSneaking(): Boolean {
        return false
    }

    override fun getName(): String {
        return name
    }

    override fun isOp(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isConnected(): Boolean {
        return false
    }

    override fun getFirstLoginTime(): Long {
        TODO("Not yet implemented")
    }

    override fun hasPrivilege(p0: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLastLoginTime(): Long {
        TODO("Not yet implemented")
    }

    override fun getHealth(): Double {
        return 20.0
    }

    override fun hasPermissionNode(p0: String?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getArmorPoints(): Int {
        return 0
    }

    override fun getWorld(): String {
        TODO("Not yet implemented")
    }

    override fun isOnline(): Boolean {
        return false
    }

    override fun sendMessage(p0: String?) {
        TODO("Not yet implemented")
    }

    override fun isInvisible(): Boolean {
        return false
    }

    override fun getLocation(): DynmapLocation {
        TODO("Not yet implemented")
    }

    override fun getBedSpawnLocation(): DynmapLocation? {
        return null
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun getSortWeight(): Int {
        TODO("Not yet implemented")
    }

    override fun setSortWeight(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun getAddress(): InetSocketAddress? {
        return null
    }
}
