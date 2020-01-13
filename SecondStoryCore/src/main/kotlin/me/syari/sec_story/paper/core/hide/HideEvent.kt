package me.syari.sec_story.paper.core.hide

import me.syari.sec_story.paper.core.hide.Hide.applyHide
import me.syari.sec_story.paper.core.hide.Hide.nowHiding
import me.syari.sec_story.paper.core.hide.Hide.setHide
import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent

object HideEvent: EventInit {
    @EventHandler
    fun on(e: PlayerJoinEvent) {
        val p = e.player
        if(p.nowHiding) {
            p.setHide(true)
        }
        if(! p.isOp) {
            applyHide(p)
        }
    }
}