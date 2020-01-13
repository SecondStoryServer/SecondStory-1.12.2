package me.syari.sec_story.paper.core.rank

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent
import com.shampaggon.crackshot.events.WeaponTriggerEvent
import me.syari.sec_story.paper.core.command.CommandCancel.CommandAddCause
import me.syari.sec_story.paper.core.hook.CrackShot
import me.syari.sec_story.paper.core.perm.PermissionLoadEvent
import me.syari.sec_story.paper.core.rank.Ranks.rank
import me.syari.sec_story.paper.library.code.StringEditor.toUncolor
import me.syari.sec_story.paper.library.init.EventInit
import me.syari.sec_story.paper.library.item.CustomItemStack
import me.syari.sec_story.paper.library.message.SendMessage.action
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

object RankEvent: EventInit {
    private val CustomItemStack.reqPerm
        get(): String? {
            if(hasItemMeta && hasLore) {
                lore.forEach { s ->
                    val req = s.reqPerm
                    if(req != null) return req
                }
            }
            return null
        }

    private val String.reqPerm
        get(): String? {
            val t = toUncolor.split(Regex("\\s+"))
            if(t.size == 2 && t[0].toLowerCase() == "必要等級:") {
                return t[1]
            }
            return null
        }

    private fun Player?.checkWeapon(title: String): Boolean {
        val p = this ?: return true
        val g = CrackShot.getItemFromCrackShot(title) ?: return true
        val r = g.reqPerm ?: return true
        if(p.hasPermission(r)) return true
        p.action("&c必要等級に達していません &7必要等級: $r")
        return false
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

    @EventHandler
    fun on(e: PermissionLoadEvent) {
        val p = e.player
        e.addPermission(p.rank.perm)
        e.setAllowCommand(CommandAddCause.Rank, p.rank.cmd)
    }
}