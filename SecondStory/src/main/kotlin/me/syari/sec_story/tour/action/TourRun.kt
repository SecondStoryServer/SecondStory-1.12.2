package me.syari.sec_story.tour.action

import me.syari.sec_story.tour.Tour.end
import me.syari.sec_story.tour.Tour.getTour
import org.bukkit.entity.Player

class TourRun(val to: String): TourAction {
    override fun run(p: Player) {
        val tour = getTour(to) ?: return
        if(tour.canStart(p)){
            p.end(true)
            tour.start(p)
        }
    }
}