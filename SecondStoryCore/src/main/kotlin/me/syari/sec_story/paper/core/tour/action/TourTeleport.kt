package me.syari.sec_story.paper.core.tour.action

import org.bukkit.Location
import org.bukkit.entity.Player

class TourTeleport(val loc: Location): TourAction {
    override fun run(p: Player) {
        p.teleport(loc)
    }
}