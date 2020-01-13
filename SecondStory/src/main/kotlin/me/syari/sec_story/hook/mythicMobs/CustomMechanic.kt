package me.syari.sec_story.hook.mythicMobs

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent
import me.syari.sec_story.plugin.Init
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object CustomMechanic : Init(), Listener {
    @EventHandler
    fun on(e: MythicMechanicLoadEvent){
        /*
        when(e.mechanicName.toLowerCase()){
            "fooditem" -> FoodItemMechanic(e.container.configLine, e.config)
            else -> return
        }.let { e.register(it) }
         */
    }
}