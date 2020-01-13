package me.syari.sec_story.paper.library.init

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

interface EventInit: Listener {
    companion object {
        fun register(plugin: JavaPlugin, vararg event: EventInit) {
            event.forEach {
                plugin.server.pluginManager.registerEvents(it, plugin)
            }
        }
    }
}