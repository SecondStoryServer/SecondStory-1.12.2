package me.syari.sec_story.hook

import com.onarandombox.MultiverseCore.MultiverseCore
import com.onarandombox.MultiverseCore.api.MultiverseWorld
import me.syari.sec_story.plugin.Plugin
import org.bukkit.World

object MultiverseCore {
    private val hook = Plugin.plugin.server.pluginManager.getPlugin("Multiverse-Core") as MultiverseCore

    val World.toMVWorld: MultiverseWorld
        get() = hook.mvWorldManager.getMVWorld(this)

    val firstSpawnWorld: MultiverseWorld get() = hook.mvWorldManager.firstSpawnWorld
}