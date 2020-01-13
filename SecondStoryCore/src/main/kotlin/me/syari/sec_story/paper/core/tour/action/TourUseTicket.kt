package me.syari.sec_story.paper.core.tour.action

import me.syari.sec_story.paper.core.tour.Tour.getPlay
import me.syari.sec_story.paper.core.tour.Tour.getTour
import org.bukkit.entity.Player

class TourUseTicket: TourAction {
    override fun run(p: Player) {
        val play = p.getPlay() ?: return
        val tour = getTour(play.id) ?: return
        tour.remTicket(p)
    }
}