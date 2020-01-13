package me.syari.sec_story.tour.action

import me.syari.sec_story.tour.Tour.cancelMove
import org.bukkit.entity.Player

class TourCancelMove(private val bool: Boolean) : TourAction {
    override fun run(p: Player) {
        if(bool){
            cancelMove.add(p.uniqueId)
        } else {
            cancelMove.remove(p.uniqueId)
        }
    }
}