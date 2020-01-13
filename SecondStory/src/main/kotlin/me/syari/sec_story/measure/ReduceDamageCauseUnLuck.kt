package me.syari.sec_story.measure

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.plugin.Init
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffectType

object ReduceDamageCauseUnLuck: Init(), Listener {
    private fun calc(p: Player): Double? {
        val eff = p.getPotionEffect(PotionEffectType.UNLUCK) ?: return null
        val lv = eff.amplifier
        var multi = 1.0 - (lv / 10.0)
        if(multi < 0) multi = 0.0
        p.action("&c&lデバフでダメージが &c&l&n${multi * 100}%&c&l に減少")
        return multi
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponDamageEntityEvent){
        val p = e.player
        val multi = calc(p) ?: return
        e.damage *= multi
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageByEntityEvent){
        val p = e.damager as? Player ?: return
        val multi = calc(p) ?: return
        e.damage *= multi
    }
}