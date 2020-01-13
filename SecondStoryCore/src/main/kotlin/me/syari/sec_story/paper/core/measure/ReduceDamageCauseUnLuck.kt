package me.syari.sec_story.paper.core.measure

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.message.SendMessage.action
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.potion.PotionEffectType

object ReduceDamageCauseUnLuck: EventInit {
    private fun calc(p: Player): Double? {
        val eff = p.getPotionEffect(PotionEffectType.UNLUCK) ?: return null
        val lv = eff.amplifier
        var multi = 1.0 - (lv / 10.0)
        if(multi < 0) multi = 0.0
        p.action("&c&lデバフでダメージが &c&l&n${multi * 100}%&c&l に減少")
        return multi
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponDamageEntityEvent) {
        val p = e.player
        val multi = calc(p) ?: return
        e.damage *= multi
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: EntityDamageByEntityEvent) {
        val p = e.damager as? Player ?: return
        val multi = calc(p) ?: return
        e.damage *= multi
    }
}