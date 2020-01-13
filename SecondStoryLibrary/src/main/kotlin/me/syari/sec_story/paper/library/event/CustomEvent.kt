package me.syari.sec_story.paper.library.event

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.plugin.java.JavaPlugin

open class CustomEvent: Event() {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}