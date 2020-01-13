package me.syari.sec_story.lib.event

import me.syari.sec_story.plugin.Init
import me.syari.sec_story.plugin.Plugin.plugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

object CustomEventListener : Listener, Init(){
    private val deathCT = mutableSetOf<UUID>()

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageEvent){
        val p = e.entity as? Player ?: return
        if(deathCT.contains(p.uniqueId)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: PlayerDeathEvent){
        val p = e.entity ?: return
        if(!deathCT.contains(p.uniqueId)){
            val newEvent = PlayerDeathWithCtEvent(p, e.deathMessage)
            newEvent.callEvent()
            e.deathMessage = newEvent.deathMessage
            e.isCancelled = newEvent.isCancelled
            if(newEvent.isCancelled){
                deathCT.add(p.uniqueId)
                object : BukkitRunnable(){
                    override fun run() {
                        deathCT.remove(p.uniqueId)
                    }
                }.runTaskLater(plugin, 3)
            }
        }
    }
}