package me.syari.sec_story.paper.core.measure

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerTeleportEvent

object LeaveVehicle: EventInit {
    @EventHandler
    fun on(e: PlayerTeleportEvent) {
        val p = e.player ?: return
        plugin.server.onlinePlayers.forEach { o ->
            val v = o.vehicle
            if(v is Player && v == p) {
                o.leaveVehicle()
                return
            }
        }
    }

    @EventHandler
    fun on(e: PlayerDeathEvent) {
        val p = e.entity ?: return
        p.getNearbyEntities(1.0, 3.0, 1.0).forEach { o ->
            val v = o.vehicle
            if(v is Player && v == p) {
                o.leaveVehicle()
                return
            }
        }
    }
}