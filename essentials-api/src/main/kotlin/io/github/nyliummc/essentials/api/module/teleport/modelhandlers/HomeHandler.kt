package io.github.nyliummc.essentials.api.module.teleport.modelhandlers

import io.github.nyliummc.essentials.api.module.teleport.dataholders.StoredHome
import java.util.*

interface HomeHandler {
    fun getHome(user: UUID, home: String): StoredHome?

    fun newHome(home: StoredHome): Boolean

    fun getHomes(user: UUID): Map<String, StoredHome>

    fun delHome(player: UUID, home: String): Boolean
}
