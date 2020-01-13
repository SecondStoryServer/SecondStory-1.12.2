package me.syari.sec_story.paper.core.measure

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent
import com.shampaggon.crackshot.events.WeaponTriggerEvent
import me.syari.sec_story.paper.core.hook.CrackShot
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.message.SendMessage.action
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

object OnlyOneCrackShotWeapon: EventInit {
    private fun Player?.checkWeapon(title: String): Boolean {
        val p = this ?: return true
        val g = CrackShot.getItemFromCrackShot(title) ?: return true
        val inv = p.inventory ?: return true
        if(g.containsLore("&a複数所持可")) return true
        var cnt = 0
        for(index in listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 40)) {
            val item = inv.getItem(index)
            if(item != null) {
                val c = CrackShot.getTitleFromCrackShot(item)
                if(c != null && c == title) {
                    cnt ++
                    if(2 <= cnt) {
                        p.action("&c同じ銃をホットバーに入れた状態では使えません")
                        return false
                    }
                }
            }
        }
        return true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponPrepareShootEvent) {
        if(e.player.checkWeapon(e.weaponTitle)) return
        e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponDamageEntityEvent) {
        if(e.player.checkWeapon(e.weaponTitle)) return
        e.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun on(e: WeaponTriggerEvent) {
        if(e.player.checkWeapon(e.weaponTitle)) return
        e.isCancelled = true
    }
}