package me.syari.sec_story.paper.core.tour.action

import me.syari.sec_story.paper.core.data.SaveData.saveLocation
import org.bukkit.entity.Player

class TourSaveLocation: TourAction {
    override fun run(p: Player) {
        p.saveLocation()
    }
}