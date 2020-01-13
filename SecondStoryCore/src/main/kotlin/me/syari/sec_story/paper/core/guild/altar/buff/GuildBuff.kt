package me.syari.sec_story.paper.core.guild.altar.buff

import me.syari.sec_story.paper.core.guild.Guild.guild
import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

object GuildBuff: EventInit {
    @EventHandler
    fun on(e: EntityDamageEvent) {
        val p = e.entity as? Player ?: return
        val guild = p.guild ?: return
        when(e.cause) {
            DamageCause.FIRE, DamageCause.FIRE_TICK -> {

            }
            DamageCause.POISON -> {

            }
            DamageCause.WITHER -> {

            }
        }
    }
}