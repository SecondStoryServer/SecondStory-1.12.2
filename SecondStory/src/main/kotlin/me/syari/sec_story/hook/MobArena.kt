package me.syari.sec_story.hook

import com.garbagemule.MobArena.MobArena
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.entity.Player

object MobArena {
    private val hook: MobArena? = plugin.server.pluginManager.getPlugin("MobArena") as? MobArena

    fun nowMobArena(p: Player): Boolean {
        if(hook == null) return false
        return hook.arenaMaster.getArenaWithPlayer(p) != null
    }
}