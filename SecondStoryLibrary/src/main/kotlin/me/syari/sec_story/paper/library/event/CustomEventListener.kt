package me.syari.sec_story.paper.library.event

import me.syari.sec_story.paper.library.Main.Companion.plugin
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.*

object CustomEventListener: EventInit {
    private val deathCT = mutableSetOf<UUID>()

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageEvent) {
        val p = e.entity as? Player ?: return
        if(deathCT.contains(p.uniqueId)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun on(e: PlayerDeathEvent) {
        val p = e.entity ?: return
        if(! deathCT.contains(p.uniqueId)) {
            val newEvent = PlayerDeathWithCtEvent(p, e.deathMessage)
            newEvent.callEvent()
            e.deathMessage = newEvent.deathMessage
            e.isCancelled = newEvent.isCancelled
            if(newEvent.isCancelled) {
                deathCT.add(p.uniqueId)
                runLater(plugin, 3) {
                    deathCT.remove(p.uniqueId)
                }
            }
        }
    }
}