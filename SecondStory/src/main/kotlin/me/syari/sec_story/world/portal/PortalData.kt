package me.syari.sec_story.world.portal

import me.syari.sec_story.lib.CustomLocation
import me.syari.sec_story.lib.message.SendMessage.action
import me.syari.sec_story.plugin.Plugin.cmd
import me.syari.sec_story.world.portal.Portal.config
import org.bukkit.Location
import org.bukkit.entity.Player

class PortalData(val name: String, c1: Location, c2: Location, to: Location, enable: Boolean, perm: String?, needSneak: Boolean, val cmdOnTp: List<String>){
    val max = Location(c1.world, max(c1.blockX, c2.blockX).toDouble(), max(c1.blockY, c2.blockY).toDouble(), max(c1.blockZ, c2.blockZ).toDouble())
    val min = Location(c1.world, min(c1.blockX, c2.blockX).toDouble(), min(c1.blockY, c2.blockY).toDouble(), min(c1.blockZ, c2.blockZ).toDouble())

    fun inPortal(l: Location) = max.world == l.world && min.x <= l.x && l.x <= max.x && min.y <= l.y && l.y <= max.y && min.z <= l.z && l.z <= max.z

    fun canUse(p: Player) = perm == null || p.hasPermission(perm) || p.isOp

    private fun max(a: Int, b: Int) = if (a > b) a else b
    private fun min(a: Int, b: Int) = if (a > b) b else a

    var enable: Boolean = enable
        set(value) {
            config.set("portal.$name.enable", value)
            field = value
        }

    var perm: String? = perm
        set(value) {
            config.set("portal.$name.perm", value)
            field = value
        }

    var to = to
        set(value) {
            config.set("portal.$name.to", CustomLocation(value).toStringWithYawPitch)
            field = value
        }

    var needSneak = needSneak
        set(value) {
            config.set("portal.$name.needSneak", if(value) true else null)
            field = value
        }

    fun tp(p: Player){
        if(needSneak && !p.isSneaking){
            p.action("&7>> &6&lしゃがむとテレポートします &7<<")
            return
        }
        p.teleport(to)
        cmdOnTp.forEach { f ->
            p.cmd(f)
        }
    }
}