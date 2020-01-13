package me.syari.sec_story.paper.core.server

import me.syari.sec_story.paper.core.Main.Companion.plugin
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.scheduler.CustomScheduler.runLater
import org.bukkit.Effect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import java.util.*

object DoubleJump: EventInit {
    private val jumpTime = mutableMapOf<UUID, Long>()
    private val noFinished = mutableSetOf<UUID>()

    @EventHandler
    fun on(e: PlayerJoinEvent) {
        val p = e.player
        val uuid = p.uniqueId
        jumpTime.remove(uuid)
        if(noFinished.contains(uuid)) {
            noFinished.remove(uuid)
            p.allowFlight = false
            p.isFlying = false
        }
    }

    @EventHandler
    fun on(e: EntityDamageEvent) {
        val ent = e.entity
        if(ent is Player) {
            val uuid = ent.uniqueId
            val time = jumpTime[uuid]
            if(time != null) {
                val secs = (System.currentTimeMillis() - time) / 1000
                if(secs > 15) {
                    jumpTime.remove(uuid)
                } else if(e.cause == EntityDamageEvent.DamageCause.FALL) {
                    e.isCancelled = true
                }
            }
        }
    }

    @EventHandler
    fun on(e: PlayerToggleFlightEvent) {
        val p = e.player
        val uuid = p.uniqueId
        if(p.gameMode != GameMode.SURVIVAL) {
            if(p.gameMode != GameMode.CREATIVE) {
                p.isFlying = false
                p.allowFlight = false
            }
            return
        }
        e.isCancelled = true
        p.allowFlight = false
        p.isFlying = false
        p.velocity = p.location.direction.multiply(1.0).setY(1)
        noFinished.add(uuid)
        val loc = p.location
        loc.world.playEffect(loc, Effect.SMOKE, 5)
        loc.world.playSound(loc, Sound.ENTITY_ENDERDRAGON_SHOOT, 2.0f, 0.0f)
        jumpTime[uuid] = java.lang.Long.valueOf(System.currentTimeMillis())
        runLater(plugin, 20) {
            if(p.isFlying) {
                p.allowFlight = false
                noFinished.remove(uuid)
            }
        }
    }

    @EventHandler
    fun on(e: PlayerMoveEvent) {
        val p = e.player
        val loc = p.location
        if(p.gameMode == GameMode.SURVIVAL && loc.subtract(0.0, 1.0, 0.0).block.type !== Material.AIR) {
            p.allowFlight = true
        }
    }

    fun onDisable() {
        plugin.server.onlinePlayers.forEach { p ->
            if(p.gameMode != GameMode.CREATIVE) {
                p.isFlying = false
                p.allowFlight = false
            }
        }
    }
}