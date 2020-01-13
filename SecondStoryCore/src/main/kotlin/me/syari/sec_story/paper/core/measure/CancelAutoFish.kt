package me.syari.sec_story.paper.core.measure

import me.syari.sec_story.paper.library.init.EventInit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerFishEvent

object CancelAutoFish: EventInit {
    @EventHandler
    fun on(e: PlayerFishEvent) {
        if(e.state == PlayerFishEvent.State.CAUGHT_FISH) {
            val loc = e.hook?.location ?: return
            for(xd in - 1..1) {
                for(yd in - 1..1) {
                    for(zd in - 1..1) {
                        val tmp = loc.clone()
                        tmp.add(xd.toDouble(), yd.toDouble(), zd.toDouble())
                        if(tmp.block?.type == Material.STRING) {
                            e.isCancelled = true
                            return
                        }
                    }
                }
            }
        }
    }

}