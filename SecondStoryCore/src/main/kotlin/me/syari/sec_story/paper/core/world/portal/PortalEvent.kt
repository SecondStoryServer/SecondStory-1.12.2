package me.syari.sec_story.paper.core.world.portal

import me.syari.sec_story.paper.core.world.portal.Portal.getPortal
import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent

object PortalEvent: EventInit {
    @EventHandler
    fun on(e: PlayerMoveEvent) {
        val f = e.from.toBlockLocation()
        val t = e.to.toBlockLocation()
        if(f != t) {
            val p = e.player
            val portal = getPortal(p) ?: return
            val run = PortalTeleportEvent(p).callEvent()
            if(run) {
                portal.tp(p)
            }
        }
    }
}