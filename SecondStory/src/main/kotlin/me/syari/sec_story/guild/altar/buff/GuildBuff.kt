package me.syari.sec_story.guild.altar.buff

import me.syari.sec_story.guild.Guild.guild
import me.syari.sec_story.plugin.Init
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause

object GuildBuff: Init(), Listener {
    @EventHandler
    fun on(e: EntityDamageEvent){
        val p = e.entity as? Player ?: return
        val guild = p.guild ?: return
        when(e.cause){
            DamageCause.FIRE, DamageCause.FIRE_TICK -> {

            }
            DamageCause.POISON -> {

            }
            DamageCause.WITHER -> {

            }
        }
    }
}