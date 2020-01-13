package me.syari.sec_story.paper.core.world.portal

import me.syari.sec_story.paper.core.world.portal.Portal.config
import me.syari.sec_story.paper.library.command.RunCommand.runCommand
import me.syari.sec_story.paper.library.message.SendMessage.action
import me.syari.sec_story.paper.library.world.CustomLocation
import me.syari.sec_story.paper.library.world.Region
import org.bukkit.Location
import org.bukkit.entity.Player

class PortalData(
    val name: String, c1: Location, c2: Location, to: Location, enable: Boolean, perm: String?, needSneak: Boolean, private val cmdOnTp: List<String>
) {
    val region = Region(c1, c2)

    fun inPortal(loc: Location) = region.inRegion(loc)

    fun canUse(p: Player) = perm == null || p.hasPermission(perm) || p.isOp

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

    fun tp(p: Player) {
        if(needSneak && ! p.isSneaking) {
            p.action("&7>> &6&lしゃがむとテレポートします &7<<")
            return
        }
        p.teleport(to)
        cmdOnTp.forEach { f ->
            runCommand(p, f)
        }
    }
}